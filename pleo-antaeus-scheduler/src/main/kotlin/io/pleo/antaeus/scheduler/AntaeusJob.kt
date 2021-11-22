package io.pleo.antaeus.scheduler

import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.InvoiceService
import org.quartz.Job
import org.quartz.JobExecutionContext

/**
 * Abstract Job class to allow injection of services.
 *
 * Sealed class to disallow extending it outside the module.
 */
sealed class AntaeusJob(
    invoiceService: InvoiceService,
    customerService: CustomerService,
) : Job {
    abstract fun startJob(): Result
    override fun execute(context: JobExecutionContext?) {
        context?.apply {
            result = startJob().also {
                it.addJobData(this)
            }
        }
    }
}
