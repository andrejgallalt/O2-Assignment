package com.example.o2assignment.ui.card

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.o2assignment.data.KtorClient
import com.example.o2assignment.data.VersionData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class CardViewModel @Inject constructor() : ViewModel() {
    private val _cardState = mutableStateOf(CardState.NEW)
    val state: State<CardState> = _cardState

    private val _codeState = mutableStateOf("")
    val codeState: State<String> = _codeState

    private val _uiState = mutableStateOf(UiState.EMPTY)
    val uiState: State<UiState> = _uiState

    private var scratchJob: Job? = null //for cancellation

    fun onScratch() {
        if (_cardState.value == CardState.NEW) {
            scratchJob?.cancel()
            scratchJob = viewModelScope.launch {
                _uiState.value = UiState.LOADING
                try {
                    delay(2000.milliseconds)
                    _codeState.value = UUID.randomUUID().toString()
                    _cardState.value = CardState.SCRATCHED
                } finally {
                    _uiState.value = UiState.EMPTY
                }
            }
        }
    }

    fun stopScratch() {
        scratchJob?.cancel()
        if (_uiState.value == UiState.LOADING) {
            _uiState.value = UiState.EMPTY
        }
    }

    fun onActivate() {
        if (_cardState.value == CardState.SCRATCHED) {
            viewModelScope.launch {
                _uiState.value = UiState.LOADING
                try {
                    val response = KtorClient.client.get("https://api.o2.sk/version") {
                        url {
                            parameter("code", _codeState.value)
                        }
                    }
                    val data = response.body<VersionData>()
                    if (data.android > 277028) {
                        _uiState.value = UiState.EMPTY
                        _codeState.value += "\n${data.android}"
                        _cardState.value = CardState.ACTIVATED
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
        _cardState.value = CardState.NEW
        _codeState.value = ""
        _uiState.value = UiState.EMPTY
    }

    fun onDismiss() {
        _uiState.value = UiState.EMPTY
    }
}

enum class CardState { NEW, SCRATCHED, ACTIVATED }
enum class UiState { EMPTY, LOADING, ERROR }