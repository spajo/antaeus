package io.pleo.antaeus.models

data class Customer(
    val id: Int,
    val currency: Currency,
    val subscriptionStatus: SubscriptionStatus,
)
