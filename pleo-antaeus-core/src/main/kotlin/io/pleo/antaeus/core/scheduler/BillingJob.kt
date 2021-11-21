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
    private val failureHandler: FailureHandler = DefaultFailureHandler()
    override fun execute(context: JobExecutionContext?) {
        logger.info { "Starting job [${this.javaClass.name}]..." }
        invoiceService.fetchAllPending()
            .also { logger.info { "Processing ${it.size} pending invoices..." } }
            .map { billingService.charge(it) }
            .forEach {
                // saving it to val to enforce exhaustive when
                val result = when (it) {
                    is PaymentState.Success -> {
                        invoiceService.markPaid(it.invoiceId)
                    }
                    is PaymentState.InsufficientFunds -> {
                        customerService.pauseSubscription(it.customerId, it.invoiceId)
                    }
                    is PaymentState.Failure -> {
                        failureHandler.handle(it)
                    }
                }
            }
    }
}

interface FailureHandler {
    fun handle(failure: PaymentState.Failure): Boolean
}

class DefaultFailureHandler : FailureHandler {
    override fun handle(failure: PaymentState.Failure): Boolean = when (failure) {
        is PaymentState.Failure.CurrencyMismatch -> TODO()
        is PaymentState.Failure.CustomerNotFound -> TODO()
        is PaymentState.Failure.NetworkFailure -> TODO()
    }
}
