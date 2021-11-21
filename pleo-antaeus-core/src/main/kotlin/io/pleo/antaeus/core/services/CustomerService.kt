/*
    Implements endpoints related to customers.
 */

package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.external.Telemetry
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.SubscriptionStatus

class CustomerService(private val dal: AntaeusDal, private val telemetry: Telemetry) {
    fun fetchAll(): List<Customer> {
        return dal.fetchCustomers()
    }

    fun fetch(id: Int): Customer {
        return dal.fetchCustomer(id) ?: throw CustomerNotFoundException(id)
    }

    @Throws(InvoiceNotFoundException::class, CustomerNotFoundException::class)
    fun pauseSubscription(id: Int, invoiceId: Int): Boolean {
        // ensure both exist to avoid errors
        val customerExists = dal.customerExists(id)
        if (customerExists && dal.invoiceExists(invoiceId))
            return dal.updateSubscriptionStatus(id, SubscriptionStatus.PAUSED) and
                    dal.updateInvoiceStatus(invoiceId, InvoiceStatus.PAUSED)
        else if (customerExists) throw InvoiceNotFoundException(id)
        else throw CustomerNotFoundException(id)
    }
}
