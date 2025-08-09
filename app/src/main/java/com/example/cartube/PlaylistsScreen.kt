package com.example.cartube

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template

class PlaylistsScreen(carContext: CarContext) : Screen(carContext) {

    // Hardcoded playlists for now
    private val playlists = listOf(
        "Awesome Mix Vol. 1" to "https://www.youtube.com/playlist?list=PLo5B21i1p24z3y42g6h2a0pI3p2T7h4A4",
        "Cyberpunk 2077 OST" to "https://www.youtube.com/playlist?list=PL4h24mnl-o40-t2fAsoP6wU_N7y2e-i9g"
    )

    override fun onGetTemplate(): Template {
        val listBuilder = ItemList.Builder()
        playlists.forEach { (title, url) ->
            listBuilder.addItem(
                Row.Builder()
                    .setTitle(title)
                    .setOnClickListener {
                        screenManager.push(PlaylistItemsScreen(carContext, url, title))
                    }
                    .build()
            )
        }

        return ListTemplate.Builder()
            .setSingleList(listBuilder.build())
            .setTitle("Playlists")
            .setHeaderAction(Action.BACK)
            .build()
    }
}
