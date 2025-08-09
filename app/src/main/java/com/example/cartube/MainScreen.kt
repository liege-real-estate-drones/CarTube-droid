package com.example.cartube

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template

class MainScreen(carContext: CarContext) : Screen(carContext) {
    override fun onGetTemplate(): Template {
        val listBuilder = ItemList.Builder()
        CarTubeMediaService.instance?.demo?.values?.forEach { (id, title, _) ->
            listBuilder.addItem(
                Row.Builder()
                    .setTitle(title)
                    .setOnClickListener {
                        CarTubeMediaService.instance?.session?.controller?.transportControls?.playFromMediaId(id, null)
                        screenManager.push(VideoPlayerScreen(carContext))
                    }
                    .build()
            )
        }

        return ListTemplate.Builder()
            .setSingleList(listBuilder.build())
            .setTitle("CarTube")
            .setHeaderAction(androidx.car.app.model.Action.APP_ICON)
            .build()
    }
}
