package io.pleo.antaeus.scheduler

import org.quartz.JobExecutionContext

fun JobExecutionContext.jobResult(): Result {
    return this.result as Result
}
