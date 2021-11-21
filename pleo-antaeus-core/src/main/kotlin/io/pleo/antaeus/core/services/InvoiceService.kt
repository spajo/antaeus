/*
    Implements endpoints related to invoices.
 */

package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.external.Telemetry
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus

class InvoiceService(private val dal: AntaeusDal, private val telemetry: Telemetry) {

    fun fetchAll(): List<Invoice> = dal.fetchInvoices()

    fun fetchAllPending(): List<Invoice> = dal.fetchPendingInvoices()

    @Throws(InvoiceNotFoundException::class)
    fun fetch(id: Int): Invoice = dal.fetchInvoice(id) ?: throw InvoiceNotFoundException(id)

    fun markPaid(id: Int): Boolean = dal.updateInvoiceStatus(id, InvoiceStatus.PAID)
}
