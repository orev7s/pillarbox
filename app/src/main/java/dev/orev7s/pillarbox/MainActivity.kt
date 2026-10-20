package dev.orev7s.pillarbox

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import dev.orev7s.pillarbox.ui.HomeScreen
import dev.orev7s.pillarbox.ui.Ink
import dev.orev7s.pillarbox.ui.PickerScreen
import dev.orev7s.pillarbox.ui.PillarBoxTheme

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            PillarBoxTheme {
                App(vm)
            }
        }
    }
}

@Composable
private fun App(vm: MainViewModel) {
    val state by vm.state.collectAsState()
    var picking by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(state.error) {
        val message = state.error
        if (message != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            vm.clearError()
        }
    }

    Box(Modifier.fillMaxSize().background(Ink)) {
        if (picking) {
            PickerScreen(
                apps = state.apps,
                configured = state.rules.map { it.pkg }.toSet(),
                onBack = { picking = false },
                onConfirm = { packages, ratio ->
                    vm.addApps(packages, ratio)
                    picking = false
                }
            )
        } else {
            HomeScreen(
                state = state,
                onEngineToggle = vm::setEngine,
                onAdd = { picking = true },
                onReset = vm::resetDisplay,
                onRatio = vm::setRatio,
                onRemove = vm::removeApp
            )
        }
    }
}
