package com.example.cunyclassroomfinder.ui.scrape

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cunyclassroomfinder.data.CUNYGlobalSearchRepository
import com.example.cunyclassroomfinder.data.Course
import com.example.cunyclassroomfinder.data.DatabaseRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ScrapeScreenViewModel(
    private val cunyRepo: CUNYGlobalSearchRepository,
    private val databaseRepo: DatabaseRepository
): ViewModel() {
    private var availableSubjects = mutableListOf<Pair<String, String>>()
    private var scrapedSubjects = mutableListOf<Pair<String, String>>()
    private var failedToScrapSubjects = mutableListOf<Pair<String, String>>()

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

            allCourses.filter { !(it.room.contains("TBA"))}
            allCourses.filter { it.days > 0 }

            databaseRepo.clearCourses()
            databaseRepo.clearRooms()
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

        val courses = mutableListOf<Course>()

        var i = 0
        while(i < classHtml.size) {
            val daysTimesString = daysAndTimesHtml[i].text()
            println(daysTimesString)
            val days = daysTimesString.substringBefore(" ")
            val timeStart = daysTimesString.substringAfter(" ").substringBefore(" - ")
            val timeEnd = daysTimesString.substringAfter(" - ").substringBefore(" ")

            //format days and times
            var dayBits = 0
            var j = 0
            var timeStartSeconds = 0
            var timeEndSeconds = 0

            // check for tba
            if (daysTimesString.contains(":")) {
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
            }

            println(classHtml[i].text().toInt())
//            println(days)
//            println(dayBits)
//            println(LocalTime.parse(timeStart, timeFormatter).toSecondOfDay())
//            println(LocalTime.parse(timeEnd, timeFormatter).toSecondOfDay())
//            println(roomHtml[i].text())

            //add to course list
            courses.add(Course(
                classHtml[i].text().toInt(),
                dayBits,
                timeStartSeconds,
                timeEndSeconds,
                roomHtml[i].text()
            ))

            i += 1
        }

        return courses
    }

    suspend fun getAvailableSubjects(): String {
        var subjectsHtml = ""
        val response = cunyRepo.scrapeSubjects()

        if (response.isSuccessful)
            subjectsHtml = response.body().toString()

        return subjectsHtml
    }


    fun parseSubjects(firstHtmlPage: String): MutableList<Pair<String, String>> {
        val doc: Document = Jsoup.parse(firstHtmlPage)

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
}