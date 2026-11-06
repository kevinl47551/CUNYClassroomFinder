package com.example.cunyclassroomfinder.ui.room

import com.example.cunyclassroomfinder.data.CunyRoom

data class FindRoomUiState(
    val isLoading: Boolean = false,
    val rooms: List<CunyRoom> = listOf<CunyRoom>(),
    val availableSchools: List<String> = listOf(),
    val currentSchool: String = ""
)
