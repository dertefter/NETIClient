package com.dertefter.neticore.network.parser

import com.dertefter.neticore.features.documents.model.DocumentOptionItem
import com.dertefter.neticore.features.documents.model.DocumentRequestItem
import com.dertefter.neticore.features.documents.model.DocumentsItem
import com.dertefter.neticore.features.money.model.MoneyItem
import com.dertefter.neticore.features.person_detail.model.Person
import okhttp3.ResponseBody
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

class HtmlParserMoney {

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

            years

        } catch (e: Exception) {

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
            null
        }
    }
}