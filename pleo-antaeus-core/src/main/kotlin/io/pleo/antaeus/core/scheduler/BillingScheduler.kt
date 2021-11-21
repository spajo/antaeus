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

    private val jobListener = AntaeusJobListener(telemetry)

    val results: Results
        get() = jobListener

    init {
        scheduler.setJobFactory(AntaeusJobFactory(invoiceService, customerService))
        scheduler.listenerManager.addJobListener(jobListener as JobListener)
    }

    /**
     * Schedule a job, you can create job and trigger from this micro DSL.
     *
     * @see ScheduleJob.jobDetail
     * @see ScheduleJob.cronTrigger
     */
    fun schedule(scheduleJob: ScheduleJob.() -> Unit) {
        ScheduleJob()
            .apply(scheduleJob)
            .throwIfUninitialized()
            .let {
                scheduler.scheduleJob(it.jobDetail, it.trigger)
            }
    }

    inner class ScheduleJob {
        lateinit var jobDetail: JobDetail
        internal lateinit var trigger: Trigger

        /**
         * Kotlinified quartz job builder
         */
        inline fun <reified T : Job> job(builder: JobBuilder.() -> JobBuilder) {
            jobDetail = JobBuilder.newJob(T::class.java)
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

        internal fun throwIfUninitialized(): ScheduleJob {
            if (!::jobDetail.isInitialized || !::trigger.isInitialized)
                throw SchedulerException("BillingScheduler job initialization error!")
            return this
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
