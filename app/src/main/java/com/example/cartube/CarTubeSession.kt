package com.example.cartube

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.CarContext

class CarTubeSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        return MainScreen(carContext)
    }
}
