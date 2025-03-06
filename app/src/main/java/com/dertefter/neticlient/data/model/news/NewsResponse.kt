package com.dertefter.neticlient.data.model.news

data class NewsResponse(
    val haveMore: Boolean,
    val items: List<NewsItem>,
    val nextUrl: String
)
