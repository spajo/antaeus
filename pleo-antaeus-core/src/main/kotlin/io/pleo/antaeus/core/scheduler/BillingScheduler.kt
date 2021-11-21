package io.pleo.antaeus.core.scheduler

import io.pleo.antaeus.core.external.Telemetry
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.InvoiceService
import mu.KotlinLogging
import org.quartz.*
import org.quartz.impl.StdSchedulerFactory

private val logger = KotlinLogging.logger {}

class BillingScheduler(
    invoiceService: InvoiceService,
    customerService: CustomerService,
    telemetry: Telemetry,
    private val scheduler: Scheduler = StdSchedulerFactory.getDefaultScheduler(),
) {

    init {
        scheduler.setJobFactory(AntaeusJobFactory(invoiceService, customerService))
        scheduler.listenerManager.addJobListener(AntaeusJobListener(telemetry))
    }

    /**
     * Schedule a job, you can create job and trigger from this micro DSL.
     *
     * @see ScheduleJob.job
     * @see ScheduleJob.cronTrigger
     */
    fun schedule(scheduleJob: ScheduleJob.() -> Unit) {
        ScheduleJob()
            .apply(scheduleJob)
            .let {
                scheduler.scheduleJob(it.job, it.trigger)
            }
    }

    inner class ScheduleJob {
        // has to be public due to type erasure :(
        // TODO: add more descriptive exceptions instead of lateinit
        lateinit var job: JobDetail
        internal lateinit var trigger: Trigger

        /**
         * Kotlinified quartz job builder
         */
        inline fun <reified T : Job> job(builder: JobBuilder.() -> JobBuilder) {
            job = JobBuilder.newJob(T::class.java)
                .builder()
                .build()
        }

        /**
         * Creates Cron Trigger
         * @see CronExpression
         * @see CronTrigger
         */
        fun cronTrigger(
            cronExpression: String,
            builder: TriggerBuilder<CronTrigger>.() -> TriggerBuilder<CronTrigger>,
        ) {
            trigger = TriggerBuilder.newTrigger()
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .builder()
                .build()
        }
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
