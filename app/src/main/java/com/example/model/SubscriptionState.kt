package com.example.model

enum class SubscriptionPlan(val title: String, val priceBdt: Int, val durationDesc: String) {
    FREE("Free Tier", 0, "Limited to 720p HD"),
    MONTHLY("Monthly Pro", 25, "30 Days Full 4K Access"),
    LIFETIME("Lifetime Unlimited", 200, "Permanent Unlimited 4K & Priority Speed")
}

data class SubscriptionState(
    val isPro: Boolean = false,
    val plan: SubscriptionPlan = SubscriptionPlan.FREE,
    val statusLabel: String = "Free User",
    val email: String? = null,
    val trxId: String? = null,
    val isSecretRedeemed: Boolean = false,
    val activatedAtMillis: Long = 0L
)
