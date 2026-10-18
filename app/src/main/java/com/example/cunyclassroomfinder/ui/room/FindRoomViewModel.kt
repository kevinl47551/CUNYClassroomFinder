package com.example.cunyclassroomfinder.ui.room

import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

    var day by mutableStateOf(1)
        private set

    fun updateDay(newDay: String) {
        when (newDay) {
            "Monday" -> day = 1
            "Tuesday" -> day = 2
            "Wednesday" -> day = 4
            "Thursday" -> day = 8
            "Friday" -> day = 16
            "Saturday" -> day = 32
            else -> day = 64
        }
    }

    fun getRooms(
        startHour: Int,
        startMin: Int,
        endHour: Int,
        endMin: Int
    ) {
        viewModelScope.launch {
            val startTime = (startHour * 3600) + startMin * 60
            val endTime = (endHour * 3600) + endMin * 60

            val availableRooms = databaseRepo.getAvailableRooms(
                day = day,
                startTime,
                endTime
            )

            _uiState.update { current ->
                current.copy(rooms = availableRooms)
            }
            println(startHour)
            println(endTime)
            println(day)
            println(availableRooms.size)
        }
    }

}