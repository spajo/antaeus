package io.pleo.antaeus.core.scheduler

import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.core.state.PaymentState
import org.quartz.JobExecutionContext

data class BillingJob(
    private val invoiceService: InvoiceService,
    private val billingService: BillingService
) : AntaeusJob(invoiceService, billingService) {
    override fun execute(context: JobExecutionContext?) {
        invoiceService.fetchAll()
            .map { billingService.charge(it) }
            .forEach {
                // saving it to val to enforce exhaustive when
                val result = when (it) {
                    is PaymentState.Success -> TODO()
                    is PaymentState.InsufficientFunds -> TODO()
                    is PaymentState.Failure.CurrencyMismatch -> TODO()
                    is PaymentState.Failure.CustomerNotFound -> TODO()
                    is PaymentState.Failure.NetworkFailure -> TODO()
                }
            }

    }
}
