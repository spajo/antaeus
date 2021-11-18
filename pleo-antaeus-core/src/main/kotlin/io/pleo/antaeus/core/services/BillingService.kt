package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider

class BillingService(
    private val paymentProvider: PaymentProvider
) {
    // TODO - Add code e.g. here
    // EX to handle
    // `CustomerNotFoundException`: when no customer has the given id.
    // `CurrencyMismatchException`: when the currency does not match the customer account.
    // `NetworkException`: when a network error happens.
}
