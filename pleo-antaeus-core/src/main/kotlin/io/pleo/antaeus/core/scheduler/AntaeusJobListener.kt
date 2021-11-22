package io.pleo.antaeus.core.scheduler

import io.pleo.antaeus.core.external.Telemetry
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.listeners.JobListenerSupport

interface Results {
    val results: List<Result>
}

class AntaeusJobListener(private val telemetry: Telemetry) : JobListenerSupport(), Results {

    // Ideally those results would be stored in the DB
    // or reported to some external service
    private val _results = mutableListOf<Result>()
    override val results
        get() = _results.toList()

    override fun getName(): String = "antaeus-job-listener"

    override fun jobExecutionVetoed(context: JobExecutionContext?) {
        val message = context?.let {
            it.jobDetail.jobClass.name.let { "$it execution vetoed" }
        } ?: "Job vetoed, but unable to get job details..."
        telemetry.sendAlert("JOB", message)
    }

    override fun jobWasExecuted(
        context: JobExecutionContext?,
        jobException: JobExecutionException?,
    ) {
        val message = context?.let {
            val result = it.jobResult()
            _results += result
            it.jobDetail.jobClass.name.let { "$it finished! $result" }
        } ?: "Job Finished, but unable to get job details..."
        telemetry.sendAlert("JOB", message)
    }
}
