package io.pleo.antaeus.models

enum class InvoiceStatus {
    PENDING,
    PAID,

    // Paused invoice will have to get an action from user
    // i.e. expired cards, insufficient funds etc.
    PAUSED,

    // when the customer does not exist set the invoice to invalid
    // this will require action from ops/admins
    INVALID
}
