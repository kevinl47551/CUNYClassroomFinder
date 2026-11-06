package com.example.cunyclassroomfinder.ui.scrape

data class ScrapeScreenUiState(
    val isLoading: Boolean = false,
    // What the current network job is doing, shown next to the progress indicator
    val loadingMessage: String = "",
    // Non-null when the last network job failed
    val errorMessage: String? = null,
    val availableSchools: List<NameWithValueOption> = listOf(),
    val availableTerms: List<NameWithValueOption> = listOf(),
    val currentSchool: NameWithValueOption = NameWithValueOption(),
    val currentTerm: NameWithValueOption = NameWithValueOption(),
    val availableSubjects: List<String> = listOf(),
    val scrapedSubjects: List<String> = listOf(),
    val failedToScrapeSubjects: List<Pair<String, String>> = listOf<Pair<String, String>>(),
)

data class NameWithValueOption(
    val selectedName: String = "",
    val value: String = "",
)