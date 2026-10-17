package com.example.cunyclassroomfinder.ui.room

import com.example.cunyclassroomfinder.data.Course

data class FindRoomUiState(
    val isLoading: Boolean = false,
    val courses: List<Course> = listOf<Course>()
)
