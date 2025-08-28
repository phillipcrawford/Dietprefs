package com.example.dietprefs.viewmodel

import com.example.dietprefs.model.SortColumn
import com.example.dietprefs.model.SortState
import com.example.dietprefs.model.SortDirection
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dietprefs.data.AppDatabase
import com.example.dietprefs.data.ItemEntity
import com.example.dietprefs.data.VendorWithItems
import com.example.dietprefs.model.Preference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SharedViewModel : ViewModel() {
    private val _user1Prefs = MutableStateFlow<Set<Preference>>(emptySet())
    val user1Prefs: StateFlow<Set<Preference>> = _user1Prefs.asStateFlow()

    private val _user2Prefs = MutableStateFlow<Set<Preference>>(emptySet())
    val user2Prefs: StateFlow<Set<Preference>> = _user2Prefs.asStateFlow()


    private val _fullDisplayVendors = MutableStateFlow<List<DisplayVendor>>(emptyList())
    private val _pagedVendors = MutableStateFlow<List<DisplayVendor>>(emptyList())
    val pagedVendors: StateFlow<List<DisplayVendor>> = _pagedVendors.asStateFlow()

    private val _totalResultsCount = MutableStateFlow(0)
    val totalResultsCount: StateFlow<Int> = _totalResultsCount.asStateFlow()

    private val _visibleRange = MutableStateFlow(0 to 0)
    val visibleRange: StateFlow<Pair<Int, Int>> = _visibleRange.asStateFlow()

    private var currentPage = 0
    private val pageSize = 10

    // --- SORTING STATE --- (UNCOMMENTED)
    private val _sortState = MutableStateFlow(SortState()) // Default sort from SortType.kt
    val sortState: StateFlow<SortState> = _sortState.asStateFlow()


    // --- LOADING STATE ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun updateVisibleRange(start: Int, end: Int) {
        _visibleRange.value = start to end
    }

    // ... (toggleUser1Pref, toggleUser2Pref, clearPrefs remain the same) ...
    fun toggleUser1Pref(pref: Preference) {
        val currentPrefs = _user1Prefs.value.toMutableSet()
        if (pref in currentPrefs) {
            currentPrefs.remove(pref)
        } else {
            currentPrefs.add(pref)
        }
        _user1Prefs.value = currentPrefs
        // Re-calculate results when preferences change
        // You'll need access to the db instance here, or trigger it from where db is available
        // For now, assuming loadAndComputeResults will be called externally after pref change
    }

    fun toggleUser2Pref(pref: Preference) {
        val currentPrefs = _user2Prefs.value.toMutableSet()
        if (pref in currentPrefs) {
            currentPrefs.remove(pref)
        } else {
            currentPrefs.add(pref)
        }
        _user2Prefs.value = currentPrefs
        // Similar to toggleUser1Pref, results should re-compute
    }

    fun clearPrefs() {
        _user1Prefs.value = emptySet()
        _user2Prefs.value = emptySet()
        // Similar to toggleUser1Pref, results should re-compute
    }


    // --- SORTING FUNCTION --- (UNCOMMENTED)
    fun updateSortState(column: SortColumn) {
        val currentSort = _sortState.value
        val newDirection = if (currentSort.column == column) {
            // Toggle direction if same column is clicked
            if (currentSort.direction == SortDirection.ASCENDING) SortDirection.DESCENDING else SortDirection.ASCENDING
        } else {
            // Default direction for new columns
            when (column) {
                SortColumn.VENDOR_RATING -> SortDirection.DESCENDING // Highest rating first
                SortColumn.MENU_ITEMS -> SortDirection.DESCENDING    // Most items first
                SortColumn.DISTANCE -> SortDirection.ASCENDING       // Closest first
            }
        }
        _sortState.value = SortState(column, newDirection)
        // Re-apply sort and pagination to the existing full list
        applySortAndPagination(_fullDisplayVendors.value)
    }


    fun loadAndComputeResults(db: AppDatabase) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val allVendorsWithItems = db.vendorDao().getVendorsWithItems()
                computeAndProcessResults(allVendorsWithItems)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun computeAndProcessResults(allVendorsWithItems: List<VendorWithItems>) {
        val u1Prefs = _user1Prefs.value
        val u2Prefs = _user2Prefs.value

        val isUser1Active = u1Prefs.isNotEmpty()
        val isUser2Active = u2Prefs.isNotEmpty()

        val processedVendors = allVendorsWithItems.mapNotNull { vendorWithItems ->
            val vendor = vendorWithItems.vendor
            val items = vendorWithItems.items

            val user1MatchingItems = if (isUser1Active) items.filter { item -> matchesPrefs(u1Prefs, item) } else emptyList()
            val user2MatchingItems = if (isUser2Active) items.filter { item -> matchesPrefs(u2Prefs, item) } else emptyList()

            val relevantItemsForQuery = when {
                isUser1Active && isUser2Active -> (user1MatchingItems + user2MatchingItems).distinct()
                isUser1Active -> user1MatchingItems
                isUser2Active -> user2MatchingItems
                else -> items
            }

            if ((isUser1Active || isUser2Active) && relevantItemsForQuery.isEmpty()) {
                null
            } else {
                val sumApplicableUpvotes = relevantItemsForQuery.sumOf { it.upvotes }
                val sumApplicableTotalVotes = relevantItemsForQuery.sumOf { it.totalVotes }

                val querySpecificRatingString: String
                val querySpecificRatingValue: Float

                if (sumApplicableTotalVotes > 0) {
                    querySpecificRatingString = "$sumApplicableUpvotes / $sumApplicableTotalVotes"
                    querySpecificRatingValue = sumApplicableUpvotes.toFloat() / sumApplicableTotalVotes.toFloat()
                } else {
                    querySpecificRatingString = "0 / 0"
                    querySpecificRatingValue = 0f
                }

                val combinedRelevantItemCount = relevantItemsForQuery.size

                DisplayVendor(
                    vendorName = vendor.name,
                    user1Count = user1MatchingItems.size,
                    user2Count = user2MatchingItems.size,
                    distanceMiles = vendor.lat / 1000.0, // REMINDER: Replace with actual distance
                    querySpecificRatingString = querySpecificRatingString,
                    querySpecificRatingValue = querySpecificRatingValue,
                    combinedRelevantItemCount = combinedRelevantItemCount
                )
            }
        }

        // _fullDisplayVendors.value = processedVendors // This will be set in applySortAndPagination
        // _totalResultsCount.value = processedVendors.size // This will be set in applySortAndPagination

        // MODIFIED: Call applySortAndPagination instead of resetAndLoadFirstPage directly
        applySortAndPagination(processedVendors)
    }


    private fun applySortAndPagination(vendors: List<DisplayVendor>) {
        // --- SORTING LOGIC --- (UNCOMMENTED and using querySpecificRatingValue)
        val sortedVendors = when (_sortState.value.column) {
            SortColumn.VENDOR_RATING -> {
                if (_sortState.value.direction == SortDirection.ASCENDING) {
                    vendors.sortedBy { it.querySpecificRatingValue } // Sort by the float value
                } else {
                    vendors.sortedByDescending { it.querySpecificRatingValue } // Sort by the float value
                }
            }
            SortColumn.DISTANCE -> {
                if (_sortState.value.direction == SortDirection.ASCENDING) {
                    vendors.sortedBy { it.distanceMiles }
                } else {
                    vendors.sortedByDescending { it.distanceMiles }
                }
            }
            SortColumn.MENU_ITEMS -> {
                // Sorting by combinedRelevantItemCount
                if (_sortState.value.direction == SortDirection.ASCENDING) {
                    vendors.sortedBy { it.combinedRelevantItemCount }
                } else {
                    vendors.sortedByDescending { it.combinedRelevantItemCount }
                }
            }
        }
        _fullDisplayVendors.value = sortedVendors // Update the full list with the NOW SORTED order
        resetAndLoadFirstPage(sortedVendors) // Then paginate from the sorted list
    }


    private fun resetAndLoadFirstPage(vendors: List<DisplayVendor>) {
        currentPage = 0
        _pagedVendors.value = vendors.take(pageSize)
        _totalResultsCount.value = vendors.size
    }

    fun loadNextPage() {
        // Ensure that we use the _fullDisplayVendors (which is sorted) as the source for next pages
        if ((currentPage + 1) * pageSize < _fullDisplayVendors.value.size) {
            currentPage++
            // It's usually better to rebuild the paged list rather than adding to a mutable list
            // to ensure Compose recomposes correctly with a new list instance.
            _pagedVendors.value = _fullDisplayVendors.value.take((currentPage + 1) * pageSize)
        }
    }

    private fun matchesPrefs(prefs: Set<Preference>, item: ItemEntity): Boolean {
        if (prefs.isEmpty()) return true
        return prefs.all { pref ->
            pref.matcher?.invoke(item) ?: true
        }
    }
}