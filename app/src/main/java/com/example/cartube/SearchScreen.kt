package com.example.cartube

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.SearchTemplate
import androidx.car.app.model.Template

class SearchScreen(carContext: CarContext) : Screen(carContext) {

    private val searchCallback = object : SearchTemplate.SearchCallback {
        override fun onSearchSubmitted(searchText: String) {
            screenManager.push(SearchResultsScreen(carContext, searchText))
        }

        override fun onSearchTextChanged(searchText: String) {
            // Not used for now
        }
    }

    override fun onGetTemplate(): Template {
        return SearchTemplate.Builder(searchCallback)
            .setSearchHint("Search for a video")
            .build()
    }
}
