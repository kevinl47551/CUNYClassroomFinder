package com.example.cunyclassroomfinder.ui.room

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.cunyclassroomfinder.data.DatabaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FindRoomViewModel(
    private val databaseRepo: DatabaseRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(FindRoomUiState())
    val uiState: StateFlow<FindRoomUiState> = _uiState.asStateFlow()

    var day by mutableStateOf(0)
        private set

    var timeStart by mutableStateOf(0)
        private set

    var timeEnd by mutableStateOf(0)
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


}