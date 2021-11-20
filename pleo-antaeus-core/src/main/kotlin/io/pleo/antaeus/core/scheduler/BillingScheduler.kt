package io.pleo.antaeus.core.scheduler

import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.InvoiceService
import mu.KotlinLogging
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.quartz.Trigger
import org.quartz.impl.StdSchedulerFactory

private val logger = KotlinLogging.logger {}

class BillingScheduler(
    invoiceService: InvoiceService,
    billingService: BillingService,
    private val scheduler: Scheduler = StdSchedulerFactory.getDefaultScheduler()
) {

    init {
        scheduler.setJobFactory(AntaeusJobFactory(invoiceService, billingService))
    }

    fun scheduleJob(job: JobDetail, trigger: Trigger) {
        scheduler.scheduleJob(job, trigger)
    }

    fun start() {
        try {
            scheduler.start()
        } catch (ex: SchedulerException) {
            logger.error("Failed to start Job Scheduler!", ex)
        }

        // Add shutdown hook so we terminate the scheduler before the JVM
        Runtime.getRuntime().addShutdownHook(SchedulerShutdownHook(scheduler))
    }

    private class SchedulerShutdownHook(private val scheduler: Scheduler) : Thread() {
        override fun run() {
            try {
                scheduler.shutdown()
            } catch (ex: SchedulerException) {
                logger.error("Failed to shutdown Job Scheduler!", ex)
            }
        }
    }
}
