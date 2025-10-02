package com.example.dietprefs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dietprefs.model.Preference
import com.example.dietprefs.model.SortColumn
import com.example.dietprefs.model.SortDirection
import com.example.dietprefs.model.SortState
import com.example.dietprefs.network.models.VendorResponse
import com.example.dietprefs.repository.VendorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SharedViewModel(
    private val repository: VendorRepository = VendorRepository()
) : ViewModel() {

    // User preference state
    private val _user1Prefs = MutableStateFlow<Set<Preference>>(emptySet())
    val user1Prefs: StateFlow<Set<Preference>> = _user1Prefs.asStateFlow()

    private val _user2Prefs = MutableStateFlow<Set<Preference>>(emptySet())
    val user2Prefs: StateFlow<Set<Preference>> = _user2Prefs.asStateFlow()

    // Vendor results state
    private val _pagedVendors = MutableStateFlow<List<DisplayVendor>>(emptyList())
    val pagedVendors: StateFlow<List<DisplayVendor>> = _pagedVendors.asStateFlow()

    private val _totalResultsCount = MutableStateFlow(0)
    val totalResultsCount: StateFlow<Int> = _totalResultsCount.asStateFlow()

    private val _visibleRange = MutableStateFlow(0 to 0)
    val visibleRange: StateFlow<Pair<Int, Int>> = _visibleRange.asStateFlow()

    // Caching for local sort operations - stores all fetched vendors
    private var cachedAllVendors = listOf<DisplayVendor>()

    // Pagination state
    private var currentPage = 1  // API uses 1-based indexing
    private val pageSize = 10
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

    // User location (can be updated if we add location services)
    private var userLat: Double? = null
    private var userLng: Double? = null

    // Debounce flag to prevent rapid successive sort operations
    private var isSorting = false

    fun updateVisibleRange(start: Int, end: Int) {
        _visibleRange.value = start to end
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

    fun clearPrefs() {
        _user1Prefs.value = emptySet()
        _user2Prefs.value = emptySet()
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
                val user1ApiPrefs = _user1Prefs.value
                    .filter { it.hasApiSupport } // Exclude preferences without API support (e.g., LOW_PRICE)
                    .map { it.apiName }

                val user2ApiPrefs = _user2Prefs.value
                    .filter { it.hasApiSupport }
                    .map { it.apiName }

                // Convert sort state to API format
                val sortBy = when (_sortState.value.column) {
                    SortColumn.VENDOR_RATING -> "rating"
                    SortColumn.DISTANCE -> "distance"
                    SortColumn.MENU_ITEMS -> "item_count"
                }

                val sortDirection = when (_sortState.value.direction) {
                    SortDirection.ASCENDING -> "asc"
                    SortDirection.DESCENDING -> "desc"
                }

                // Reset to first page
                currentPage = 1

                val result = repository.searchVendors(
                    user1Preferences = user1ApiPrefs,
                    user2Preferences = user2ApiPrefs,
                    latitude = userLat,
                    longitude = userLng,
                    sortBy = sortBy,
                    sortDirection = sortDirection,
                    page = currentPage,
                    pageSize = pageSize
                )

                result.onSuccess { response ->
                    _totalResultsCount.value = response.pagination.totalResults
                    totalPages = response.pagination.totalPages

                    // Convert API response to DisplayVendor and cache them
                    val displayVendors = response.vendors.map { it.toDisplayVendor() }
                    cachedAllVendors = displayVendors
                    _pagedVendors.value = displayVendors
                }.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Unknown error occurred"
                    _pagedVendors.value = emptyList()
                    _totalResultsCount.value = 0
                    cachedAllVendors = emptyList()
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

                val user1ApiPrefs = _user1Prefs.value
                    .filter { it.hasApiSupport }
                    .map { it.apiName }

                val user2ApiPrefs = _user2Prefs.value
                    .filter { it.hasApiSupport }
                    .map { it.apiName }

                val sortBy = when (_sortState.value.column) {
                    SortColumn.VENDOR_RATING -> "rating"
                    SortColumn.DISTANCE -> "distance"
                    SortColumn.MENU_ITEMS -> "item_count"
                }

                val sortDirection = when (_sortState.value.direction) {
                    SortDirection.ASCENDING -> "asc"
                    SortDirection.DESCENDING -> "desc"
                }

                val result = repository.searchVendors(
                    user1Preferences = user1ApiPrefs,
                    user2Preferences = user2ApiPrefs,
                    latitude = userLat,
                    longitude = userLng,
                    sortBy = sortBy,
                    sortDirection = sortDirection,
                    page = currentPage,
                    pageSize = pageSize
                )

                result.onSuccess { response ->
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

    private fun VendorResponse.toDisplayVendor(): DisplayVendor {
        return DisplayVendor(
            vendorName = this.name,
            user1Count = this.itemCounts.user1Matches,
            user2Count = this.itemCounts.user2Matches,
            distanceMiles = this.distanceMiles ?: 0.0,
            querySpecificRatingString = "${this.rating.upvotes} / ${this.rating.totalVotes}",
            querySpecificRatingValue = this.rating.percentage,
            combinedRelevantItemCount = this.itemCounts.totalRelevant
        )
    }
}
