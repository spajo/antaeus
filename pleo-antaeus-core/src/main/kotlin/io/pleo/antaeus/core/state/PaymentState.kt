package io.pleo.antaeus.core.state

sealed class PaymentState {
    data class Success(val invoiceId: Int) : PaymentState()
    data class InsufficientFunds(val invoiceId: Int) : PaymentState()
    sealed class Failure : PaymentState() {
        data class NetworkFailure(val invoiceId: Int) : Failure()
        data class CurrencyMismatch(val invoiceId: Int, val customerId: Int) : Failure()
        data class CustomerNotFound(val invoiceId: Int, val customerId: Int) : Failure()
    }
}
