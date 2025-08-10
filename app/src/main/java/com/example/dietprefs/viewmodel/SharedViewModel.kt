package com.example.dietprefs.viewmodel

import android.util.Log // Make sure this is imported
import androidx.compose.foundation.layout.size // Not used here, can be removed if only here
import com.example.dietprefs.model.SortColumn
import com.example.dietprefs.model.SortState
import com.example.dietprefs.model.SortDirection
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dietprefs.data.AppDatabase
import com.example.dietprefs.data.ItemEntity
import com.example.dietprefs.data.VendorWithItems
import com.example.dietprefs.model.Preference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// import kotlin.math.roundToInt // Only if you were using the old rating scaling

class SharedViewModel : ViewModel() {

    // Define a consistent TAG for logging
    private val TAG = "ViewModelDebug" // Or "SharedViewModelDebug"

    // --- Preference State ---
    private val _user1Prefs = MutableStateFlow<Set<Preference>>(emptySet())
    val user1Prefs: StateFlow<Set<Preference>> = _user1Prefs.asStateFlow()

    private val _user2Prefs = MutableStateFlow<Set<Preference>>(emptySet())
    val user2Prefs: StateFlow<Set<Preference>> = _user2Prefs.asStateFlow()

    // --- Results Data State ---
    private val _fullDisplayVendors = MutableStateFlow<List<DisplayVendor>>(emptyList())
    // Not directly exposed, pagedVendors is used for UI
    private val _pagedVendors = MutableStateFlow<List<DisplayVendor>>(emptyList())
    val pagedVendors: StateFlow<List<DisplayVendor>> = _pagedVendors.asStateFlow()

    private val _totalResultsCount = MutableStateFlow(0)
    val totalResultsCount: StateFlow<Int> = _totalResultsCount.asStateFlow()

    private val _visibleRange = MutableStateFlow(0 to 0) // Currently seems unused by pagination
    val visibleRange: StateFlow<Pair<Int, Int>> = _visibleRange.asStateFlow()

    private var currentPage = 0
    private val pageSize = 10

    // --- Sorting State ---
    private val _sortState = MutableStateFlow(SortState())
    val sortState: StateFlow<SortState> = _sortState.asStateFlow()

    // --- Loading State ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun updateVisibleRange(start: Int, end: Int) {
        // If you decide to use this for pagination later
        _visibleRange.value = start to end
    }

    // --- Preference Toggling Functions ---
    fun toggleUser1Pref(pref: Preference) {
        Log.d(TAG, "toggleUser1Pref: Toggling '${pref.name}'. Current: ${_user1Prefs.value.map { it.name }}")
        val currentPrefs = _user1Prefs.value.toMutableSet()
        if (pref in currentPrefs) {
            currentPrefs.remove(pref)
        } else {
            currentPrefs.add(pref)
        }
        _user1Prefs.value = currentPrefs
        Log.d(TAG, "toggleUser1Pref: New User1 Prefs: ${_user1Prefs.value.map { it.name }}")
        // NOTE: loadAndComputeResults should be called from where db is available (e.g., UI layer)
        // after a preference toggle.
    }

    fun toggleUser2Pref(pref: Preference) {
        Log.d(TAG, "toggleUser2Pref: Toggling '${pref.name}'. Current: ${_user2Prefs.value.map { it.name }}")
        val currentPrefs = _user2Prefs.value.toMutableSet()
        if (pref in currentPrefs) {
            currentPrefs.remove(pref)
        } else {
            currentPrefs.add(pref)
        }
        _user2Prefs.value = currentPrefs
        Log.d(TAG, "toggleUser2Pref: New User2 Prefs: ${_user2Prefs.value.map { it.name }}")
    }

    fun clearPrefs() {
        Log.d(TAG, "clearPrefs: Clearing all preferences.")
        _user1Prefs.value = emptySet()
        _user2Prefs.value = emptySet()
    }

    // --- Sorting Function ---
    fun updateSortState(column: SortColumn) {
        Log.d(TAG, "updateSortState: Requested column: $column. Current sort: ${_sortState.value}")
        val currentSort = _sortState.value
        val newDirection = if (currentSort.column == column) {
            if (currentSort.direction == SortDirection.ASCENDING) SortDirection.DESCENDING else SortDirection.ASCENDING
        } else {
            when (column) {
                SortColumn.VENDOR_RATING -> SortDirection.DESCENDING
                SortColumn.MENU_ITEMS -> SortDirection.DESCENDING
                SortColumn.DISTANCE -> SortDirection.ASCENDING
            }
        }
        _sortState.value = SortState(column, newDirection)
        Log.d(TAG, "updateSortState: New sort state: ${_sortState.value}")
        // Re-apply sort and pagination to the existing full list
        applySortAndPagination(_fullDisplayVendors.value) // Use the current full list
    }

    // --- CORE DATA LOADING AND PROCESSING ---
    fun loadAndComputeResults(db: AppDatabase) {
        viewModelScope.launch {
            Log.i(TAG, "loadAndComputeResults: STARTING. User1Prefs: ${user1Prefs.value.joinToString { it.name }}, User2Prefs: ${user2Prefs.value.joinToString { it.name }}")
            _isLoading.value = true
            try {
                val allVendorsWithItems = withContext(Dispatchers.IO) {
                    Log.d(TAG, "loadAndComputeResults: Fetching vendors from DB...")
                    val data = db.vendorDao().getVendorsWithItems()
                    Log.d(TAG, "loadAndComputeResults: Fetched ${data.size} vendors from DB.")
                    data
                }

                computeAndProcessResults(allVendorsWithItems)

                // This log might be premature if computeAndProcessResults itself is asynchronous
                // in a way not captured here, but given its structure, it should complete before this.
                Log.i(TAG, "loadAndComputeResults: Successfully launched and passed data to computeAndProcessResults.")

            } catch (e: Exception) {
                Log.e(TAG, "loadAndComputeResults: CRITICAL ERROR during data loading or initial processing", e)
            } finally {
                // This 'finally' might execute before computeAndProcessResults fully finishes if
                // computeAndProcessResults itself spawns further async work without proper joining.
                // However, your current structure seems sequential after fetching.
                // The _isLoading should ideally be set to false at the very end of all processing.
                // Let's keep it here for now and refine if computeAndProcessResults is more complex.
                // For now, the assumption is computeAndProcessResults is blocking within this launch.
                // **Consider moving _isLoading.value = false to the end of computeAndProcessResults if it's the true end.**
                // For now, let's assume this finally block is the effective end of the user-perceived loading operation.
                // We'll see from logs if this is reached prematurely.
                // A better pattern if computeAndProcessResults is complex and might throw:
                // _isLoading.value = false would be inside computeAndProcessResults's own finally block,
                // and computeAndProcessResults would be called directly in the try of this function.
                // For now, let's proceed with this logging. The critical part is if this line is reached.
                Log.i(TAG, "loadAndComputeResults: FINALLY block reached. Setting isLoading to false.")
                _isLoading.value = false
            }
        }
    }

    private fun computeAndProcessResults(allVendorsWithItems: List<VendorWithItems>) {
        val processingStartTime = System.currentTimeMillis()
        Log.d(TAG, "computeAndProcessResults: START - Processing ${allVendorsWithItems.size} vendors.")
        // Local copies of state for consistent processing in this run
        val currentU1Prefs = user1Prefs.value
        val currentU2Prefs = user2Prefs.value
        Log.d(TAG, "computeAndProcessResults: Using U1 Prefs: ${currentU1Prefs.map{it.name}}, U2 Prefs: ${currentU2Prefs.map{it.name}}")

        val isUser1Active = currentU1Prefs.isNotEmpty()
        val isUser2Active = currentU2Prefs.isNotEmpty()

        try { // Add try-catch within this processing unit
            val processedVendors = allVendorsWithItems.mapNotNull { vendorWithItems ->
                Log.v(TAG, "computeAndProcessResults: Processing vendor: '${vendorWithItems.vendor.name}' (ID: ${vendorWithItems.vendor.id})")

                val items = vendorWithItems.items
                Log.v(TAG, "computeAndProcessResults: Vendor '${vendorWithItems.vendor.name}' has ${items.size} items initially.")

                val user1MatchingItems = if (isUser1Active) {
                    Log.v(TAG, "computeAndProcessResults: Vendor '${vendorWithItems.vendor.name}' - Filtering for User 1...")
                    items.filter { item -> matchesPrefs(currentU1Prefs, item, "User1") }
                } else {
                    emptyList()
                }
                Log.v(TAG, "computeAndProcessResults: Vendor '${vendorWithItems.vendor.name}' - User 1 matching items count: ${user1MatchingItems.size}")

                val user2MatchingItems = if (isUser2Active) {
                    Log.v(TAG, "computeAndProcessResults: Vendor '${vendorWithItems.vendor.name}' - Filtering for User 2...")
                    items.filter { item -> matchesPrefs(currentU2Prefs, item, "User2") }
                } else {
                    emptyList()
                }
                Log.v(TAG, "computeAndProcessResults: Vendor '${vendorWithItems.vendor.name}' - User 2 matching items count: ${user2MatchingItems.size}")

                val relevantItemsForQuery = when {
                    isUser1Active && isUser2Active -> {
                        Log.v(TAG, "computeAndProcessResults: Vendor '${vendorWithItems.vendor.name}' - Both users active. Combining and distincting items.")
                        (user1MatchingItems + user2MatchingItems).distinct()
                    }
                    isUser1Active -> user1MatchingItems
                    isUser2Active -> user2MatchingItems
                    else -> {
                        Log.v(TAG, "computeAndProcessResults: Vendor '${vendorWithItems.vendor.name}' - No users active with prefs, using all items for rating.")
                        items // If no prefs, all items are "relevant" for rating context
                    }
                }
                Log.v(TAG, "computeAndProcessResults: Vendor '${vendorWithItems.vendor.name}' - Relevant items for query count: ${relevantItemsForQuery.size}")


                if ((isUser1Active || isUser2Active) && relevantItemsForQuery.isEmpty()) {
                    Log.d(TAG, "computeAndProcessResults: Vendor '${vendorWithItems.vendor.name}' DISCARDED (active prefs but no relevant items).")
                    null
                } else {
                    Log.v(TAG, "computeAndProcessResults: Vendor '${vendorWithItems.vendor.name}' KEPT for DisplayVendor creation.")
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
                    Log.v(TAG, "computeAndProcessResults: Vendor '${vendorWithItems.vendor.name}' - Rating: $querySpecificRatingString ($querySpecificRatingValue)")

                    val combinedRelevantItemCount = relevantItemsForQuery.size

                    DisplayVendor(
                        vendorName = vendorWithItems.vendor.name,
                        user1Count = user1MatchingItems.size,
                        user2Count = user2MatchingItems.size,
                        distanceMiles = vendorWithItems.vendor.lat / 1000.0, // REMINDER: Replace with actual distance calculation
                        querySpecificRatingString = querySpecificRatingString,
                        querySpecificRatingValue = querySpecificRatingValue,
                        combinedRelevantItemCount = combinedRelevantItemCount
                    )
                }
            }

            val processingEndTime = System.currentTimeMillis()
            Log.i(TAG, "computeAndProcessResults: END - Successfully processed ${allVendorsWithItems.size} vendors into ${processedVendors.size} display vendors. Time: ${processingEndTime - processingStartTime}ms")

            applySortAndPagination(processedVendors)

        } catch (e: Exception) {
            Log.e(TAG, "computeAndProcessResults: CRITICAL ERROR during mapping/filtering vendors", e)
            // If an error happens here, the _isLoading in loadAndComputeResults' finally block will still run.
            // If you want to show a specific error state, you'd set another StateFlow here.
            // For now, ensure applySortAndPagination is called with an empty list or doesn't crash.
            applySortAndPagination(emptyList()) // Fallback to empty list on error
        }
        // If computeAndProcessResults is the true end of all background work triggered by loadAndComputeResults,
        // _isLoading.value = false should be here.
        // However, if loadAndComputeResults's finally block is reliable, that's okay too.
        // The most robust would be:
        // loadAndComputeResults { try { ... computeAndProcessResults() ... } finally { _isLoading = false } }
        // computeAndProcessResults { try { ... main logic ... applySortAndPagination() ... } catch { ... log ... applySortAndPagination(emptyList()) /* to prevent crash */ ... } }
    }

    // Modified matchesPrefs to include a user label for clearer logging
    private fun matchesPrefs(prefs: Set<Preference>, item: ItemEntity, userLabel: String): Boolean {
        // Log.v(TAG, "matchesPrefs ($userLabel): Item '${item.name}' (ID: ${item.id}) - Checking against ${prefs.size} prefs: ${prefs.map { it.name }}")
        if (prefs.isEmpty()) { // Should not happen if isUserActive was true, but defensive.
            Log.w(TAG, "matchesPrefs ($userLabel): Item '${item.name}' (ID: ${item.id}) - prefs set is empty, returning true (should be caught by isUserActive).")
            return true
        }
        val result = prefs.all { pref ->
            Log.v(TAG, "matchesPrefs ($userLabel): Item '${item.name}' (ID: ${item.id}), Pref '${pref.name}' - Invoking matcher...")
            val individualMatch = pref.matcher?.invoke(item) ?: run {
                Log.w(TAG, "matchesPrefs ($userLabel): Item '${item.name}' (ID: ${item.id}), Pref '${pref.name}' - Matcher is NULL, defaulting to true.")
                true // For preferences like LOW_PRICE that might not have a direct item matcher
            }
            Log.v(TAG, "matchesPrefs ($userLabel): Item '${item.name}' (ID: ${item.id}), Pref '${pref.name}' - Matcher invoked. Result: $individualMatch")
            if (!individualMatch && pref.name == "LOCALLY_SOURCED") { // Specific log for the problematic case if it fails
                Log.w(TAG, "matchesPrefs ($userLabel): >>> LOCALLY_SOURCED FAILED for Item '${item.name}' (ID: ${item.id}) <<<")
            }
            if (!individualMatch && pref.name == "KOSHER") { // Specific log for the problematic case if it fails
                Log.w(TAG, "matchesPrefs ($userLabel): >>> KOSHER FAILED for Item '${item.name}' (ID: ${item.id}) <<<")
            }
            individualMatch
        }
        Log.d(TAG, "matchesPrefs ($userLabel): Item '${item.name}' (ID: ${item.id}) - Overall match for prefs '${prefs.joinToString { it.name }}': $result")
        return result
    }

    private fun applySortAndPagination(vendors: List<DisplayVendor>) {
        Log.d(TAG, "applySortAndPagination: START - Sorting ${vendors.size} vendors. Current sort state: ${_sortState.value}")
        val sortStartTime = System.currentTimeMillis()

        val sortedVendors = when (_sortState.value.column) {
            SortColumn.VENDOR_RATING -> {
                if (_sortState.value.direction == SortDirection.ASCENDING) {
                    vendors.sortedBy { it.querySpecificRatingValue }
                } else {
                    vendors.sortedByDescending { it.querySpecificRatingValue }
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
                if (_sortState.value.direction == SortDirection.ASCENDING) {
                    vendors.sortedBy { it.combinedRelevantItemCount }
                } else {
                    vendors.sortedByDescending { it.combinedRelevantItemCount }
                }
            }
        }
        val sortEndTime = System.currentTimeMillis()
        Log.d(TAG, "applySortAndPagination: END - Sorting finished. Time: ${sortEndTime - sortStartTime}ms")

        _fullDisplayVendors.value = sortedVendors
        Log.d(TAG, "applySortAndPagination: Updated _fullDisplayVendors with ${sortedVendors.size} vendors.")
        resetAndLoadFirstPage(sortedVendors)
    }

    private fun resetAndLoadFirstPage(vendors: List<DisplayVendor>) {
        Log.d(TAG, "resetAndLoadFirstPage: Resetting pagination with ${vendors.size} total vendors.")
        currentPage = 0 // Reset current page
        _pagedVendors.value = vendors.take(pageSize)
        _totalResultsCount.value = vendors.size
        Log.d(TAG, "resetAndLoadFirstPage: Paged vendors count: ${_pagedVendors.value.size}. Total results: ${_totalResultsCount.value}")
    }

    fun loadNextPage() {
        Log.d(TAG, "loadNextPage: Attempting to load next page. Current page: $currentPage. Total results: ${_fullDisplayVendors.value.size}")
        if ((currentPage + 1) * pageSize < _fullDisplayVendors.value.size) {
            currentPage++
            _pagedVendors.value = _fullDisplayVendors.value.take((currentPage + 1) * pageSize)
            Log.d(TAG, "loadNextPage: Loaded page $currentPage. Paged vendors count: ${_pagedVendors.value.size}")
        } else {
            Log.d(TAG, "loadNextPage: No more pages to load.")
        }
    }
}