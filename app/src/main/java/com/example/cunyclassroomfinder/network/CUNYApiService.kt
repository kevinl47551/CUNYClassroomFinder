package com.example.cunyclassroomfinder.network

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface CUNYApiService {
    @FormUrlEncoded
    @POST("CFSearchToolController")
    suspend fun scrapeSubjects(
        @Field("selectedInstName") selectedInstName: String = "Hunter College | ",
        @Field("inst_selection") instSelection: String = "HTR01",
        @Field("selectedTermName") selectedTermName: String = "2026 Fall Term",
        @Field("term_value") termValue: String = "1269",
        @Field("next_btn") nextButton: String = "Next"
    ): Response<String>

    @FormUrlEncoded
    @POST("CFSearchToolController")
    suspend fun scrapeCourses(
        @Field("selectedSubjectName") selectedSubjectName: String,
        @Field("subject_name") subjectName: String,
        @Field("selectedCCareerName") selectedCCareerName: String = "",
        @Field("courseCareer") courseCareer: String = "",
        @Field("selectedCAttrName") selectedCAttrName: String = "",
        @Field("courseAttr") courseAttr: String = "",
        @Field("selectedCAttrVName") selectedCAttrVName: String = "",
        @Field("courseAttValue") courseAttValue: String = "",
        @Field("selectedReqDName") selectedReqDName: String = "",
        @Field("reqDesignation") reqDesignation: String = "",
        @Field("open_class") openClass: String = "O",
        @Field("selectedSessionName") selectedSessionName: String = "",
        @Field("class_session") classSession: String = "",
        @Field("ModeOfIns") modeOfIns: String = "P",
        @Field("selectedModeInsName") selectedModeInsName: String = "In Person, ",
        @Field("meetingStart") meetingStart: String = "LT",
        @Field("selectedMeetingStartName") selectedMeetingStartName: String = "less than",
        @Field("meetingStartText") meetingStartText: String = "",
        @Field("AndMeetingStartText") andMeetingStartText: String = "",
        @Field("meetingEnd") meetingEnd: String = "LE",
        @Field("selectedMeetingEndName") selectedMeetingEndName: String = "less than or equal to",
        @Field("meetingEndText") meetingEndText: String = "",
        @Field("AndMeetingEndText") andMeetingEndText: String = "",
        @Field("daysOfWeek") daysOfWeek: String = "I",
        @Field("selectedDaysOfWeekName") selectedDaysOfWeekName: String = "include only these days",
        @Field("instructor") instructor: String = "B",
        @Field("selectedInstructorName") selectedInstructorName: String = "begins with",
        @Field("instructorName") instructorName: String = "",
        @Field("search_btn_search") searchButton: String = "Search"
    ): Response<String>
}