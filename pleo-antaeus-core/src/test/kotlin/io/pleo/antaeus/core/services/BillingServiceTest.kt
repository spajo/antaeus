package io.pleo.antaeus.core.services

import io.mockk.MockKStubScope
import io.mockk.every
import io.mockk.mockk
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.state.PaymentState
import io.pleo.antaeus.core.state.PaymentState.Failure.*
import io.pleo.antaeus.core.state.PaymentState.InsufficientFunds
import io.pleo.antaeus.core.state.PaymentState.Success
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.math.BigDecimal
import kotlin.reflect.KClass


class BillingServiceTest {

    @ParameterizedTest
    @MethodSource("stateSource")
    private fun stateTest(paymentProvider: PaymentProvider, state: KClass<out PaymentState>) {
        val billingService = BillingService(paymentProvider)
        assert(billingService.charge(invoice)::class == state) { "$state test failed" }
    }

    companion object {
        private val invoice =
            Invoice(1, 1, Money(BigDecimal.valueOf(100), Currency.USD), InvoiceStatus.PENDING)

        @JvmStatic
        private fun paymentProviderMock(stub: MockKStubScope<Boolean, Boolean>.() -> Unit) =
            mockk<PaymentProvider> {
                every { charge(invoice) }.stub()
            }

        @JvmStatic
        private fun stateSource(): List<Arguments> {
            return listOf(
                paymentProviderMock {returns(true)} andArg Success::class,
                paymentProviderMock {returns(true)} andArg InsufficientFunds::class,
                paymentProviderMock {throws(CustomerNotFoundException(1))} andArg CustomerNotFound::class,
                paymentProviderMock {throws(CurrencyMismatchException(1, 1))} andArg CurrencyMismatch::class,
                paymentProviderMock {throws(NetworkException())} andArg NetworkFailure::class
            )
        }

        private infix fun <A, B> A.andArg(that: B): Arguments = Arguments.of(this, that)
    }

}
