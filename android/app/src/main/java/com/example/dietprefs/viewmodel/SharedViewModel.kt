package com.example.dietprefs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dietprefs.Constants
import com.example.dietprefs.model.Preference
import com.example.dietprefs.model.SortColumn
import com.example.dietprefs.model.SortDirection
import com.example.dietprefs.model.SortState
import android.content.Context
import com.example.dietprefs.location.LocationService
import com.example.dietprefs.location.UserLocation
import com.example.dietprefs.network.models.VendorResponse
import com.example.dietprefs.network.models.ItemResponse
import com.example.dietprefs.network.models.AppConfig
import com.example.dietprefs.network.models.PricingConfig
import com.example.dietprefs.network.models.PaginationConfig
import com.example.dietprefs.network.models.LocationConfig
import com.example.dietprefs.network.models.SortingConfig
import com.example.dietprefs.repository.VendorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * SharedViewModel manages the application state and business logic.
 *
 * ## Filter Architecture
 * We support two types of filters that work together:
 *
 * 1. **Categorical Filters (Preferences)**:
 *    - Boolean yes/no selections (e.g., "vegetarian", "gluten-free")
 *    - Stored as Set<Preference> for each user
 *    - Toggled on/off via toggleUser1Pref() / toggleUser2Pref()
 *
 * 2. **Numeric Filters (Price)**:
 *    - Maximum price threshold (nullable Float)
 *    - Stored separately as Float? for each user
 *    - Set via setUser1MaxPrice() / setUser2MaxPrice()
 *
 * Both filter types are combined when calling the API via searchVendors().
 * The separation reflects their different data types and interaction patterns.
 */
class SharedViewModel(
    private val repository: VendorRepository = VendorRepository()
) : ViewModel() {

    // App configuration from backend
    private val _appConfig = MutableStateFlow<AppConfig?>(null)
    val appConfig: StateFlow<AppConfig?> = _appConfig.asStateFlow()

    // Categorical filters: Boolean preferences (vegetarian, gluten-free, etc.)
    private val _user1Prefs = MutableStateFlow<Set<Preference>>(emptySet())
    val user1Prefs: StateFlow<Set<Preference>> = _user1Prefs.asStateFlow()

    private val _user2Prefs = MutableStateFlow<Set<Preference>>(emptySet())
    val user2Prefs: StateFlow<Set<Preference>> = _user2Prefs.asStateFlow()

    // Numeric filter: Maximum price threshold
    private val _user1MaxPrice = MutableStateFlow<Float?>(null)
    val user1MaxPrice: StateFlow<Float?> = _user1MaxPrice.asStateFlow()

    private val _user2MaxPrice = MutableStateFlow<Float?>(null)
    val user2MaxPrice: StateFlow<Float?> = _user2MaxPrice.asStateFlow()

    // Text search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Display text from backend (formatted filter descriptions)
    private val _user1Display = MutableStateFlow("")
    val user1Display: StateFlow<String> = _user1Display.asStateFlow()

    private val _user2Display = MutableStateFlow("")
    val user2Display: StateFlow<String> = _user2Display.asStateFlow()

    // Vendor results state
    private val _pagedVendors = MutableStateFlow<List<DisplayVendor>>(emptyList())
    val pagedVendors: StateFlow<List<DisplayVendor>> = _pagedVendors.asStateFlow()

    private val _totalResultsCount = MutableStateFlow(0)
    val totalResultsCount: StateFlow<Int> = _totalResultsCount.asStateFlow()

    private val _visibleRange = MutableStateFlow(0 to 0)
    val visibleRange: StateFlow<Pair<Int, Int>> = _visibleRange.asStateFlow()

    // Caching for local sort operations - stores all fetched vendors
    private var cachedAllVendors = listOf<DisplayVendor>()

    // Cache full VendorResponse objects for navigation
    private var cachedVendorResponses = listOf<VendorResponse>()

    // Pagination state
    private var currentPage = 1  // API uses 1-based indexing
    private val pageSize = Constants.PAGE_SIZE
    private var totalPages = 0

    // Sorting state
    private val _sortState = MutableStateFlow(SortState())
    val sortState: StateFlow<SortState> = _sortState.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // User location
    private val _userLocation = MutableStateFlow<UserLocation?>(null)
    val userLocation: StateFlow<UserLocation?> = _userLocation.asStateFlow()

    // Debounce flag to prevent rapid successive sort operations
    private var isSorting = false

    // Restaurant detail state
    private val _selectedVendor = MutableStateFlow<VendorResponse?>(null)
    val selectedVendor: StateFlow<VendorResponse?> = _selectedVendor.asStateFlow()

    private val _menuItems = MutableStateFlow<List<ItemResponse>>(emptyList())
    val menuItems: StateFlow<List<ItemResponse>> = _menuItems.asStateFlow()

    private val _isLoadingItems = MutableStateFlow(false)
    val isLoadingItems: StateFlow<Boolean> = _isLoadingItems.asStateFlow()

    private val _selectedItemIndex = MutableStateFlow(0)
    val selectedItemIndex: StateFlow<Int> = _selectedItemIndex.asStateFlow()

    init {
        // Fetch app configuration on ViewModel initialization
        fetchConfig()
    }

    /**
     * Fetch application configuration from backend.
     * This provides centralized business rules (prices, pagination, etc.)
     * that are consistent across all clients.
     */
    fun fetchConfig() {
        viewModelScope.launch {
            repository.getConfig().onSuccess { config ->
                _appConfig.value = config
            }.onFailure { error ->
                // Log error but don't block app - fall back to hardcoded Constants
                android.util.Log.e("SharedViewModel", "Failed to fetch config", error)
            }
        }
    }

    fun updateVisibleRange(start: Int, end: Int) {
        _visibleRange.value = start to end
    }

    /**
     * Helper method to convert preferences to API format.
     * Eliminates duplication across searchVendors, loadNextPage, and fetchMenuItems.
     */
    private fun getApiPreferences(): Pair<List<String>, List<String>> {
        return Pair(
            _user1Prefs.value.map { it.apiName },
            _user2Prefs.value.map { it.apiName }
        )
    }

    /**
     * Helper method to convert sort state to API format.
     * Eliminates duplication between searchVendors and loadNextPage.
     */
    private fun getSortParameters(): Pair<String, String> {
        val sortBy = when (_sortState.value.column) {
            SortColumn.VENDOR_RATING -> "rating"
            SortColumn.DISTANCE -> "distance"
            SortColumn.MENU_ITEMS -> "item_count"
        }

        val sortDirection = when (_sortState.value.direction) {
            SortDirection.ASCENDING -> "asc"
            SortDirection.DESCENDING -> "desc"
        }

        return Pair(sortBy, sortDirection)
    }

    fun selectVendor(vendor: VendorResponse) {
        _selectedVendor.value = vendor
    }

    fun selectVendorByName(vendorName: String) {
        val vendor = cachedVendorResponses.find { it.name == vendorName }
        _selectedVendor.value = vendor
    }

    fun updateSelectedItemIndex(index: Int) {
        _selectedItemIndex.value = index
    }

    fun toggleUser1Pref(pref: Preference) {
        val currentPrefs = _user1Prefs.value.toMutableSet()
        if (pref in currentPrefs) {
            currentPrefs.remove(pref)
        } else {
            currentPrefs.add(pref)
        }
        _user1Prefs.value = currentPrefs
    }

    fun toggleUser2Pref(pref: Preference) {
        val currentPrefs = _user2Prefs.value.toMutableSet()
        if (pref in currentPrefs) {
            currentPrefs.remove(pref)
        } else {
            currentPrefs.add(pref)
        }
        _user2Prefs.value = currentPrefs
    }

    fun setUser1MaxPrice(price: Float?) {
        _user1MaxPrice.value = price
        // Sync LOW_PRICE preference with price state
        val currentPrefs = _user1Prefs.value.toMutableSet()
        if (price != null) {
            currentPrefs.add(Preference.LOW_PRICE)
        } else {
            currentPrefs.remove(Preference.LOW_PRICE)
        }
        _user1Prefs.value = currentPrefs
    }

    fun setUser2MaxPrice(price: Float?) {
        _user2MaxPrice.value = price
        // Sync LOW_PRICE preference with price state
        val currentPrefs = _user2Prefs.value.toMutableSet()
        if (price != null) {
            currentPrefs.add(Preference.LOW_PRICE)
        } else {
            currentPrefs.remove(Preference.LOW_PRICE)
        }
        _user2Prefs.value = currentPrefs
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Clears all filters for both users.
     * This includes both categorical filters (preferences) and numeric filters (price).
     */
    fun clearAllFilters() {
        // Clear categorical filters
        _user1Prefs.value = emptySet()
        _user2Prefs.value = emptySet()
        // Clear numeric filters
        _user1MaxPrice.value = null
        _user2MaxPrice.value = null
        // Clear search query
        _searchQuery.value = ""
        // Clear display text
        _user1Display.value = ""
        _user2Display.value = ""
    }

    fun updateSortState(column: SortColumn) {
        // Prevent concurrent sort operations
        if (isSorting) return

        val currentSort = _sortState.value
        val newDirection = if (currentSort.column == column) {
            // Toggle direction if same column is clicked
            if (currentSort.direction == SortDirection.ASCENDING) {
                SortDirection.DESCENDING
            } else {
                SortDirection.ASCENDING
            }
        } else {
            // Default direction for new columns
            when (column) {
                SortColumn.VENDOR_RATING -> SortDirection.DESCENDING // Highest rating first
                SortColumn.MENU_ITEMS -> SortDirection.DESCENDING    // Most items first
                SortColumn.DISTANCE -> SortDirection.ASCENDING       // Closest first
            }
        }
        _sortState.value = SortState(column, newDirection)

        // Sort cached results locally instead of making API call
        applySortToCachedResults()
    }

    private fun applySortToCachedResults() {
        if (cachedAllVendors.isEmpty() || isSorting) return

        isSorting = true

        try {
            // Create a copy to avoid concurrent modification
            val vendorsToSort = cachedAllVendors.toList()

            val sortedVendors = when (_sortState.value.column) {
                SortColumn.VENDOR_RATING -> {
                    if (_sortState.value.direction == SortDirection.ASCENDING) {
                        vendorsToSort.sortedBy { it.querySpecificRatingValue }
                    } else {
                        vendorsToSort.sortedByDescending { it.querySpecificRatingValue }
                    }
                }
                SortColumn.DISTANCE -> {
                    if (_sortState.value.direction == SortDirection.ASCENDING) {
                        vendorsToSort.sortedBy { it.distanceMiles }
                    } else {
                        vendorsToSort.sortedByDescending { it.distanceMiles }
                    }
                }
                SortColumn.MENU_ITEMS -> {
                    if (_sortState.value.direction == SortDirection.ASCENDING) {
                        vendorsToSort.sortedBy { it.combinedRelevantItemCount }
                    } else {
                        vendorsToSort.sortedByDescending { it.combinedRelevantItemCount }
                    }
                }
            }

            // Update cached list with sorted order
            cachedAllVendors = sortedVendors

            // Reset to first page after sort (brings user back to top)
            _pagedVendors.value = sortedVendors.take(pageSize)
        } finally {
            isSorting = false
        }
    }

    fun searchVendors() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Convert preferences to API format
                val (user1ApiPrefs, user2ApiPrefs) = getApiPreferences()

                // Convert sort state to API format
                val (sortBy, sortDirection) = getSortParameters()

                // Reset to first page
                currentPage = 1

                val result = repository.searchVendors(
                    user1Preferences = user1ApiPrefs,
                    user2Preferences = user2ApiPrefs,
                    user1MaxPrice = _user1MaxPrice.value,
                    user2MaxPrice = _user2MaxPrice.value,
                    latitude = _userLocation.value?.latitude,
                    longitude = _userLocation.value?.longitude,
                    searchQuery = _searchQuery.value.ifBlank { null },
                    sortBy = sortBy,
                    sortDirection = sortDirection,
                    page = currentPage,
                    pageSize = pageSize
                )

                result.onSuccess { response ->
                    _totalResultsCount.value = response.pagination.totalResults
                    totalPages = response.pagination.totalPages

                    // Store display text from backend
                    _user1Display.value = response.user1Display
                    _user2Display.value = response.user2Display

                    // Cache full vendor responses
                    cachedVendorResponses = response.vendors

                    // Convert API response to DisplayVendor and cache them
                    val displayVendors = response.vendors.map { it.toDisplayVendor() }
                    cachedAllVendors = displayVendors
                    _pagedVendors.value = displayVendors
                }.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Unknown error occurred"
                    _pagedVendors.value = emptyList()
                    _totalResultsCount.value = 0
                    cachedAllVendors = emptyList()
                    cachedVendorResponses = emptyList()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadNextPage() {
        // Check if we have more cached results to show
        val nextPageStartIndex = _pagedVendors.value.size
        if (nextPageStartIndex < cachedAllVendors.size) {
            // Load next page from cache
            val nextPageEndIndex = minOf(nextPageStartIndex + pageSize, cachedAllVendors.size)
            _pagedVendors.value = cachedAllVendors.subList(0, nextPageEndIndex)
            return
        }

        // Need to fetch more from API
        if (currentPage >= totalPages || _isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true

            try {
                currentPage++

                val (user1ApiPrefs, user2ApiPrefs) = getApiPreferences()
                val (sortBy, sortDirection) = getSortParameters()

                val result = repository.searchVendors(
                    user1Preferences = user1ApiPrefs,
                    user2Preferences = user2ApiPrefs,
                    user1MaxPrice = _user1MaxPrice.value,
                    user2MaxPrice = _user2MaxPrice.value,
                    latitude = _userLocation.value?.latitude,
                    longitude = _userLocation.value?.longitude,
                    searchQuery = _searchQuery.value.ifBlank { null },
                    sortBy = sortBy,
                    sortDirection = sortDirection,
                    page = currentPage,
                    pageSize = pageSize
                )

                result.onSuccess { response ->
                    // Append new vendor responses to cache
                    cachedVendorResponses = cachedVendorResponses + response.vendors

                    // Append new vendors to cache and displayed list
                    val newVendors = response.vendors.map { it.toDisplayVendor() }
                    cachedAllVendors = cachedAllVendors + newVendors
                    _pagedVendors.value = _pagedVendors.value + newVendors
                }.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Error loading next page"
                    currentPage-- // Revert page increment on failure
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Request user's current location.
     * This should be called from the UI after location permissions are granted.
     * Returns the location or null if unavailable.
     */
    suspend fun requestUserLocation(context: Context): UserLocation? {
        val locationService = LocationService(context)

        if (!locationService.hasLocationPermission()) {
            // Permission not granted - UI should handle requesting permission
            return null
        }

        // Use cached location first (fast), fall back to current (slow) if unavailable
        val location = locationService.getLastKnownLocation()
            ?: locationService.getCurrentLocation()

        _userLocation.value = location
        return location
    }

    private fun VendorResponse.toDisplayVendor(): DisplayVendor {
        return DisplayVendor(
            vendorName = this.name,
            user1Count = this.itemCounts.user1Matches,
            user2Count = this.itemCounts.user2Matches,
            distanceMiles = this.distanceMiles ?: 0.0,
            querySpecificRatingString = "${this.rating.upvotes}/${this.rating.totalVotes}",
            querySpecificRatingValue = this.rating.percentage,
            combinedRelevantItemCount = this.itemCounts.totalRelevant
        )
    }

    fun fetchMenuItems(vendorId: Int) {
        viewModelScope.launch {
            _isLoadingItems.value = true
            _errorMessage.value = null

            try {
                val (user1ApiPrefs, user2ApiPrefs) = getApiPreferences()

                val result = repository.getVendorItems(
                    vendorId = vendorId,
                    user1Preferences = user1ApiPrefs,
                    user2Preferences = user2ApiPrefs,
                    user1MaxPrice = _user1MaxPrice.value,
                    user2MaxPrice = _user2MaxPrice.value
                )

                result.onSuccess { items ->
                    _menuItems.value = items
                    _selectedItemIndex.value = 0 // Reset to restaurant header
                }.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to load menu items"
                    _menuItems.value = emptyList()
                }
            } finally {
                _isLoadingItems.value = false
            }
        }
    }

    fun voteOnItem(itemId: Int, voteType: String) {
        viewModelScope.launch {
            try {
                val result = repository.voteOnItem(itemId, voteType)
                result.onSuccess {
                    // Optimistic update: increment the vote count locally
                    val updatedItems = _menuItems.value.map { item ->
                        if (item.id == itemId) {
                            val newUpvotes = if (voteType == "up") item.rating.upvotes + 1 else item.rating.upvotes
                            val newTotalVotes = item.rating.totalVotes + 1
                            val newPercentage = if (newTotalVotes > 0) newUpvotes.toFloat() / newTotalVotes else 0f
                            item.copy(
                                rating = item.rating.copy(
                                    upvotes = newUpvotes,
                                    totalVotes = newTotalVotes,
                                    percentage = newPercentage
                                )
                            )
                        } else {
                            item
                        }
                    }
                    _menuItems.value = updatedItems
                }.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to vote"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to vote"
            }
        }
    }
}
