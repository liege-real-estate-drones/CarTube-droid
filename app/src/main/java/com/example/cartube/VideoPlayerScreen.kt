package com.example.cartube

import android.view.Surface
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.car.app.navigation.model.SurfaceCallback
import androidx.car.app.navigation.model.SurfaceContainer

class VideoPlayerScreen(carContext: CarContext) : Screen(carContext) {

    private val surfaceCallback = object : SurfaceCallback {
        override fun onSurfaceAvailable(surfaceContainer: SurfaceContainer) {
            val surface = surfaceContainer.surface
            CarTubeMediaService.instance?.player?.setVideoSurface(surface)
        }

        override fun onSurfaceDestroyed(surfaceContainer: SurfaceContainer) {
            CarTubeMediaService.instance?.player?.setVideoSurface(null)
        }
    }

    override fun onGetTemplate(): Template {
        return NavigationTemplate.Builder()
            .setSurfaceCallback(surfaceCallback)
            .setActionStrip(
                ActionStrip.Builder()
                    .addAction(Action.BACK)
                    .build()
            )
            .build()
    }
}
