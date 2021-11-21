package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.external.Telemetry
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.InvoiceStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class InvoiceServiceTest {
    private val dal = mockk<AntaeusDal> {
        every { fetchInvoice(404) } returns null
        every { updateInvoiceStatus(1, InvoiceStatus.PAID) } returns true
        every { updateInvoiceStatus(404, InvoiceStatus.PAID) } returns false
    }

    private val telemetry = mockk<Telemetry> {}

    private val invoiceService = InvoiceService(dal = dal, telemetry = telemetry)

    @Test
    fun `will throw if invoice is not found`() {
        assertThrows<InvoiceNotFoundException> {
            invoiceService.fetch(404)
        }
    }

    @Test
    fun `will update the invoice and return true`() {
        assert(invoiceService.markPaid(1))
        verify {
            dal.updateInvoiceStatus(1, InvoiceStatus.PAID)
        }
    }

    @Test
    fun `will not update the invoice and return false if not found`() {
        assert(!invoiceService.markPaid(404))
        verify {
            dal.updateInvoiceStatus(404, InvoiceStatus.PAID)
        }
    }
}
