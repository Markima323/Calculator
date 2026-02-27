package com.stall.calculator.data.repo

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.stall.calculator.data.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "app_settings")

class SettingsRepository(private val context: Context) {
    private object Keys {
        val showCurrency = booleanPreferencesKey("show_currency")
        val confirmBeforeComplete = booleanPreferencesKey("confirm_before_complete")
        val allowFreeProduct = booleanPreferencesKey("allow_free_product")
        val restoreCart = booleanPreferencesKey("restore_cart")
        val showSoldOut = booleanPreferencesKey("show_sold_out")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { pref: Preferences ->
        AppSettings(
            showCurrencySymbol = pref[Keys.showCurrency] ?: true,
            confirmBeforeComplete = pref[Keys.confirmBeforeComplete] ?: false,
            allowFreeProduct = pref[Keys.allowFreeProduct] ?: false,
            restoreCartOnLaunch = pref[Keys.restoreCart] ?: false,
            showSoldOutOnOrderPage = pref[Keys.showSoldOut] ?: false
        )
    }

    suspend fun updateShowCurrency(value: Boolean) {
        context.dataStore.edit { it[Keys.showCurrency] = value }
    }

    suspend fun updateConfirmBeforeComplete(value: Boolean) {
        context.dataStore.edit { it[Keys.confirmBeforeComplete] = value }
    }

    suspend fun updateAllowFreeProduct(value: Boolean) {
        context.dataStore.edit { it[Keys.allowFreeProduct] = value }
    }

    suspend fun updateRestoreCart(value: Boolean) {
        context.dataStore.edit { it[Keys.restoreCart] = value }
    }

    suspend fun updateShowSoldOut(value: Boolean) {
        context.dataStore.edit { it[Keys.showSoldOut] = value }
    }
}
