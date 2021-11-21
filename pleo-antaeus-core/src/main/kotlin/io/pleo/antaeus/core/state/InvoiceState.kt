package io.pleo.antaeus.core.state

import io.pleo.antaeus.models.Invoice

sealed class InvoiceState {
    data class Paid(val invoiceId: Int) : InvoiceState()
    data class Paused(val invoiceId: Int, val customerId: Int) : InvoiceState()
    data class Pending(val invoice: Invoice) : InvoiceState()
    data class Invalid(val invoiceId: Int) : InvoiceState()
}
