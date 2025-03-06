package com.dertefter.neticlient.data.network

import android.util.Log
import com.dertefter.neticlient.data.model.schedule.Day
import com.dertefter.neticlient.data.model.schedule.Lesson
import com.dertefter.neticlient.data.model.schedule.LessonTrigger
import com.dertefter.neticlient.data.model.schedule.Schedule
import com.dertefter.neticlient.data.model.schedule.Time
import com.dertefter.neticlient.data.model.sessia_schedule.SessiaScheduleItem
import com.dertefter.neticlient.data.model.UserInfo
import com.dertefter.neticlient.data.model.messages.Message
import com.dertefter.neticlient.data.model.messages.MessageDetail
import com.dertefter.neticlient.data.model.money.MoneyItem
import com.dertefter.neticlient.data.model.news.NewsDetail
import com.dertefter.neticlient.data.model.news.NewsItem
import com.dertefter.neticlient.data.model.news.NewsResponse
import com.dertefter.neticlient.data.model.person.Person
import com.dertefter.neticlient.data.model.sessia_results.SessiaResultItem
import com.dertefter.neticlient.data.model.sessia_results.SessiaResultSemestr
import com.dertefter.neticlient.utils.Utils
import okhttp3.ResponseBody
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class HtmlParser {

    fun parseSessiaSchedule(responseBody: ResponseBody?): List<SessiaScheduleItem>?{
        try{
            val output = mutableListOf<SessiaScheduleItem>()
            val pretty = responseBody!!.string()
            val doc = Jsoup.parse(pretty)
            val table = doc.select("div.schedule__session-body").first()
            val items = table!!.select("> *")
            for (i in items){
                var date = i.select("div.schedule__session-day").first()?.text().toString()
                val dayName = Utils.formatDayName(date)
                date = Utils.formatDate(date)
                val time = i.select("div.schedule__session-time").first()?.text().toString()
                val name = i.select("div.schedule__session-item").first()?.ownText().toString()
                val aud = i.select("div.schedule__session-class").first()?.text().toString()
                val type = i.select("div.schedule__session-label").first()?.text().toString()
                val personLink = i.select("div.schedule__session-item").first()?.select("a")?.first()?.attr("href")+"/"
                output.add(SessiaScheduleItem(name, time, date, type, aud, personLink, dayName))
            }
            return output
        }catch (e: Exception) {
            return null
        }

    }

    fun parseUserInfo(input: ResponseBody?): UserInfo?{
        try{
            val pretty = input?.string().toString()
            val doc: Document = Jsoup.parse(pretty)
            val fullName = doc.select("span.fio").text().split(",")[0]
            val group = doc.select("span.fio").text().split(",")[1].replace(" ", "")
            return UserInfo(name = fullName, group = group)
        }catch (e: Exception){
            return null
        }
    }

    fun parseWeekNumberList(responseBody: ResponseBody?): List<Int>? {
        try{
            val output = mutableListOf<Int>()

            val pretty = responseBody?.string().toString()
            val doc: Document = Jsoup.parse(pretty)
            val weeks_content = doc.select("div.schedule__weeks-content")
            val weeks_a = weeks_content.select("a")

            for (it in weeks_a){
                val query = it.attr("data-week")
                val week_item = query.toInt()
                if (query.toInt() > 0){
                    output.add(week_item)
                }
            }

            return output
        }catch (e: Exception){
            return null
        }
    }

    fun parseSchedule(responseBody: ResponseBody?): Schedule? {
        try{
            val pretty = responseBody?.string().toString()
            val doc: Document = Jsoup.parse(pretty)
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
                            val l = Lesson(lessonTitle, type, aud, personIds, trigger, triggerWeeks)
                            lessonItems.add(l)
                            Log.e("lessons", l.toString())

                        }

                    }
                    if (lessonItems.isNotEmpty()){
                        timeItems.add(Time(timeStart.toString(), timeEnd.toString(), lessonItems))
                    }
                }
                dayItems.add(Day(dayName, timeItems))

            }
            return Schedule(dayItems)
        }
        catch (e: Exception){
            return null
        }

    }

    fun parseGroupList(responseBody: ResponseBody?): List<String>? {
        try{
            val outputGroupList = mutableListOf<String>()

            val jsonString = responseBody!!.string()
            val jsonObject = JSONObject(jsonString)
            val items = jsonObject.getString("items")

            val doc: Document = Jsoup.parse(items.replace("\n", "").replace("\t", ""))
            val available_group_items = doc.body().select("a")

            for (it in available_group_items){
                val title = it.text().toString()
                val item = title
                outputGroupList.add(item)
            }
            return outputGroupList
        }
        catch (e: Exception){
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
            Log.e("ResponseParser", "parseCurrentWeekNumber: ${e.stackTraceToString()}")
            return null
        }

    }

    fun parseHeaderLabel(body: ResponseBody?): String? {
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
            Log.e("ResponseParser", "parseCurrentWeekNumber: ${e.stackTraceToString()}")
            return null
        }

    }

    fun parseMessages(body: ResponseBody?, tab: String): List<Message>? {
        try{
            val output = mutableListOf<Message>()
            val pretty = body?.string().toString()
            val doc: Document = Jsoup.parse(pretty)
            val tab_content = doc.select("div#$tab").first()
            val messages = tab_content?.select("div.pad")
            if (messages != null) {
                for (it in messages){
                    var is_new = false
                    if (it.hasClass("new_message_header")){ is_new = true }
                    val send_by = it.select("div.col-2.col-sm-6").first()?.text().toString()
                    val mes_id = it.select("div.col-8.col-sm-6").first()?.attr("onclick").toString()
                        .replace("openWin2('https://ciu.nstu.ru/student_study/mess_teacher/view?id=","")
                        .replace("');return false;","")
                    val mes_text = it.select("div.col-8.col-sm-6").first()?.text().toString()
                    val text = mes_text.split(" -- ")[1]
                    val title = mes_text.split(" -- ")[0]
                    var date = it.select("div.col-1.col-sm-3").first()?.text().toString()
                    val message = Message(mes_id, title, send_by, text, is_new, date)
                    output.add(message)
                }
            }
            return output

        } catch (e: Exception){
            Log.e("ResponseParser", "parseCurrentWeekNumber: ${e.stackTraceToString()}")
            return null
        }
    }

    fun parsePerson(body: ResponseBody?): Person? {
        try{
            val pretty = body?.string().toString()
            val doc: Document = Jsoup.parse(pretty)
            val name = doc.select("div.page-title").first()?.text()!!
            var avatarUrl = doc.select("div.contacts__card-image").first()?.select("img")?.first()?.attr("src")
            if (avatarUrl != null){
                avatarUrl = "https://ciu.nstu.ru/kaf/${avatarUrl}"
            }
            val hasTimetable = doc.html().contains("расписание занятий", true)
            val contacts__card_post = doc.select("div.contacts__card-post").first()?.text()
            val phone = doc.select("div.contacts__card-phone").first()?.text()
            val contacts__card_email = doc.select("div.contacts__card-email").first()?.select("a")?.first()
            var email = ""
            if (contacts__card_email != null){
                val data_email_1 = contacts__card_email.attr("data-email-1")
                val data_email_2 = contacts__card_email.attr("data-email-2")
                email = "$data_email_1@$data_email_2"
            }

            val address = doc.select("div.contacts__card-address").first()?.text()
            Log.e("email", email.toString())
            val profiles_ = doc.select("div.col-9").first()
            profiles_?.select("h3")?.remove()
            val profiles = profiles_?.html()

            val disceplines_ = doc.select("div.about_disc").first()
            disceplines_?.select("h3")?.remove()
            val disceplines = disceplines_?.html()

            return Person(name,avatarUrl, contacts__card_post, phone, email, address, profiles, disceplines, hasTimetable)

        } catch (e: Exception){
            Log.e("ResponseParser", "parseCurrentWeekNumber: ${e.stackTraceToString()}")
            return null
        }
    }

    fun getQueryString(input: String): String? {
        val regex = Regex("\\?(.+)")
        val matchResult = regex.find(input)

        return matchResult?.groups?.get(1)?.value
    }

    fun parseNews(body: ResponseBody?): NewsResponse? {
        return try {
            val json = body?.string() ?: return null
            val jsonObject = JSONObject(json)

            val haveMore = jsonObject.getBoolean("haveMore")
            val nextUrl = jsonObject.getString("nextUrl")

            val itemsArray = jsonObject.getString("items")
            val items = mutableListOf<NewsItem>()
            val doc: Document = Jsoup.parse(itemsArray.replace("\n", "").replace("\t", ""))
            val news_items = doc.body().select("a")

            for (it in news_items) {
                var imageUrl: String? = null
                if (it.attr("style").toString().replace("background-image: url(", "").replace(");", "").replace("//", "/") != ""){
                    imageUrl = "https://www.nstu.ru/" + it.attr("style").toString().replace("background-image: url(", "").replace(");", "").replace("//", "/")
                }
                val title = it.select("div.main-events__item-title").text().toString()
                val tags = it.select("div.main-events__item-tags").text().toString()
                val date = it.select("div.main-events__item-date").text().toString()
                val link = it.attr("href")
                val type = it.select("div.main-events__item-type").text().toString()
                val id = getQueryString(link)!!.replace("idnews=", "")
                val detailUrl  = it.attr("data-type")
                if (detailUrl == "video"){
                    continue
                }

                items.add(NewsItem(id, type, title, tags, date, imageUrl, detailUrl))
            }

            NewsResponse(haveMore, items, nextUrl)
        } catch (e: Exception) {
            Log.e("ResponseParser", "parseNews: ${e.stackTraceToString()}")
            null
        }
    }

    fun parseNewsDetail(body: ResponseBody?): NewsDetail? {
        return try {
            val pretty = body?.string().toString()
            val doc: Document = Jsoup.parse(pretty)

            val title = doc.select("div.page-title").text()

            val news_detail = doc.select("div.news-detail").first()

            val contentHtml = news_detail?.select("div.col-9")?.html()

            val fotorama = doc.select("div.fotorama")
            val baseUrl = "https://www.nstu.ru"

            val imageUrls = mutableListOf<String>()

            val images = fotorama.select("img")
            for (i in images){
                val imageUrl = baseUrl + i.attr("src")
                imageUrls.add(imageUrl)
            }

            NewsDetail(title, contentHtml, imageUrls.toList())

        } catch (e: Exception) {
            Log.e("ResponseParser", "parseNewsDetail: ${e.stackTraceToString()}")
            null
        }
    }

    fun parseMessageDetail(body: ResponseBody?): MessageDetail? {
        return try {
            val pretty = body?.string().toString()
            val doc: Document = Jsoup.parse(pretty)

            val title = doc.select("span.message_theme").text()

            val linkElement = doc.select("a[title=Перейти на личный сайт преподавателя]").first()
            val personLink = linkElement?.attr("href")
            val personId = if (personLink.isNullOrEmpty()){
                null
            } else {
                val pathSegments = personLink.split("/")
                pathSegments.last()
            }

            val contentHtml = doc.select("form").select("span")[6].html()

            MessageDetail(title, contentHtml, personId)

        } catch (e: Exception) {
            Log.e("ResponseParser", "parseMessageDetail: ${e.stackTraceToString()}")
            null
        }
    }

    fun parseSessiaResults(body: ResponseBody?): List<SessiaResultSemestr>? {
        return try {
            val html = body?.string() ?: return null
            val doc = Jsoup.parse(html)
            val semesters = mutableListOf<SessiaResultSemestr>()

            doc.select("h3:containsOwn(Семестр:)").forEach { header ->
                val title = header.text().replace("Семестр:", "").trim()

                var table: Element? = null
                var sibling = header.nextElementSibling()
                while (sibling != null) {
                    if (sibling.`is`("div.row")) {
                        table = sibling.select("table.tdall").first()
                        break
                    }
                    sibling = sibling.nextElementSibling()
                }

                if (table != null) {
                    val items = table.select("tr.last_progress").mapNotNull { row ->
                        val cols = row.select("td")
                        if (cols.size >= 8) {
                            val disciplineHtml = cols[1].html()
                            val firstPart = disciplineHtml.split("<br\\s*/?>".toRegex(RegexOption.IGNORE_CASE)).first()
                            val disciplineText = Jsoup.parse(firstPart).text()
                                .trim().replace("\\s+".toRegex(), " ")

                            // Дата
                            val date = cols[2].text().trim().takeIf { it.isNotEmpty() }

                            // Баллы
                            val score = cols[3].select("span").first()?.text()?.trim()

                            // 5-балльная оценка
                            val scoreFive = cols[4].select("span").first()?.text()?.trim() ?: ""

                            // ECTS
                            val scoreEcts = cols[5].select("span").first()?.text()?.trim() ?: ""

                            // Преподаватели
                            val personPass = cols[6].let { col ->
                                col.select("a").first()?.text()?.trim() ?: col.text().trim()
                            }.takeIf { it.isNotEmpty() }

                            val personTeacher = cols[7].let { col ->
                                col.select("a").first()?.text()?.trim() ?: col.text().trim()
                            }.takeIf { it.isNotEmpty() }

                            SessiaResultItem(
                                title = disciplineText,
                                date = date,
                                score = score,
                                score_five = scoreFive,
                                score_ects = scoreEcts,
                                personWhoPassId = personPass,
                                personWhoTeacherId = personTeacher
                            )
                        } else {
                            null
                        }
                    }

                    semesters.add(SessiaResultSemestr(title, items))
                }
            }

            semesters
        } catch (e: Exception) {
            Log.e("ResponseParser", "Error parsing sessia results: ${e.stackTraceToString()}")
            null
        }
    }

    fun parseMoneyYearList(body: ResponseBody?): List<String>? {
        return try {
            val pretty = body?.string().toString()
            val doc: Document = Jsoup.parse(pretty)
            val years = mutableListOf<String>()
            val select_year = doc.select("select#year").first()

            val option_list = select_year?.select("option")
            if (option_list != null) {
                for (option in option_list){
                    val year = option.attr("value")
                    years.add(year)
                }
            }
            Log.e("years", years.toString())
            years

        } catch (e: Exception) {
            Log.e("ResponseParser", "parseMessageDetail: ${e.stackTraceToString()}")
            null
        }
    }


    fun parseMoneyItems(body: ResponseBody?): List<MoneyItem>? {
        return try {
            val pretty = body?.string().toString()
            val doc: Document = Jsoup.parse(pretty)
            val items = mutableListOf<MoneyItem>()

            val monthsList = listOf(
                "январь", "февраль", "март", "апрель", "май", "июнь",
                "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь"
            )

            doc.select("b").forEach { element ->
                val monthName = element.text().trim()
                if (monthName.lowercase() in monthsList) {
                    var infoText = ""
                    var nextNode = element.nextSibling()

                    while (nextNode != null) {
                        when (nextNode) {
                            is TextNode -> {
                                val text = (nextNode as TextNode).text().trim()
                                if (text.isNotEmpty()) {
                                    infoText = text
                                    break
                                }
                            }
                            is Element -> {
                                val el = nextNode as Element
                                if (el.tagName() == "div" && el.text().isNotBlank()) {
                                    infoText = el.text().trim()
                                    break
                                }
                            }
                        }
                        nextNode = nextNode.nextSibling()
                    }

                    items.add(MoneyItem(monthName, infoText))
                }
            }

            items
        } catch (e: Exception) {
            Log.e("ResponseParser", e.stackTraceToString())
            null
        }
    }


}