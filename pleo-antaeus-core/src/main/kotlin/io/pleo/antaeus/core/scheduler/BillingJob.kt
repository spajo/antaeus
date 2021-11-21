package io.pleo.antaeus.core.scheduler

import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.core.state.PaymentState
import mu.KotlinLogging
import org.quartz.JobExecutionContext

private val logger = KotlinLogging.logger {}

class BillingJob(
    private val invoiceService: InvoiceService,
    private val customerService: CustomerService,
    private val billingService: BillingService,
) : AntaeusJob(invoiceService, customerService, billingService) {

    override fun execute(context: JobExecutionContext?) {
        logger.info { "Starting job [${this.javaClass.name}]..." }
        context?.result = invoiceService.fetchAllPending()
            .also { logger.info { "Processing ${it.size} pending invoices..." } }
            .asSequence()
            .map { billingService.charge(it) }
            .map {
                when (it) {
                    is PaymentState.Success -> {
                        invoiceService.markPaid(it.invoiceId)
                    }
                    is PaymentState.InsufficientFunds -> {
                        customerService
                            .pauseSubscription(it.customerId, it.invoiceId)
                    }
                    is PaymentState.Failure -> {
                        handle(it)
                    }
                }
            }
            .groupBy { it }
            .map { (key, value) -> (if (key) "success" else "failure") to value.size }
            .joinToString(prefix = "Billing Job Results") { "${it.first} : ${it.second}" }
    }

    private fun handle(failure: PaymentState.Failure): Boolean = when (failure) {
        is PaymentState.Failure.CurrencyMismatch -> TODO()
        is PaymentState.Failure.CustomerNotFound -> TODO()
        is PaymentState.Failure.NetworkFailure -> TODO()
    }
}
