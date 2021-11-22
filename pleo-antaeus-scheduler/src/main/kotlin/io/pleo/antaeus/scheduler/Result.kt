package io.pleo.antaeus.scheduler

import org.quartz.JobExecutionContext
import java.sql.Timestamp
import java.time.Instant
import java.util.*

interface Results {
    val results: List<Result>
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
