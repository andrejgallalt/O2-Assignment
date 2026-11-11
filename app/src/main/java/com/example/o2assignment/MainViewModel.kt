package com.example.o2assignment

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

enum class CardState { NEW, SCRATCHED, ACTIVATED }
enum class UiState { EMPTY, LOADING, ERROR }

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    private val _state = mutableStateOf(CardState.NEW)
    val state: State<CardState> = _state

    private val _codeState = mutableStateOf("")
    val codeState: State<String> = _codeState

    private val _uiState = mutableStateOf(UiState.EMPTY)
    val uiState: State<UiState> = _uiState

    fun onScratch() {
        if (_state.value == CardState.NEW) {
            viewModelScope.launch {
                _uiState.value = UiState.LOADING
                delay(2000.milliseconds)
                _codeState.value = UUID.randomUUID().toString()
                _uiState.value = UiState.EMPTY
                _state.value = CardState.SCRATCHED
            }
        }
    }

    fun onActivate() {
        if (_state.value == CardState.SCRATCHED) {
            viewModelScope.launch {
                _uiState.value = UiState.LOADING
                try {
                    val response =
                        KtorClient.client.get("https://api.o2.sk/version") {
                            url {
                                parameter("code", _codeState.value)
                            }
                        }
                    val data = response.body<VersionData>()
                    if (data.android > 277028) {
                        _uiState.value = UiState.EMPTY
                        _state.value = CardState.ACTIVATED
                    } else {
                        _uiState.value = UiState.ERROR
                    }
                } catch (_: Exception) {
                    _uiState.value = UiState.ERROR
                }
            }
        }
    }

    fun onReset() {
        _state.value = CardState.NEW
        _codeState.value = ""
        _uiState.value = UiState.EMPTY
    }
}