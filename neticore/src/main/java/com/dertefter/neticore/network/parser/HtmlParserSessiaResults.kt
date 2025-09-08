package com.dertefter.neticore.network.parser

import com.dertefter.neticore.features.sessia_results.model.SessiaResultItem
import com.dertefter.neticore.features.sessia_results.model.SessiaResultSemestr
import com.dertefter.neticore.features.sessia_results.model.SessiaResults
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class HtmlParserSessiaResults {


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

}