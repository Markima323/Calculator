package com.stall.calculator.data.model

data class AppSettings(
    val showCurrencySymbol: Boolean = true,
    val confirmBeforeComplete: Boolean = false,
    val allowFreeProduct: Boolean = false,
    val restoreCartOnLaunch: Boolean = false,
    val showSoldOutOnOrderPage: Boolean = false
)
