package com.example.cunyclassroomfinder.ui.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cunyclassroomfinder.data.DatabaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FindRoomViewModel(
    private val databaseRepo: DatabaseRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(FindRoomUiState())
    val uiState: StateFlow<FindRoomUiState> = _uiState.asStateFlow()

    init {
        loadSchools()
    }

    // Schools come from the rooms table, so only schools that have been scraped appear.
    fun loadSchools() {
        viewModelScope.launch {
            val schools = databaseRepo.getSchools()
            _uiState.update { current ->
                current.copy(
                    availableSchools = schools,
                    currentSchool = current.currentSchool
                        .takeIf { it in schools }
                        ?: schools.firstOrNull().orEmpty()
                )
            }
        }
    }

    fun setCurrentSchool(school: String) {
        _uiState.update { current ->
            current.copy(currentSchool = school, rooms = listOf(), hasSearched = false)
        }
    }

    /** [dayBit] uses the same bitmask as Course.days (Mo=1, Tu=2, ... Su=64). */
    fun updateDay(dayBit: Int) {
        _uiState.update { it.copy(selectedDay = dayBit) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun getRooms(
        startHour: Int,
        startMin: Int,
        endHour: Int,
        endMin: Int
    ) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val state = _uiState.value
                val availableRooms = databaseRepo.getAvailableRooms(
                    school = state.currentSchool,
                    day = state.selectedDay,
                    timeStart = (startHour * 3600) + startMin * 60,
                    timeEnd = (endHour * 3600) + endMin * 60
                )

                _uiState.update { current ->
                    current.copy(rooms = availableRooms, hasSearched = true)
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
