package com.example.o2assignment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.o2assignment.ui.theme.O2AssignmentTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            O2AssignmentTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = {
                        SnackbarHost(
                            snackbarHostState, snackbar = {
                                Snackbar(
                                    it,
                                    modifier = Modifier.padding(WindowInsets.ime.asPaddingValues())
                                )
                            })
                    }) { innerPadding ->
                    val vm: MainViewModel = hiltViewModel()
                    Greeting(
                        modifier = Modifier.padding(innerPadding),
                        snackbarHostState,
                        vm.state.value,
                        vm.codeState.value,
                        vm.uiState.value,
                        { vm.onScratch() },
                        { vm.onActivate() },
                        { vm.onReset() })
                }
            }
        }
    }
}

@Composable
fun Greeting(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    state: CardState,
    codeState: String,
    uiState: UiState,
    onScratch: () -> Unit,
    onActivate: () -> Unit,
    onReset: () -> Unit
) {
    Box(modifier = modifier) {
        Column {
            Text(
                text = when (state) {
                    CardState.NEW -> "Unscratched"
                    CardState.SCRATCHED -> "Scratched\n$codeState"
                    CardState.ACTIVATED -> "Activated"
                }
            )
            Row {
                if (state == CardState.NEW) {
                    if (uiState == UiState.LOADING) {
                        Text(text = "Scratching")
                    } else {
                        FloatingActionButton(onScratch) {
                            Text(text = "Scratch")
                        }
                    }
                }
                if (state == CardState.SCRATCHED) {
                    if (uiState == UiState.LOADING) {
                        Text(text = "Activating")
                    } else {
                        FloatingActionButton(onActivate) {
                            Text(text = "Activate")
                        }
                    }
                }
                if (state == CardState.ACTIVATED) {
                    FloatingActionButton(onReset) {
                        Text(text = "Reset")
                    }
                }
            }
        }
    }
    if (uiState == UiState.ERROR) {
        LaunchedEffect(snackbarHostState) {
            snackbarHostState.showSnackbar("Error occurred")
            onReset()
        }
    }
}
