package io.pleo.antaeus.core.scheduler

import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.core.state.PaymentState
import org.quartz.JobExecutionContext

data class BillingJob(
    private val invoiceService: InvoiceService,
    private val billingService: BillingService,
) : AntaeusJob(invoiceService, billingService) {
    override fun execute(context: JobExecutionContext?) {
        invoiceService.fetchAll()
            .map { billingService.charge(it) }
            .forEach {
                // saving it to val to enforce exhaustive when
                val result = when (it) {
                    is PaymentState.Success -> {
                        invoiceService.markPaid(it.invoiceId)
                    }
                    is PaymentState.InsufficientFunds -> {
                        TODO("Pause customer subscription due to no payment? or retry")
                    }
                    is PaymentState.Failure.CurrencyMismatch -> {
                        TODO("fix customer/invoice currency")
                    }
                    is PaymentState.Failure.CustomerNotFound -> {
                        TODO("alert someone that we have an invoice w/o a customer :0")
                    }
                    is PaymentState.Failure.NetworkFailure -> {
                        TODO("Retry with back-off?")
                    }
                }
            }

    }
}
