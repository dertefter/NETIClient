package com.dertefter.neticlient.data.model.news

data class NewsDetail(
    val title: String,
    val contentHtml: String?,
    val imageUrls: List<String>,
)
