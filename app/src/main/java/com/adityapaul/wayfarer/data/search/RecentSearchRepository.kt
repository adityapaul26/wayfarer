package com.adityapaul.wayfarer.data.search

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

interface RecentSearchRepository {
    fun getRecentSearches(): List<SearchResult>
    fun addRecentSearch(searchResult: SearchResult): List<SearchResult>
    fun removeRecentSearch(searchResultId: Long): List<SearchResult>
    fun clearRecentSearches(): List<SearchResult>
}

class SharedPreferencesRecentSearchRepository(
    context: Context,
    private val gson: Gson = Gson()
) : RecentSearchRepository {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getRecentSearches(): List<SearchResult> {
        val json = prefs.getString(KEY_RECENT_SEARCHES, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<SearchResult>>() {}.type
            gson.fromJson<List<SearchResult>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun addRecentSearch(searchResult: SearchResult): List<SearchResult> {
        val current = getRecentSearches().toMutableList()
        current.removeAll { it.id == searchResult.id || (it.name == searchResult.name && it.address == searchResult.address) }
        current.add(0, searchResult)

        val trimmed = current.take(10)
        saveList(trimmed)
        return trimmed
    }

    override fun removeRecentSearch(searchResultId: Long): List<SearchResult> {
        val current = getRecentSearches().toMutableList()
        current.removeAll { it.id == searchResultId }
        saveList(current)
        return current
    }

    override fun clearRecentSearches(): List<SearchResult> {
        prefs.edit().remove(KEY_RECENT_SEARCHES).apply()
        return emptyList()
    }

    private fun saveList(list: List<SearchResult>) {
        val json = gson.toJson(list)
        prefs.edit().putString(KEY_RECENT_SEARCHES, json).apply()
    }

    companion object {
        private const val PREFS_NAME = "wayfarer_recent_searches"
        private const val KEY_RECENT_SEARCHES = "recent_searches_list"
    }
}
