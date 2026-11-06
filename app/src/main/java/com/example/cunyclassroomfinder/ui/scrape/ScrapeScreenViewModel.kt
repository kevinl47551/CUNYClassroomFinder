package com.example.cunyclassroomfinder.ui.scrape

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cunyclassroomfinder.data.CUNYGlobalSearchRepository
import com.example.cunyclassroomfinder.data.Course
import com.example.cunyclassroomfinder.data.DatabaseRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ScrapeScreenViewModel(
    private val cunyRepo: CUNYGlobalSearchRepository,
    private val databaseRepo: DatabaseRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(ScrapeScreenUiState())
    val uiState = _uiState.asStateFlow()

    private val timeFormatter = DateTimeFormatter.ofPattern("h:mma")

    fun getAvailableCourses() {
        viewModelScope.launch {
            val parsedSubjects = parseSubjects(getAvailableSubjects())

            var allCourses: MutableList<Course> = mutableListOf()

            val response = parsedSubjects.drop(1).map{ subjects ->
                async {
                    cunyRepo.scrapeCourses(
                        subjects.first,
                        subjects.second
                    )
                }
            }.awaitAll()

            //var temp = 0
            for (res in response) {
                if (res.isSuccessful) {
                    allCourses.addAll(parseCourses(res.body() ?: ""))
//                    println(temp)
//                    temp++
                }
            }

            allCourses.retainAll { !(it.room.contains("TBA", true) || it.room.contains("Off Campus", true))}
            allCourses.retainAll { it.days > 0 }

            databaseRepo.clearCourses(_uiState.value.currentSchool.selectedName)
            databaseRepo.insertCoursesWRooms(allCourses)
            println("YES YES YES")


//            for ((res, subject) in response.zip(parsedSubjects)) {
//                println(subject.first)
//                println(subject.second)
//                if (!res.isSuccessful)
//                    println("NOOOOOOOO I FAILED")
//                else println("nice")
//            }
//            println(parsedSubjects.size)
//            println(response.size)

        }
    }

    fun parseCourses(coursesHtml: String): MutableList<Course> {
        val doc: Document = Jsoup.parse(coursesHtml)
        val classHtml = doc.select("td[data-label=Class]")
        val daysAndTimesHtml = doc.select("td[data-label=DaysAndTimes]")
        val roomHtml = doc.select("td[data-label=Room]")
        val courseTopicHtml = doc.select("td[data-label='Course Topic']")

        val courses = mutableListOf<Course>()

        // loops through all courses / sections in one page
        var i = 0
        while(i < classHtml.size) {
            val daysTimesNodes = daysAndTimesHtml[i].textNodes()
            val roomNodes = roomHtml[i].textNodes()

            // loops through if multiple day/times in a section
            for ((dayTimesNode, roomNode) in daysTimesNodes.zip(roomNodes)) {
                val daysTimesString = dayTimesNode.text().trim()

                val days = daysTimesString.substringBefore(" ")
                val timeStart = daysTimesString.substringAfter(" ").substringBefore(" - ")
                val timeEnd = daysTimesString.substringAfter(" - ").substringBefore(" ")

                //format days and times
                var dayBits = 0
                var j = 0
                var timeStartSeconds = 0
                var timeEndSeconds = 0

                // check if contains time
                if (daysTimesString.contains(":") && !days.contains(":")) {
                    // format days
                    while (j < days.length) {
                        when (days.substring(j, j + 2)) {
                            "Mo" -> dayBits = dayBits or (1 shl 0)
                            "Tu" -> dayBits = dayBits or (1 shl 1)
                            "We" -> dayBits = dayBits or (1 shl 2)
                            "Th" -> dayBits = dayBits or (1 shl 3)
                            "Fr" -> dayBits = dayBits or (1 shl 4)
                            "Sa" -> dayBits = dayBits or (1 shl 5)
                            "Su" -> dayBits = dayBits or (1 shl 6)
                        }
                        j += 2
                    }

                    //format time
                    timeStartSeconds = LocalTime.parse(timeStart, timeFormatter).toSecondOfDay()
                    timeEndSeconds = LocalTime.parse(timeEnd, timeFormatter).toSecondOfDay()

                    //add to course list
                    courses.add(Course(
                        _uiState.value.currentSchool.selectedName,
                        classHtml[i].text().toInt(),
                        dayBits,
                        timeStartSeconds,
                        timeEndSeconds,
                        roomNode.text(),
                        courseTopicHtml[i].text()
                    ))
                }
            }

            i += 1
        }

        return courses
    }

    suspend fun getAvailableSubjects(): String {
        var subjectsHtml = ""
        val response = cunyRepo.scrapeSubjects(
            _uiState.value.currentSchool,
            _uiState.value.currentTerm
        )

        if (response.isSuccessful)
            subjectsHtml = response.body().toString()

        return subjectsHtml
    }


    fun parseSubjects(subjectsHtmlPage: String): MutableList<Pair<String, String>> {
        val doc: Document = Jsoup.parse(subjectsHtmlPage)

        val optionsHtml = doc.select("#subject_ld option")
        val parsedOptions = mutableListOf<Pair<String, String>>()

        for(option in optionsHtml) {
            parsedOptions.add(
                Pair(
                    option.text(),
                    option.attr("value")
                )
            )
        }

        return parsedOptions
    }

    suspend fun getAvailableSchoolsAndTerms(): String {
        val response = cunyRepo.scrapeSchoolsAndTerms()

        if (response.isSuccessful) {
            return response.body().toString()
        }
        return ""
    }

    fun parseSchoolsAndSetTerms(firstHtmlPage: String): Unit {
        val doc: Document = Jsoup.parse(firstHtmlPage)

        val schoolSelectHtml = doc.select("input[name=inst_selection]")
        val termSelectHtml = doc.select("select[name=term_value] option").drop(1)
        val parsedSchools = mutableListOf<NameWithValueOption>()
        val parsedTerms = mutableListOf<NameWithValueOption>()

        for (school in schoolSelectHtml) {
            val instSelectionValue = school.attr("value")
            val selectedInstName = doc.select("label[for=${instSelectionValue}]").text() + (" | ")

            parsedSchools.add(NameWithValueOption(
                selectedInstName,
                instSelectionValue
            ))
        }

        for (term in termSelectHtml) {

            parsedTerms.add(NameWithValueOption(
                term.text(),
                term.attr("value")
            ))
        }

        _uiState.update { current ->
            current.copy(
                availableSchools = parsedSchools,
                availableTerms = parsedTerms
            )
        }
    }


    fun setCurrentSchool(school: NameWithValueOption) {
        _uiState.update { current ->
            current.copy(currentSchool = school)
        }
    }

    fun setCurrentTerm(term: NameWithValueOption) {
        _uiState.update { current ->
            current.copy(currentTerm = term)
        }
    }
}