package io.pleo.antaeus.core.external

interface Telemetry {
    fun sendAlert(domain: String, alertMessage: String)
}
