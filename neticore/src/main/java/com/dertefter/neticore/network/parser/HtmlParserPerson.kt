package com.dertefter.neticore.network.parser

import com.dertefter.neticore.features.person_detail.model.Person
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class HtmlParserPerson {

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
            val profiles_ = doc.select("div.col-9").first()
            profiles_?.select("h3")?.remove()
            val profiles = profiles_?.html()

            val disceplines_ = doc.select("div.about_disc").first()
            disceplines_?.select("h3")?.remove()
            val disceplines = disceplines_?.html()

            return Person(name,avatarUrl, contacts__card_post, phone, email, address, profiles, disceplines, hasTimetable)

        } catch (e: Exception){

            return null
        }
    }
}