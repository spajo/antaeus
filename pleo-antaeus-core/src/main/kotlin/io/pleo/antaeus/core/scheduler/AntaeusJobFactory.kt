package io.pleo.antaeus.core.scheduler

import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.InvoiceService
import org.quartz.Job
import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.quartz.simpl.SimpleJobFactory
import org.quartz.spi.TriggerFiredBundle

/**
 * Abstract Job class to allow injection of services.
 *
 * Sealed class to disallow extending it outside the module.
 */
sealed class AntaeusJob(
    invoiceService: InvoiceService,
    customerService: CustomerService,
    billingService: BillingService,
) : Job

/**
 * Produces jobs and handles service injection into `AntaeusJobs`
 * @see AntaeusJob
 */
class AntaeusJobFactory(
    private val invoiceService: InvoiceService,
    private val customerService: CustomerService,
    private val billingService: BillingService,
) : SimpleJobFactory() {
    override fun newJob(bundle: TriggerFiredBundle?, scheduler: Scheduler?): Job {
        val jobDetail = bundle!!.jobDetail
        val jobClass = jobDetail.jobClass
        return if (AntaeusJob::class.java.isAssignableFrom(jobClass)) {
            try {
                jobClass.getDeclaredConstructor(
                    InvoiceService::class.java,
                    CustomerService::class.java,
                    BillingService::class.java
                ).newInstance(invoiceService, customerService, billingService)
            } catch (e: Exception) {
                throw SchedulerException(
                    "Problem instantiating Antaeus Job '${jobClass.name}'", e
                )
            }
        } else super.newJob(bundle, scheduler)
    }
}
