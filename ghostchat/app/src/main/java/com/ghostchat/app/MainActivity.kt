package com.ghostchat.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Density
import java.io.File
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Surface
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ghostchat.app.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val f = File(filesDir, "crash.txt")
        if (f.exists()) {   // mostra l'errore dell'ultimo crash, così si può copiare
            val txt = f.readText(); f.delete()
            setContent {
                MaterialTheme {
                    Surface(Modifier.fillMaxSize()) {
                        Box(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) { SelectionContainer { Text(txt) } }
                    }
                }
            }
            return
        }
        setContent {
            val ctx = LocalContext.current
            val tv = remember { isTelevision(ctx) }
            val d = LocalDensity.current
            // Sulla TV si guarda da lontano: testo più grande del 20%.
            CompositionLocalProvider(
                LocalIsTv provides tv,
                LocalDensity provides Density(d.density, d.fontScale * (if (tv) 1.2f else 1f))
            ) { GhostApp() }
        }
    }
}

@Composable
fun GhostApp(vm: AppViewModel = viewModel(), sv: SessionVM = viewModel()) {
    val st by vm.state.collectAsState()
    val owner = LocalLifecycleOwner.current
    // L'app si blocca ogni volta che esce dallo schermo (tranne durante una chiamata).
    DisposableEffect(owner) {
        val o = LifecycleEventObserver { _, e -> if (e == Lifecycle.Event.ON_STOP && sv.call == null) vm.lock() }
        owner.lifecycle.addObserver(o)
        onDispose { owner.lifecycle.removeObserver(o) }
    }
    GhostTheme(st.theme) {
        CompositionLocalProvider(LocalStrings provides st.strings) {
            val s = st.strings
            val blank = @Composable { Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) }
            when {
                !st.loaded -> blank()
                !Backend.configured -> Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp), Alignment.Center) {
                    Text(s["not_configured"], color = MaterialTheme.colorScheme.onBackground)
                }
                sv.loadFailed -> Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                    Arrangement.Center, Alignment.CenterHorizontally) { Button({ sv.retry() }) { Text(s["retry"]) } }
                sv.authed == null || (sv.authed == true && !sv.profileLoaded) -> blank()
                sv.authed == false || sv.me == null -> OnboardingScreen(sv)
                !st.hasPin -> PinSetupScreen(vm)
                !st.unlocked -> LockScreen(vm, st)
                else -> MainScreen(vm, st, sv)
            }
        }
    }
}
