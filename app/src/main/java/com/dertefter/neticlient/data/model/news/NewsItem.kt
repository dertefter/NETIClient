package com.dertefter.neticlient.data.model.news

data class NewsItem(
    val id: String,
    val type: String,
    val title: String,
    val tags: String,
    val date: String,
    val imageUrl: String?,
    val detailUrl: String
)
