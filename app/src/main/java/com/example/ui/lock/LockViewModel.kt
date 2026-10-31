package com.example.ui.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.AppPreferences
import com.example.security.SecurityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class LockUiState(
    val isLocked: Boolean = false,
    val enteredPin: String = "",
    val errorMessage: String? = null,
    val showPinDigits: Boolean = false
)

class LockViewModel(
    private val preferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(LockUiState())
    val uiState: StateFlow<LockUiState> = _uiState.asStateFlow()

    init {
        checkInitialLockState()
    }

    fun checkInitialLockState() {
        viewModelScope.launch {
            val enabled = preferences.isPinEnabled.first()
            if (enabled) {
                _uiState.value = _uiState.value.copy(isLocked = true, enteredPin = "", errorMessage = null)
            } else {
                _uiState.value = _uiState.value.copy(isLocked = false)
            }
        }
    }

    fun onAppResume() {
        viewModelScope.launch {
            val enabled = preferences.isPinEnabled.first()
            if (!enabled) return@launch

            val lastBg = preferences.lastBackgroundTime.first()
            val timeout = preferences.autoLockTimeout.first()
            val now = System.currentTimeMillis()

            if (timeout == 0L || (now - lastBg) >= timeout) {
                _uiState.value = _uiState.value.copy(
                    isLocked = true,
                    enteredPin = "",
                    errorMessage = null
                )
            }
        }
    }

    fun onAppPause() {
        viewModelScope.launch {
            preferences.setLastBackgroundTime(System.currentTimeMillis())
        }
    }

    fun onDigitEntered(digit: Char) {
        if (_uiState.value.enteredPin.length < 6) {
            val newPin = _uiState.value.enteredPin + digit
            _uiState.value = _uiState.value.copy(enteredPin = newPin, errorMessage = null)
            if (newPin.length in 4..6) {
                // Auto-attempt unlock if 4 or 6 digits
                attemptUnlock(newPin)
            }
        }
    }

    fun onBackspace() {
        if (_uiState.value.enteredPin.isNotEmpty()) {
            val newPin = _uiState.value.enteredPin.dropLast(1)
            _uiState.value = _uiState.value.copy(enteredPin = newPin, errorMessage = null)
        }
    }

    fun onClear() {
        _uiState.value = _uiState.value.copy(enteredPin = "", errorMessage = null)
    }

    fun toggleShowPin() {
        _uiState.value = _uiState.value.copy(showPinDigits = !_uiState.value.showPinDigits)
    }

    private fun attemptUnlock(pin: String) {
        viewModelScope.launch {
            val isValid = SecurityManager.verifyPin(pin, preferences)
            if (isValid) {
                _uiState.value = _uiState.value.copy(
                    isLocked = false,
                    enteredPin = "",
                    errorMessage = null
                )
            } else {
                if (pin.length >= 6) {
                    _uiState.value = _uiState.value.copy(
                        enteredPin = "",
                        errorMessage = "Incorrect PIN. Please try again."
                    )
                }
            }
        }
    }

    fun verifyExplicit() {
        val pin = _uiState.value.enteredPin
        if (pin.length < 4) {
            _uiState.value = _uiState.value.copy(errorMessage = "PIN must be 4–6 digits")
            return
        }
        viewModelScope.launch {
            val isValid = SecurityManager.verifyPin(pin, preferences)
            if (isValid) {
                _uiState.value = _uiState.value.copy(
                    isLocked = false,
                    enteredPin = "",
                    errorMessage = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    enteredPin = "",
                    errorMessage = "Incorrect PIN. Please try again."
                )
            }
        }
    }

    class Factory(
        private val preferences: AppPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LockViewModel(preferences) as T
        }
    }
}
