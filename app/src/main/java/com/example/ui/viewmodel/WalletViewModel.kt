package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.model.WalletAsset
import com.example.data.repository.TradingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WalletViewModel(
    private val tradingRepository: TradingRepository
) : ViewModel() {

    val totalBalanceUsdt: StateFlow<Double> = tradingRepository.totalBalanceUsdt
    val availableMarginUsdt: StateFlow<Double> = tradingRepository.availableMarginUsdt
    val walletAssets: StateFlow<List<WalletAsset>> = tradingRepository.walletAssets

    private val _isPrivacyHidden = MutableStateFlow(false)
    val isPrivacyHidden: StateFlow<Boolean> = _isPrivacyHidden.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun togglePrivacyHidden() {
        _isPrivacyHidden.value = !_isPrivacyHidden.value
    }

    fun triggerQuickAction(actionName: String) {
        _toastMessage.value = "$actionName feature initiated."
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
