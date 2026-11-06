package com.example.cunyclassroomfinder.ui.room

import com.example.cunyclassroomfinder.data.CunyRoom
import java.time.LocalDate

data class FindRoomUiState(
    val isLoading: Boolean = false,
    // False until the first lookup, so the screen can tell "no results" from "not searched yet"
    val hasSearched: Boolean = false,
    val rooms: List<CunyRoom> = listOf<CunyRoom>(),
    val availableSchools: List<String> = listOf(),
    val currentSchool: String = "",
    // Day bitmask (Mo=1 ... Su=64, same as Course.days); defaults to today
    val selectedDay: Int = 1 shl (LocalDate.now().dayOfWeek.value - 1),
    // Text filter applied to the rooms already found; doesn't hit the database
    val searchQuery: String = ""
) {
    /** [rooms] whose name contains [searchQuery], ignoring case and spaces ("hw7" matches "HW 7"). */
    val filteredRooms: List<CunyRoom>
        get() {
            val query = searchQuery.normalized()
            if (query.isEmpty()) return rooms
            return rooms.filter { it.room.normalized().contains(query, ignoreCase = true) }
        }
}

private val whitespace = Regex("\\s+")

private fun String.normalized() = replace(whitespace, "")
