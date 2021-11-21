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
        .groupBy { it }
        .toList()
        .fold(Result()) { acc, pair ->
            acc.apply {
                if (pair.first) {
                    paymentSuccessCount = pair.second.size
                } else
                    paymentFailureCount = pair.second.size
            }
        }

    private fun handleState(state: InvoiceState): Boolean = when (state) {
        is InvoiceState.Paid -> true
        is InvoiceState.Paused -> {
            customerService
                .pauseSubscription(state.customerId)
        }
        is InvoiceState.Invalid -> false
        is InvoiceState.Pending -> {
            // try again, I don't like recursion this will have to change probs
            // TODO: add some backoff?
            handleState(invoiceService.charge(state.invoice))
        }
    }
}
