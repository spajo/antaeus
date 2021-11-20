package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.state.PaymentState
import io.pleo.antaeus.models.Invoice
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class BillingService(
    private val paymentProvider: PaymentProvider
) {

    /**
     * Charges the invoice via the payment provider and returns the state of the payment.
     *
     * @return `PaymentState.Success` in case of successful payment, `PaymentState.InsufficientFunds`
     *  when the provider returns `False`, `PaymentState.Failure` in case of any Exceptions
     *  @see PaymentState
     */
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
        logger.error("Invoice[$invoiceId] charging failed!", ex)
    }
}
