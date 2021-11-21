package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.external.Telemetry
import io.pleo.antaeus.core.state.InvoiceState
import io.pleo.antaeus.core.state.PaymentState
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.math.BigDecimal
import kotlin.reflect.KClass

class InvoiceServiceTest {
    private val dal = mockk<AntaeusDal> {
        every { fetchInvoice(404) } returns null
        every { updateInvoiceStatus(1, InvoiceStatus.PAID) } returns true
        every { updateInvoiceStatus(404, InvoiceStatus.PAID) } returns false
        every { fetchInvoice(1) } returns invoice
    }

    private val telemetry = mockk<Telemetry> {}

    @Test
    fun `will throw if invoice is not found`() {
        val invoiceService =
            InvoiceService(dal = dal,
                telemetry = telemetry,
                billingService = billingServiceMock(PaymentState.Failure.CustomerNotFound(404,
                    404)))
        assertThrows<InvoiceNotFoundException> {
            invoiceService.fetch(404)
        }
    }

    @ParameterizedTest
    @MethodSource("stateSource")
    private fun stateTest(paymentState: PaymentState, state: KClass<out InvoiceState>) {
        val billingService = billingServiceMock(paymentState)
        val invoiceService =
            InvoiceService(dal = dal, telemetry = telemetry, billingService = billingService)
        assert(invoiceService.charge(invoice)::class == state) { "$state test failed" }
    }

    companion object {
        private val invoice =
            Invoice(1, 1, Money(BigDecimal.valueOf(100), Currency.USD), InvoiceStatus.PENDING)

        @JvmStatic
        private fun billingServiceMock(paymentState: PaymentState) =
            mockk<BillingService> {
                every { charge(invoice) }.returns(paymentState)
            }

        @JvmStatic
        private fun stateSource(): List<Arguments> {
            return listOf(
                PaymentState.Success(invoice.id) andArg InvoiceState.Paid::class,
                PaymentState.Success(404) andArg InvoiceState.Invalid::class,
                PaymentState.InsufficientFunds(invoice.id,
                    invoice.customerId) andArg InvoiceState.Paused::class,
                PaymentState.Failure.CustomerNotFound(invoice.id,
                    invoice.customerId) andArg InvoiceState.Invalid::class,
                PaymentState.Failure.CurrencyMismatch(invoice.id,
                    invoice.customerId) andArg InvoiceState.Invalid::class,
                PaymentState.Failure.NetworkFailure(invoice.id) andArg InvoiceState.Pending::class,
            )
        }
    }
}
