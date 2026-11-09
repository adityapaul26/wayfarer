package com.adityapaul.wayfarer.data.search

data class SearchResult(
    val id: Long,
    val name: String,
    val address: String,
    val category: String?,
    val latitude: Double,
    val longitude: Double
)
