package com.example.cunyclassroomfinder.ui.scrape

data class ScrapeScreenUiState(
    val isLoading: Boolean = false,
    val availableSchoolsAndTerms: List<SchoolAndTerm> = listOf(),
    val schoolAndTerm: SchoolAndTerm = SchoolAndTerm(),
    val availableSubjects: List<String> = listOf(),
    val scrapedSubjects: List<String> = listOf(),
    val failedToScrapeSubjects: List<Pair<String, String>> = listOf<Pair<String, String>>(),
)

data class SchoolAndTerm(
    val selectedInstName: String = "Hunter College",
    val instSelection: String = "HTR01",
    val selectedTermName: String = "",
    val termValue: String = "",
)