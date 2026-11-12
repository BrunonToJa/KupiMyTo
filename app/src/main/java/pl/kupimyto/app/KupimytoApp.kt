package pl.kupimyto.app

import android.app.Application

class KupimytoApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}