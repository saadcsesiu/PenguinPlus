package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.SubscriptionPlan
import com.example.model.SubscriptionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferences(context: Context) {

    companion object {
        private const val PREFS_NAME = "penguin_plus_preferences"
        private const val KEY_IS_PRO = "key_is_pro"
        private const val KEY_PLAN_TYPE = "key_plan_type"
        private const val KEY_STATUS_LABEL = "key_status_label"
        private const val KEY_USER_EMAIL = "key_user_email"
        private const val KEY_TRX_ID = "key_trx_id"
        private const val KEY_SECRET_REDEEMED = "key_secret_redeemed"
        private const val KEY_ACTIVATED_AT = "key_activated_at"
        private const val KEY_CUSTOM_SCRIPT_URL = "key_custom_script_url"

        const val DEFAULT_APPS_SCRIPT_URL = "https://script.google.com/macros/s/YOUR_SCRIPT_ID_HERE/exec"
        const val BKASH_NUMBER = "01621918393"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _subscriptionState = MutableStateFlow(loadSubscriptionState())
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    private fun loadSubscriptionState(): SubscriptionState {
        val isPro = prefs.getBoolean(KEY_IS_PRO, false)
        val planName = prefs.getString(KEY_PLAN_TYPE, SubscriptionPlan.FREE.name) ?: SubscriptionPlan.FREE.name
        val statusLabel = prefs.getString(KEY_STATUS_LABEL, "Free User") ?: "Free User"
        val email = prefs.getString(KEY_USER_EMAIL, null)
        val trxId = prefs.getString(KEY_TRX_ID, null)
        val isSecret = prefs.getBoolean(KEY_SECRET_REDEEMED, false)
        val activatedAt = prefs.getLong(KEY_ACTIVATED_AT, 0L)

        val plan = try {
            SubscriptionPlan.valueOf(planName)
        } catch (_: Exception) {
            if (isPro) SubscriptionPlan.LIFETIME else SubscriptionPlan.FREE
        }

        return SubscriptionState(
            isPro = isPro,
            plan = plan,
            statusLabel = statusLabel,
            email = email,
            trxId = trxId,
            isSecretRedeemed = isSecret,
            activatedAtMillis = activatedAt
        )
    }

    fun activateLifetimePro(email: String, isSecret: Boolean = true) {
        prefs.edit()
            .putBoolean(KEY_IS_PRO, true)
            .putString(KEY_PLAN_TYPE, SubscriptionPlan.LIFETIME.name)
            .putString(KEY_STATUS_LABEL, "Active Lifetime User")
            .putString(KEY_USER_EMAIL, email)
            .putBoolean(KEY_SECRET_REDEEMED, isSecret)
            .putLong(KEY_ACTIVATED_AT, System.currentTimeMillis())
            .apply()

        _subscriptionState.value = loadSubscriptionState()
    }

    fun activateMonthlyPro(email: String, trxId: String) {
        prefs.edit()
            .putBoolean(KEY_IS_PRO, true)
            .putString(KEY_PLAN_TYPE, SubscriptionPlan.MONTHLY.name)
            .putString(KEY_STATUS_LABEL, "Pro Monthly (30 Days)")
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_TRX_ID, trxId)
            .putBoolean(KEY_SECRET_REDEEMED, false)
            .putLong(KEY_ACTIVATED_AT, System.currentTimeMillis())
            .apply()

        _subscriptionState.value = loadSubscriptionState()
    }

    fun resetToFree() {
        prefs.edit()
            .putBoolean(KEY_IS_PRO, false)
            .putString(KEY_PLAN_TYPE, SubscriptionPlan.FREE.name)
            .putString(KEY_STATUS_LABEL, "Free User")
            .remove(KEY_USER_EMAIL)
            .remove(KEY_TRX_ID)
            .putBoolean(KEY_SECRET_REDEEMED, false)
            .apply()

        _subscriptionState.value = loadSubscriptionState()
    }

    var appsScriptUrl: String
        get() {
            val prefUrl = prefs.getString(KEY_CUSTOM_SCRIPT_URL, null)?.trim()
            if (isValidBackendUrl(prefUrl)) {
                return prefUrl!!
            }
            val envUrl = try {
                com.example.BuildConfig.GOOGLE_APPS_SCRIPT_URL.trim()
            } catch (_: Throwable) {
                null
            }
            if (isValidBackendUrl(envUrl)) {
                return envUrl!!
            }
            return DEFAULT_APPS_SCRIPT_URL
        }
        set(value) = prefs.edit().putString(KEY_CUSTOM_SCRIPT_URL, value).apply()

    fun isBackendConfigured(): Boolean = isValidBackendUrl(appsScriptUrl)

    private fun isValidBackendUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        if (url.contains("YOUR_SCRIPT_ID_HERE") || url.contains("YOUR_DEPLOYED_URL_HERE")) return false
        return url.startsWith("https://")
    }
}
