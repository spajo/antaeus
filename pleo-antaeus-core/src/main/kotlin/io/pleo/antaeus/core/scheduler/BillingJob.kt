package io.pleo.antaeus.core.scheduler

import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.core.state.InvoiceState
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class BillingJob(
    private val invoiceService: InvoiceService,
    private val customerService: CustomerService,
) : AntaeusJob(invoiceService, customerService) {

    override fun startJob(): Result = invoiceService.fetchAllPending()
        .also { logger.info { "Processing ${it.size} pending invoices..." } }
        .asSequence()
        .map { invoiceService.charge(it) }
        .map { handleState(it) }
        .groupBy { it::class }
        .toList()
        .fold(Result()) { acc, (klass, states) ->
            acc.apply {
                when (klass) {
                    InvoiceState.Invalid::class -> invalidInvoicesCount = states.size
                    InvoiceState.Paid::class -> paymentSuccessCount = states.size
                    InvoiceState.Paused::class -> pausedInvoicesCount = states.size
                    else -> logger.warn { "Invalid state of the invoice: $klass" }
                }
            }
        }

    private fun handleState(state: InvoiceState): InvoiceState = when (state) {
        is InvoiceState.Paused -> {
            customerService
                .pauseSubscription(state.customerId)
            state
        }
        is InvoiceState.Pending -> {
            // try again, I don't like recursion this will have to change probs
            // TODO: add some backoff? or maybe schedule new job for still pending?
            handleState(invoiceService.charge(state.invoice))
        }
        else -> state
    }
}
