package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.external.Telemetry
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.SubscriptionStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CustomerServiceTest {
    private val dal = mockk<AntaeusDal> {
        every { fetchCustomer(404) } returns null
        every { customerExists(1) } returns true
        every { customerExists(404) } returns false
        every { updateSubscriptionStatus(1, SubscriptionStatus.PAUSED) } returns true
        every { updateSubscriptionStatus(404, SubscriptionStatus.PAUSED) } returns false
    }

    private val telemetry = object : Telemetry {
        override fun sendAlert(domain: String, alertMessage: String) {
            // do nothing
        }
    }

    private val customerService = CustomerService(dal = dal, telemetry = telemetry)

    @Test
    fun `will throw if customer is not found`() {
        assertThrows<CustomerNotFoundException> {
            customerService.fetch(404)
        }
    }

    @Test
    fun `will return false if customer does not exist`() {
        assert(customerService.pauseSubscription(404).not())
    }

    @Test
    fun `will pause the customers subscription`() {
        assert(customerService.pauseSubscription(1))
        verify {
            dal.updateSubscriptionStatus(1, SubscriptionStatus.PAUSED)
        }
    }
}
