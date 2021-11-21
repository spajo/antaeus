/*
    Implements endpoints related to invoices.
 */

package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.external.Telemetry
import io.pleo.antaeus.core.state.InvoiceState
import io.pleo.antaeus.core.state.PaymentState
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus

class InvoiceService(
    private val dal: AntaeusDal,
    private val billingService: BillingService,
    private val telemetry: Telemetry,
) {

    fun charge(invoice: Invoice): InvoiceState {
        return when (val state = billingService.charge(invoice)) {
            is PaymentState.Success -> {
                if (dal.updateInvoiceStatus(state.invoiceId, InvoiceStatus.PAID))
                    InvoiceState.Paid(state.invoiceId)
                // if status update fails, mark as invalid
                // if it fails that means that invoice does not exist
                else markInvalid(state.invoiceId)
            }
            is PaymentState.InsufficientFunds -> {
                dal.updateInvoiceStatus(state.invoiceId, InvoiceStatus.PAUSED)
                InvoiceState.Paused(state.invoiceId, state.customerId)
            }
            is PaymentState.Failure.NetworkFailure -> {
                // just retry
                InvoiceState.Pending(invoice)
            }
            // below states imply bad data in the DB
            // if data is bad mark the invoice as invalid and raise an alert with telemetry
            is PaymentState.Failure.CurrencyMismatch -> {
                // I was thinking about switching the currency of the invoice but I think it's safer
                // to just alert about this, as the exchange rate varies etc.
                telemetry.sendAlert("INVOICE",
                    "Setting Invoice id[${state.invoiceId}] to INVALID, reason: " +
                            "Currency mismatch between invoice and customer id[${state.customerId}]")
                markInvalid(state.invoiceId)
            }
            is PaymentState.Failure.CustomerNotFound -> {
                telemetry.sendAlert("INVOICE",
                    "Setting Invoice id[${state.invoiceId}] to INVALID, reason: " +
                            "Customer id[${state.customerId}] does not exist.")
                markInvalid(state.invoiceId)
            }
        }
    }

    fun fetchAll(): List<Invoice> = dal.fetchInvoices()

    fun fetchAllPending(): List<Invoice> = dal.fetchPendingInvoices()

    @Throws(InvoiceNotFoundException::class)
    fun fetch(id: Int): Invoice = dal.fetchInvoice(id) ?: throw InvoiceNotFoundException(id)

    private fun markInvalid(invoiceId: Int): InvoiceState {
        dal.updateInvoiceStatus(invoiceId, InvoiceStatus.INVALID)
        return InvoiceState.Invalid(invoiceId)
    }
}
