package com.dertefter.neticore.network.parser

import org.jsoup.Jsoup

class HtmlParserAuthorization {

    fun extractFormParams(html: String): Map<String, String> {
        val params = mutableMapOf<String, String>()

        try {
            val doc = Jsoup.parse(html)
            val form = doc.selectFirst("form.login-form")

            form?.attr("action")?.let { actionUrl ->
                val queryString = actionUrl.split("?").getOrNull(1)
                queryString?.split("&")?.forEach { param ->
                    val pair = param.split("=", limit = 2)
                    if (pair.size == 2) {
                        when (pair[0]) {
                            "session_code" -> params["session_code"] = pair[1]
                            "execution" -> params["execution"] = pair[1]
                            "client_id" -> params["client_id"] = pair[1]
                            "tab_id" -> params["tab_id"] = pair[1]
                            "client_data" -> params["client_data"] = pair[1]
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return params
    }

}