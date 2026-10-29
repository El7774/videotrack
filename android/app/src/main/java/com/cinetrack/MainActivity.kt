package com.cinetrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cinetrack.ui.*

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MaterialTheme {
        val vm: AppViewModel = viewModel()
        val token by vm.token.collectAsState()
        var route by remember { mutableStateOf("home") }
        var selType by remember { mutableStateOf("movie") }
        var selId by remember { mutableIntStateOf(0) }
        var selSeason by remember { mutableIntStateOf(1) }

        if (token == null) {
          AuthScreen(vm)
        } else when (route) {
          "home" -> HomeShell(vm, onOpen = { t, id -> selType = t; selId = id; route = "detail" })
          "detail" -> DetailScreen(vm, selType, selId,
            onSeason = { s -> selSeason = s; route = "season" },
            onBack = { route = "home" })
          "season" -> SeasonScreen(vm, selId, selSeason, onBack = { route = "detail" })
        }
      }
    }
  }
}
