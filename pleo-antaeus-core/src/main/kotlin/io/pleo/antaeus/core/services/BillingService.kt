package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.state.PaymentState
import io.pleo.antaeus.models.Invoice
import org.slf4j.LoggerFactory

class BillingService(
    private val paymentProvider: PaymentProvider
) {
    companion object {
        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(PaymentState::class.java)
    }

    // TODO - Add code e.g. here
    // EX to handle
    // `CustomerNotFoundException`: when no customer has the given id.
    // `CurrencyMismatchException`: when the currency does not match the customer account.
    // `NetworkException`: when a network error happens.
    fun charge(invoice: Invoice) = try {
        if (paymentProvider.charge(invoice)) PaymentState.Success(invoice.id)
        else PaymentState.InsufficientFunds(invoice.id)
    } catch (ex: CustomerNotFoundException) {
        error(invoice.id, ex)
        PaymentState.Failure.CustomerNotFound(invoice.id, invoice.customerId)
    } catch (ex: CurrencyMismatchException) {
        error(invoice.id, ex)
        PaymentState.Failure.CurrencyMismatch(invoice.id, invoice.customerId)
    } catch (ex: NetworkException) {
        error(invoice.id, ex)
        PaymentState.Failure.NetworkFailure(invoice.id)
    }

    private fun error(invoiceId: Int, ex: Exception) {
        LOGGER.error("Invoice[$invoiceId] charging failed!", ex)
    }
}
