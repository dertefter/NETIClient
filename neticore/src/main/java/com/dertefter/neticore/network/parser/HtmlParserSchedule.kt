package com.dertefter.neticore.network.parser

import android.util.Log
import com.dertefter.neticore.features.schedule.model.Day
import com.dertefter.neticore.features.schedule.model.Lesson
import com.dertefter.neticore.features.schedule.model.LessonTrigger
import com.dertefter.neticore.features.schedule.model.Schedule
import com.dertefter.neticore.features.schedule.model.Time
import com.dertefter.neticore.features.schedule.model.Week
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class HtmlParserSchedule {

    fun parseSchedule(
        responseBody: ResponseBody?,
        weekNumberList: List<Int>,
        firstDayDate: String,
        symGroup: String
    ): Schedule? {
        try{
            val pretty = responseBody?.string().toString()
            val doc: Document = Jsoup.parse(pretty)

            val title = doc.select("div.schedule__title").first()
            if (title?.text()?.contains(symGroup, true) != true) {return null}

            val schedule__table_body = doc.select("div.schedule__table-body").first()
            val days = schedule__table_body?.select("> *")!!
            val dayItems = mutableListOf<Day>()

            for (day in days){

                val dayName = day.select("div.schedule__table-day").text()
                val timeItems = mutableListOf<Time>()
                val cell = day.select("div.schedule__table-cell")[1]
                val times = cell.select("> *")
                for (time in times){
                    val timeRange = time.select("div.schedule__table-time").text()
                    val formatter = DateTimeFormatter.ofPattern("HH:mm")
                    val times = timeRange.split("-")
                    if (times.size != 2) throw IllegalArgumentException("Неверный формат строки времени")
                    val timeStart = LocalTime.parse(times[0].trim(), formatter)
                    val timeEnd = LocalTime.parse(times[1].trim(), formatter)

                    val lessonItems = mutableListOf<Lesson>()
                    val lessons = time.select("div.schedule__table-cell")[1].select("> *")

                    for (lesson in lessons){
                        lateinit var trigger: LessonTrigger
                        val triggerWeeks = mutableListOf<Int>()
                        val schedule__table_label = lesson.select("span.schedule__table-label").first()
                        if (schedule__table_label == null){
                            trigger = LessonTrigger.ALL
                        }
                        else {
                            val label = schedule__table_label.text()
                            if (label.contains("по чётным")){
                                trigger = LessonTrigger.EVEN
                            } else if (label.contains("по нечётным")){
                                trigger = LessonTrigger.ODD
                            } else if (label.contains("недели")){
                                trigger = LessonTrigger.CUSTOM
                                val triggerWeeksString = label.split(" ")
                                for (i in 1..<triggerWeeksString.size){
                                    triggerWeeks.add(triggerWeeksString[i].toInt())
                                }
                            } else {
                                trigger = LessonTrigger.ALL
                            }
                        }

                        val lessonTitle = lesson.select("div.schedule__table-item").first()!!.ownText().replace("·", "").replace(",", "")
                        val aud = lesson.select("span.schedule__table-class").text()
                        val type = lesson.select("span.schedule__table-typework").text()
                        val person_links_list = lesson.select("a")
                        val personIds = mutableListOf<String>()
                        for (p in person_links_list){
                            val link = p.attr("href")
                            val id = link.substringAfterLast('/')
                            if (id.isNotEmpty()) {
                                personIds.add(id)
                            }
                        }
                        if (lessonTitle.isNotEmpty()){
                            val l = Lesson(lessonTitle, type, aud, personIds, trigger, triggerWeeks,
                                timeStart.toString(), timeEnd.toString(), date = ""
                            )
                            lessonItems.add(l)
                        }

                    }
                    if (lessonItems.isNotEmpty()){
                        timeItems.add(Time(timeStart.toString(), timeEnd.toString(), "", lessonItems))
                    }
                }
                dayItems.add(Day(
                    dayName, timeItems,
                    dayNumber = 0,
                    date = null
                ))

            }

            val yearTitle = doc.select("span.schedule__title-content").first()!!.text()
            val yearPart = yearTitle.split(" ")[0].split("/")

            val year: String = if (yearPart.size == 2){
                val yearSign = if (yearTitle.contains("весенний")) 1 else 0
                yearPart[yearSign]
            } else {
                LocalDate.now().year.toString()
            }

            val combinedDate = "$firstDayDate.$year"
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            val firstDayLocalDate = LocalDate.parse(combinedDate, formatter)

            val weeks = mutableListOf<Week>()

            for (weekNumber in weekNumberList){
                val days = mutableListOf<Day>()
                var day_index = 0
                for (dayItem in dayItems){

                    val date = firstDayLocalDate.plusDays(day_index.toLong()).plusWeeks((weekNumber - 1).toLong()).toString()

                    val times = mutableListOf<Time>()
                    for (time in dayItem.times){
                        val lessons = mutableListOf<Lesson>()
                        for (lesson in time.lessons){
                            if (
                                (lesson.trigger == LessonTrigger.ALL) ||
                                (lesson.trigger == LessonTrigger.ODD && weekNumber % 2 != 0) ||
                                (lesson.trigger == LessonTrigger.EVEN && weekNumber % 2 == 0) ||
                                (lesson.trigger == LessonTrigger.CUSTOM && lesson.triggerWeeks.contains(weekNumber))
                            ){
                                lessons.add(
                                    Lesson(
                                        title = lesson.title,
                                        type = lesson.type,
                                        aud = lesson.aud,
                                        personIds = lesson.personIds.toMutableList(),
                                        trigger = lesson.trigger,
                                        triggerWeeks = lesson.triggerWeeks.toMutableList(),
                                        timeStart = lesson.timeStart,
                                        timeEnd = lesson.timeEnd,
                                        date = date
                                    )
                                )
                            }
                        }

                        val newTime = Time(
                            timeEnd = time.timeEnd,
                            timeStart = time.timeStart,
                            date = date,
                            lessons = lessons
                        )
                        if (lessons.isNotEmpty()) times.add(newTime)
                    }
                    val newDay = Day(
                        dayName = dayItem.dayName,
                        times = times,
                        day_index+1,
                        date = date,
                        weekNumber = weekNumber
                    )
                    days.add(newDay)
                    day_index = day_index + 1
                }

                val week = Week(
                    weekNumber, days
                )
                weeks.add(week)

            }
            Log.e("schchchc", weeks.toString())
            return Schedule(weeks)
        }
        catch (e: Exception){
            Log.e("parseSchedule", e.stackTraceToString())
            return null
        }

    }


    fun parseWeekNumberList(responseBody: ResponseBody?): Pair<String, List<Int>>? {
        try{
            val output = mutableListOf<Int>()

            val pretty = responseBody?.string().toString()
            val doc: Document = Jsoup.parse(pretty)
            val weeks_content = doc.select("div.schedule__weeks-content")
            val weeks_a = weeks_content.select("a")

            val firstDayDateString = doc.select("span.schedule__table-date").first()!!.text()

            for (it in weeks_a){
                val query = it.attr("data-week")
                val week_item = query.toInt()
                if (query.toInt() > 0){
                    output.add(week_item)
                }
            }

            return Pair(firstDayDateString, output)
        }catch (e: Exception){
            return null
        }
    }

    fun parseWeekLabel(body: ResponseBody?): String? {
        try{
            val pretty = body?.string().toString()
            val doc: Document = Jsoup.parse(pretty)
            val header_label = doc.select("div.header__label").first()
            if (header_label != null){
                if (!header_label.text().isNullOrEmpty()){
                    return header_label.text()
                }
            }

            return null
        } catch (e: Exception){

            return null
        }

    }

    fun parseCurrentWeekNumber(body: ResponseBody?): Int? {
        try{
            val pretty = body?.string().toString()
            val doc: Document = Jsoup.parse(pretty)
            val header_label = doc.select("div.header__label").first()
            if (header_label != null){
                val number = Regex("\\d+").find(header_label.text())?.value?.toIntOrNull()
                return number
            }

            return null
        } catch (e: Exception){
            return null
        }

    }
}