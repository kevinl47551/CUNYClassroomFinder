package com.example.cunyclassroomfinder.data

import com.example.cunyclassroomfinder.network.CUNYApiService
import retrofit2.Response

interface CUNYGlobalSearchRepository {
    suspend fun scrapeSubjects(): Response<String>
    suspend fun scrapeCourses(selectedSubjectName: String, subjectName: String): Response<String>
}

class CUNYGlobalSearchRepositoryRetrofit(
    val cunyApiService: CUNYApiService
): CUNYGlobalSearchRepository {
    override suspend fun scrapeSubjects(): Response<String> = cunyApiService.scrapeSubjects()
    override suspend fun scrapeCourses(selectedSubjectName: String, subjectName: String): Response<String> =
        cunyApiService.scrapeCourses(selectedSubjectName, subjectName)
}