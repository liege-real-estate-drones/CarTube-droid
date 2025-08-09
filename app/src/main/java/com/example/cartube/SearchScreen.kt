package com.example.cartube

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.SearchTemplate
import androidx.car.app.model.Template
import com.example.cartube.data.YouTubeSource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchScreen(carContext: CarContext) : Screen(carContext) {

    private var searchJob: Job? = null
    private var suggestions: List<String> = emptyList()
    private var searchText: String = ""

    private val searchCallback = object : SearchTemplate.SearchCallback {
        override fun onSearchSubmitted(searchText: String) {
            searchJob?.cancel()
            screenManager.push(SearchResultsScreen(carContext, searchText))
        }

        override fun onSearchTextChanged(searchText: String) {
            this@SearchScreen.searchText = searchText
            searchJob?.cancel()
            if (searchText.length < 3) {
                suggestions = emptyList()
                invalidate()
                return
            }
            searchJob = lifecycleScope.launch {
                delay(300) // Debounce
                suggestions = YouTubeSource.getSuggestions(searchText)
                invalidate()
            }
        }
    }

    override fun onGetTemplate(): Template {
        return SearchTemplate.Builder(searchCallback)
            .setSearchHint("Search for a video")
            .setInitialSearchText(searchText)
            .setSuggestions(suggestions)
            .build()
    }
}
