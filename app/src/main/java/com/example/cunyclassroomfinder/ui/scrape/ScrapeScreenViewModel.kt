package com.example.cunyclassroomfinder.ui.scrape

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cunyclassroomfinder.data.CUNYGlobalSearchRepository
import com.example.cunyclassroomfinder.data.Course
import com.example.cunyclassroomfinder.data.DatabaseRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ScrapeScreenViewModel(
    private val cunyRepo: CUNYGlobalSearchRepository,
    private val databaseRepo: DatabaseRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(ScrapeScreenUiState())
    val uiState = _uiState.asStateFlow()

    private val timeFormatter = DateTimeFormatter.ofPattern("h:mma")

    /**
     * Runs [block] as the single network job for this screen. Does nothing if a job is
     * already running, so repeated taps can't start overlapping requests. Any failure is
     * reported through [ScrapeScreenUiState.errorMessage].
     */
    private fun launchNetworkJob(loadingMessage: String, block: suspend CoroutineScope.() -> Unit) {
        if (_uiState.value.isLoading) return
        // Set synchronously (before launch) so a second call right after this one is rejected
        _uiState.update {
            it.copy(isLoading = true, loadingMessage = loadingMessage, errorMessage = null)
        }
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.toUserMessage()) }
            } finally {
                _uiState.update { it.copy(isLoading = false, loadingMessage = "") }
            }
        }
    }

    private fun Exception.toUserMessage(): String = when (this) {
        is IOException -> "Network error, check your connection and try again. (${message ?: javaClass.simpleName})"
        else -> message ?: "Something went wrong (${javaClass.simpleName})"
    }

    fun loadSchoolsAndTerms() {
        if (_uiState.value.availableSchools.isNotEmpty()) return
        launchNetworkJob("Loading schools and terms...") {
            parseSchoolsAndSetTerms(getAvailableSchoolsAndTerms())
            if (_uiState.value.availableSchools.isEmpty() || _uiState.value.availableTerms.isEmpty())
                error("Couldn't find any schools or terms on the CUNY Global Search page.")
        }
    }

    fun getAvailableCourses() {
        // Capture the selection up front so changing it mid-scrape can't mix schools
        val school = _uiState.value.currentSchool
        val term = _uiState.value.currentTerm
        if (school.value.isEmpty() || term.value.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Select a school and a term first.") }
            return
        }

        launchNetworkJob("Scraping ${school.selectedName.removeSuffix(" | ")} courses...") {
            _uiState.update {
                it.copy(scrapedSubjects = listOf(), failedToScrapeSubjects = listOf())
            }

            // First option is the "select a subject" placeholder
            val subjects = parseSubjects(getAvailableSubjects(school, term)).drop(1)
            if (subjects.isEmpty()) error("No subjects found for this school and term.")

            // Each subject is caught on its own so one failure doesn't cancel the rest
            val results = subjects.map { subject ->
                async {
                    try {
                        val res = cunyRepo.scrapeCourses(subject.first, subject.second)
                        if (res.isSuccessful) parseCourses(res.body().orEmpty(), school.selectedName)
                        else null
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        null
                    }
                }
            }.awaitAll()

            val allCourses = mutableListOf<Course>()
            val scraped = mutableListOf<String>()
            val failed = mutableListOf<Pair<String, String>>()
            for ((subject, courses) in subjects.zip(results)) {
                if (courses == null) failed.add(subject)
                else {
                    scraped.add(subject.first)
                    allCourses.addAll(courses)
                }
            }

            _uiState.update {
                it.copy(scrapedSubjects = scraped, failedToScrapeSubjects = failed)
            }

            // Keep the existing data if nothing came back
            if (scraped.isEmpty()) error("Every subject failed to scrape, so the saved data was left unchanged.")

            allCourses.retainAll { !(it.room.contains("TBA", true) || it.room.contains("Off Campus", true))}
            allCourses.retainAll { it.days > 0 }

            databaseRepo.clearCourses(school.selectedName)
            databaseRepo.insertCoursesWRooms(allCourses)
        }
    }

    fun parseCourses(coursesHtml: String, school: String): MutableList<Course> {
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
                        school,
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

    private suspend fun getAvailableSubjects(
        school: NameWithValueOption,
        term: NameWithValueOption
    ): String {
        val response = cunyRepo.scrapeSubjects(school, term)

        if (!response.isSuccessful)
            error("Couldn't load subjects (HTTP ${response.code()}).")

        return response.body().orEmpty()
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

    private suspend fun getAvailableSchoolsAndTerms(): String {
        val response = cunyRepo.scrapeSchoolsAndTerms()

        if (!response.isSuccessful)
            error("Couldn't load schools and terms (HTTP ${response.code()}).")

        return response.body().orEmpty()
    }

    private fun parseSchoolsAndSetTerms(firstHtmlPage: String): Unit {
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


    // Selection is locked while a job runs, since the server session tracks the current school/term
    fun setCurrentSchool(school: NameWithValueOption) {
        if (_uiState.value.isLoading) return
        _uiState.update { current ->
            current.copy(currentSchool = school)
        }
    }

    fun setCurrentTerm(term: NameWithValueOption) {
        if (_uiState.value.isLoading) return
        _uiState.update { current ->
            current.copy(currentTerm = term)
        }
    }
}