package com.example.cunyclassroomfinder.ui.DownloadClasses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cunyclassroomfinder.data.CUNYGlobalSearchRepository
import com.example.cunyclassroomfinder.data.Course
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class DownloadScreenViewModel(
    private val cunyRepo: CUNYGlobalSearchRepository
): ViewModel() {
    private var availableSubjects = mutableListOf<Pair<String, String>>()

    private val timeFormatter = DateTimeFormatter.ofPattern("h:mma")

    fun getAvailableCourses() {
        viewModelScope.launch {
            val parsedSubjects = parseSubjects(getAvailableSubjects())
            val response = cunyRepo.scrapeCourses(
                parsedSubjects[1].first,
                parsedSubjects[1].second
            )
            if (response.isSuccessful) {
                //println(response.body())
                parseCourses(response.body().toString())
            }
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

            val days = daysTimesString.substringBefore(" ")
            val timeStart = daysTimesString.substringAfter(" ").substringBefore(" - ")
            val timeEnd = daysTimesString.substringAfter(" - ")

            //format days
            var dayBits = 0
            var j = 0
            while(j < days.length) {
                when(days.substring(j, j+2)) {
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

//            println(classHtml[i].text().toInt())
//            println(days)
//            println(dayBits)
//            println(LocalTime.parse(timeStart, timeFormatter).toSecondOfDay())
//            println(LocalTime.parse(timeEnd, timeFormatter).toSecondOfDay())
//            println(roomHtml[i].text())

            //add to course list
            courses.add(Course(
                classHtml[i].text().toInt(),
                dayBits,
                LocalTime.parse(timeStart, timeFormatter).toSecondOfDay(),
                LocalTime.parse(timeEnd, timeFormatter).toSecondOfDay(),
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