package com.example.cunyclassroomfinder.ui.scrape

data class ScrapeScreenUiState(
    val isLoading: Boolean = false,
    val currentCUNY: String = "Hunter College",
    val failedToScrapeSubjects: List<Pair<String, String>> = listOf<Pair<String, String>>(),
    val availableCUNYSchools: List<String> = listOf<String>(),
    val availableTerms: List<String> = listOf<String>()
)