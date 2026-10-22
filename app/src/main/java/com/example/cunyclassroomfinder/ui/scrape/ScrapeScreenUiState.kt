package com.example.cunyclassroomfinder.ui.scrape

data class ScrapeScreenUiState(
    val isLoading: Boolean = false,
    val availableSchools: List<SchoolOption> = listOf(),
    val availableTerms: List<TermOption> = listOf(),
    val currentSchool: SchoolOption = SchoolOption(),
    val currentTerm: TermOption = TermOption(),
    val availableSubjects: List<String> = listOf(),
    val scrapedSubjects: List<String> = listOf(),
    val failedToScrapeSubjects: List<Pair<String, String>> = listOf<Pair<String, String>>(),
)

data class SchoolOption(
    val selectedInstName: String = "Hunter College",
    val instSelection: String = "HTR01",
)

data class TermOption(
    val selectedTermName: String = "",
    val termValue: String = "",
)