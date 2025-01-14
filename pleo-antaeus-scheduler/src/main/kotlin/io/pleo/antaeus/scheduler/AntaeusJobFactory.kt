package io.pleo.antaeus.scheduler

import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.InvoiceService
import org.quartz.Job
import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.quartz.simpl.SimpleJobFactory
import org.quartz.spi.TriggerFiredBundle

/**
 * Produces jobs and handles service injection into `AntaeusJobs`
 * @see AntaeusJob
 */
class AntaeusJobFactory(
    private val invoiceService: InvoiceService,
    private val customerService: CustomerService,
) : SimpleJobFactory() {
    override fun newJob(bundle: TriggerFiredBundle?, scheduler: Scheduler?): Job {
        val jobDetail = bundle!!.jobDetail
        val jobClass = jobDetail.jobClass
        return if (AntaeusJob::class.java.isAssignableFrom(jobClass)) {
            try {
                jobClass.getDeclaredConstructor(
                    InvoiceService::class.java,
                    CustomerService::class.java
                ).newInstance(invoiceService, customerService)
            } catch (e: Exception) {
                throw SchedulerException(
                    "Problem instantiating Antaeus Job '${jobClass.name}'", e
                )
            }
        } else super.newJob(bundle, scheduler)
    }
}
