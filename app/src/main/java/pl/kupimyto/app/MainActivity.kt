package pl.kupimyto.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import pl.kupimyto.app.ui.AppRoot
import pl.kupimyto.app.ui.AppViewModel
import pl.kupimyto.app.ui.theme.KupimytoTheme

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels {
        viewModelFactory {
            initializer {
                val c = (application as KupimytoApp).container
                AppViewModel(c.repository, c.settings)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { KupimytoTheme(vm.themeMode) { AppRoot(vm) } }
    }
}