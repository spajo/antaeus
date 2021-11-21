package io.pleo.antaeus.core.scheduler

import io.pleo.antaeus.core.external.Telemetry
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.listeners.JobListenerSupport

class AntaeusJobListener(private val telemetry: Telemetry) : JobListenerSupport() {
    override fun getName(): String = "antaeus-job-listener"

    override fun jobExecutionVetoed(context: JobExecutionContext?) {
        val message = context?.let {
            it.jobDetail.jobClass.name.let { "$it execution vetoed" }
        } ?: "Unable to get job details..."
        telemetry.sendAlert("JOB", message)
    }

    override fun jobWasExecuted(
        context: JobExecutionContext?,
        jobException: JobExecutionException?,
    ) {
        val message = context?.let {
            val resultMessage = it.result as String
            it.jobDetail.jobClass.name.let { "$it finished! $resultMessage" }
        } ?: "Unable to get job details..."
        telemetry.sendAlert("JOB", message)
    }
}
