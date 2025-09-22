package com.dertefter.neticore.network.parser

import com.dertefter.neticore.features.control_weeks.model.ControlItem
import com.dertefter.neticore.features.control_weeks.model.ControlResult
import com.dertefter.neticore.features.control_weeks.model.ControlSemestr
import com.dertefter.neticore.features.control_weeks.model.ControlWeek
import com.dertefter.neticore.features.sessia_results.model.SessiaResultItem
import com.dertefter.neticore.features.sessia_results.model.SessiaResultSemestr
import com.dertefter.neticore.features.sessia_results.model.SessiaResults
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class HtmlParserSessiaResults {

    fun parseControlWeeks(body: ResponseBody?): ControlResult? {
        return try {
            val html = body?.string() ?: return null
            val doc = Jsoup.parse(html)

            val rows = doc.select("table.tdall tbody tr:has(td)")

            val grouped = LinkedHashMap<String, LinkedHashMap<String, MutableList<ControlItem>>>()

            rows.forEach { row ->
                val columns = row.select("td")
                if (columns.size >= 5) {
                    val semester = columns[2].text().trim()
                    val week = columns[3].text().trim()
                    val discipline = columns[1].text().trim()
                    val grade = columns[4].text().trim().takeIf { it.isNotEmpty() && it != "&nbsp;" }

                    val semesterMap = grouped.getOrPut(semester) { LinkedHashMap() }
                    val weekList = semesterMap.getOrPut(week) { mutableListOf() }
                    weekList.add(ControlItem(discipline, grade))
                }
            }

            val semesters = grouped.map { (semesterKey, weeksMap) ->
                val weeks = weeksMap.map { (weekKey, items) ->
                    ControlWeek("Неделя $weekKey", items)
                }
                ControlSemestr("Семестр $semesterKey", weeks)
            }

            ControlResult(semesters.ifEmpty { null })
        } catch (e: Exception) {
            null
        }
    }

    fun parseSessiaResults(body: ResponseBody?): SessiaResults? {
        return try {

            val html = body?.string() ?: return null
            val doc = Jsoup.parse(html)
            val semesters = mutableListOf<SessiaResultSemestr>()
            var sum = 0f
            var count = 0

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

                    var sumSem = 0f
                    var countSem = 0

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

                            if (scoreFive.toFloatOrNull() != null){
                                countSem += 1
                                sumSem += scoreFive.toFloat()
                            }

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
                    val srScoreSem = sumSem / countSem

                    sum += srScoreSem
                    count += 1

                    val sem = SessiaResultSemestr(title, items, srScoreSem)
                    semesters.add(sem)

                }
            }

            var srScore: Float? = sum / count

            if (srScore?.isNaN() == true){
                srScore = null
            }

            SessiaResults(
                srScore,
                semesters
            )
        } catch (e: Exception) {
            null
        }
    }


    fun parseShareScore(body: ResponseBody?): String? {
        return try {
            val html = body?.string() ?: return null
            val doc = Jsoup.parse(html)

            val textArea = doc.select("textarea#view_grades").first()?.text()

            textArea
        } catch (e: Exception) {
            null
        }
    }


}