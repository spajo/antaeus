package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.SubscriptionStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CustomerServiceTest {
    private val dal = mockk<AntaeusDal> {
        every { fetchCustomer(404) } returns null
        every { customerExists(1) } returns true
        every { customerExists(404) } returns false
        every { invoiceExists(1) } returns true
        every { invoiceExists(404) } returns false
        every { updateSubscriptionStatus(1, SubscriptionStatus.PAUSED) } returns true
        every { updateInvoiceStatus(1, InvoiceStatus.PAUSED) } returns true
    }

    private val customerService = CustomerService(dal = dal)

    @Test
    fun `will throw if customer is not found`() {
        assertThrows<CustomerNotFoundException> {
            customerService.fetch(404)
        }
    }

    @Test
    fun `will throw if customer is not found while updating status`() {
        assertThrows<CustomerNotFoundException> {
            customerService.pauseSubscription(404, 1)
        }
    }

    @Test
    fun `will throw if invoice is not found while updating status`() {
        assertThrows<InvoiceNotFoundException> {
            customerService.pauseSubscription(1, 404)
        }
    }

    @Test
    fun `will update the status if both invoice and customer are present`() {
        assert(customerService.pauseSubscription(1, 1))
        verify {
            dal.updateInvoiceStatus(1, InvoiceStatus.PAUSED)
            dal.updateSubscriptionStatus(1, SubscriptionStatus.PAUSED)
        }
    }
}
