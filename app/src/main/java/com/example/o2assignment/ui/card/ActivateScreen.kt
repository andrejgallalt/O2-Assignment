package com.example.o2assignment.ui.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun ActivateScreen(navController: NavController, vm: CardViewModel = hiltViewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        modifier = Modifier.fillMaxSize(), snackbarHost = {
            SnackbarHost(
                snackbarHostState, snackbar = {
                    Snackbar(
                        it, modifier = Modifier.padding(WindowInsets.ime.asPaddingValues())
                    )
                })
        }) { innerPadding ->
        ActivateUI(
            modifier = Modifier.padding(innerPadding),
            snackbarHostState,
            vm.state.value,
            vm.codeState.value,
            vm.uiState.value,
            { vm.onActivate() },
            { navController.popBackStack() },
            { vm.onDismiss() })
    }
}

@Composable
fun ActivateUI(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    state: CardState,
    codeState: String,
    uiState: UiState,
    onActivate: () -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Activate screen", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = "Card status", fontWeight = FontWeight.Bold)
        Text(
            text = if (uiState == UiState.LOADING) "Activating..." else when (state) {
                CardState.NEW -> "Unscratched"
                CardState.SCRATCHED -> "Scratched\n$codeState"
                CardState.ACTIVATED -> "Activated\n$codeState"
            }, textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.weight(1f))
        Row {
            FloatingActionButton(onActivate) {
                Text(text = "Activate")
            }
            Spacer(modifier = Modifier.padding(8.dp))
            FloatingActionButton(onBack) {
                Text(text = "Back")
            }
        }
    }
    if (uiState == UiState.ERROR) {
        LaunchedEffect(snackbarHostState) {
            snackbarHostState.showSnackbar("Error occurred")
            onDismiss()
        }
    }
}