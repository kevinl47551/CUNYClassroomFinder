package com.example.cunyclassroomfinder.data

import com.example.cunyclassroomfinder.network.CUNYApiService
import com.example.cunyclassroomfinder.ui.scrape.NameWithValueOption
import retrofit2.Response

interface CUNYGlobalSearchRepository {
    suspend fun scrapeSubjects(
        school: NameWithValueOption,
        term: NameWithValueOption)
    : Response<String>
    suspend fun scrapeCourses(selectedSubjectName: String, subjectName: String): Response<String>
    suspend fun scrapeSchoolsAndTerms(): Response<String>
}

class CUNYGlobalSearchRepositoryRetrofit(
    val cunyApiService: CUNYApiService
): CUNYGlobalSearchRepository {
    override suspend fun scrapeSubjects(
        school: NameWithValueOption,
        term: NameWithValueOption
    ): Response<String> = cunyApiService.scrapeSubjects(
        school.selectedName,
        school.value,
        term.selectedName,
        term.value
    )
    override suspend fun scrapeCourses(selectedSubjectName: String, subjectName: String): Response<String> =
        cunyApiService.scrapeCourses(selectedSubjectName, subjectName)

    override suspend fun scrapeSchoolsAndTerms(): Response<String> = cunyApiService.scrapeSchoolsAndTerms()
}