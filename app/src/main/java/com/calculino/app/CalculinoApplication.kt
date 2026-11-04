package com.calculino.app

import android.app.Application

class CalculinoApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
