package com.dertefter.neticore.network.parser

import android.util.Log
import com.dertefter.neticore.features.user_detail.model.UserDetail
import com.dertefter.neticore.features.user_detail.model.lks
import okhttp3.ResponseBody
import org.jsoup.Jsoup

class HtmlParserUserDetail {


    fun parseUserDetail(body: ResponseBody?): UserDetail? {
        return try {
            val html = body?.string() ?: return null
            val doc = Jsoup.parse(html)
            val fullName = doc.select("span.fio").text().split(",")[0].split(" ")
            val name = fullName.getOrNull(1)
            val surname = fullName.getOrNull(0)
            val patronymic = fullName.getOrNull(2)
            val group = doc.select("span.fio").text().split(",")[1].replace(" ", "")
            val other_lks_content = doc.select("div#other_lks_content").first()

            val otherGroups = mutableListOf<String>()

            if (other_lks_content != null){
                val other_lks = other_lks_content.select("div.other_lks")
                for (i in other_lks){
                    val b: String? = i.selectFirst("b")?.first()?.ownText()
                    if (!b.isNullOrEmpty()){
                        val group = b.split(", ").lastOrNull()
                        if (!group.isNullOrEmpty()){
                            otherGroups.add(group)
                        }
                    }

                }

            }

            Log.e("othggg", otherGroups.toString())



            UserDetail(
                name = name,
                surname = surname,
                patronymic = patronymic,
                symGroup = group,
                email = doc.selectFirst("input[name=n_email]")?.attr("value").takeIf { it?.isNotEmpty() == true },
                address = doc.selectFirst("input[name=n_address]")?.attr("value").takeIf { it?.isNotEmpty() == true },
                mobilePhoneNumber = doc.selectFirst("input[name=n_phone]")?.attr("value").takeIf { it?.isNotEmpty() == true },
                snils = doc.selectFirst("input[name=n_snils]")?.attr("value").takeIf { it?.isNotEmpty() == true },
                polis = doc.selectFirst("input[name=n_oms]")?.attr("value").takeIf { it?.isNotEmpty() == true },
                vk = doc.selectFirst("input[name=n_vk]")?.attr("value").takeIf { it?.isNotEmpty() == true },
                tg = doc.selectFirst("input[name=n_tg]")?.attr("value").takeIf { it?.isNotEmpty() == true },
                leaderId = doc.selectFirst("input[name=n_leader]")?.attr("value").takeIf { it?.isNotEmpty() == true },
                otherGroups = otherGroups

            )
        } catch (e: Exception) {
            Log.e("HTMLParserUserDetail", e.stackTraceToString())
            null
        }
    }


    fun parseLks(body: ResponseBody?): List<lks>? {
        return try {
            val html = body?.string() ?: return null
            val doc = Jsoup.parse(html)

            val lksList = mutableListOf<lks>()
            val elements = doc.select("div#other_lks_content > div.other_lks")

            for (element in elements) {
                val id = element.select("a[onclick]").first()?.let { a ->
                    val onclick = a.attr("onclick")
                    Regex("selectOtherLks\\((\\d+)\\)").find(onclick)?.groupValues?.get(1)?.toInt()
                }
                val title = element.select("b").first()?.text()?.trim() ?: continue
                val subtitle =
                    element.ownText().takeIf { it.isNotBlank() }?.trim()?.removeSurrounding("(", ")")
                        ?.trim()
                val isSelected = element.select("span:contains(выбран)").isNotEmpty()

                lksList.add(lks(id, title, subtitle, isSelected))
            }
            lksList
        } catch (e: Exception) {
            Log.e("HTMLParserUserDetail", e.stackTraceToString())
            null
        }
    }

}