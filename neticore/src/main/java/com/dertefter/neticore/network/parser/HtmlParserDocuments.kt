package com.dertefter.neticore.network.parser

import com.dertefter.neticore.features.documents.model.DocumentOptionItem
import com.dertefter.neticore.features.documents.model.DocumentRequestItem
import com.dertefter.neticore.features.documents.model.DocumentsItem
import com.dertefter.neticore.features.person_detail.model.Person
import okhttp3.ResponseBody
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class HtmlParserDocuments {

    fun parseDocumentList(body: ResponseBody?): List<DocumentsItem>? {
        return try {
            val html = body?.string() ?: return null
            val doc = Jsoup.parse(html)
            val output = mutableListOf<DocumentsItem>()

            doc.select("table#tutor-messages tbody tr").forEach { row ->
                val cells = row.select("td")
                if (cells.size >= 6) {
                    val type = cells[0].select("a").first()?.text() ?: ""
                    val date = cells[1].text().trim { it <= ' ' }
                    val status = cells[2].text().trim()
                    val person = cells[3].text().trim()
                    val comment = cells[4].select("i").first()?.text()?.trim() ?: ""
                    val number = cells[5].text().trim()

                    output.add(
                        DocumentsItem(
                            type = type.ifEmpty { null },
                            date = date.ifEmpty { null },
                            status = status.ifEmpty { null },
                            person = person.ifEmpty { null },
                            comment = comment.ifEmpty { null },
                            number = number.ifEmpty { null }
                        )
                    )
                }
            }
            output
        } catch (e: Exception) {
            null
        }
    }

    fun parseDocumentOptionsList(body: ResponseBody?): List<DocumentOptionItem>? {
        return try {
            val html = body?.string() ?: return null
            val doc = Jsoup.parse(html)
            val output = mutableListOf<DocumentOptionItem>()

            val options = doc.select("select.types").first()?.select("option")
            if (options != null) {
                for (i in options){
                    output.add(
                        DocumentOptionItem(
                            text = i.text().toString(),
                            value = i.attr("value")
                        )
                    )
                }
            }


            output
        } catch (e: Exception) {
            null
        }
    }

    fun parseDocumentRequest(body: ResponseBody?): DocumentRequestItem? {
        return try {
            val jsonString = body?.string() ?: return null
            val jsonObject = JSONObject(jsonString)

            DocumentRequestItem(
                is_avail = jsonObject.optString("is_avail"),
                need_appl = jsonObject.optString("need_appl"),
                need_pay = jsonObject.optString("need_pay"),
                need_verify = jsonObject.optString("need_verify"),
                text_comm = jsonObject.optString("text_comm"),
                text_doc = jsonObject.optString("text_doc")
            )
        } catch (e: Exception) {
            null
        }
    }
}