package io.pleo.antaeus.core.scheduler

import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.InvoiceService
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.quartz.simpl.SimpleJobFactory
import org.quartz.spi.TriggerFiredBundle
import java.sql.Timestamp
import java.time.Instant
import java.util.*

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

data class Result(
    var timestamp: Timestamp = Timestamp.from(Instant.now()),
    var paymentSuccessCount: Int = 0,
    var invalidInvoicesCount: Int = 0,
    var pausedInvoicesCount: Int = 0,
) {

    lateinit var jobId: String
    lateinit var fireTime: Date

    internal fun addJobData(context: JobExecutionContext) {
        jobId = context.jobDetail.key.name
        fireTime = context.fireTime
    }
}

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
