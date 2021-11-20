package io.pleo.antaeus.core.scheduler

import mu.KotlinLogging
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.quartz.Trigger
import org.quartz.impl.StdSchedulerFactory

private val logger = KotlinLogging.logger {}

class BillingScheduler(private val scheduler: Scheduler = StdSchedulerFactory.getDefaultScheduler()) {

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
