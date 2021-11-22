import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.external.Telemetry
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import io.pleo.antaeus.models.SubscriptionStatus
import mu.KotlinLogging
import java.math.BigDecimal
import kotlin.random.Random

// This will create all schemas and setup initial data
internal fun setupInitialData(dal: AntaeusDal) {
    val customers = (1..100).mapNotNull {
        dal.createCustomer(
            currency = Currency.values()[Random.nextInt(0, Currency.values().size)]
        )
    }

    customers.forEach { customer ->
        (1..10).forEach {
            dal.createInvoice(
                amount = Money(
                    value = BigDecimal(Random.nextDouble(10.0, 500.0)),
                    // put in some mismatched currencies
                    currency = if (Random.nextInt(100) < 95) customer.currency
                    else getRandomCurrency()
                ),
                customer = customer,
                status = if (it == 1) InvoiceStatus.PENDING else InvoiceStatus.PAID
            )
        }
    }

    // put in some not existing customers
    dal.createInvoice(
        amount = Money(
            value = BigDecimal(Random.nextDouble(10.0, 500.0)),
            // put in some mismatched currencies
            currency = getRandomCurrency()
        ),
        customer = Customer(404, Currency.USD, SubscriptionStatus.ACTIVE),
        status = InvoiceStatus.PENDING
    )
}

internal fun getRandomCurrency(): Currency {
    return Currency.values()[Random.nextInt(0, Currency.values().size)]
}

// This is the mocked instance of the payment provider
internal fun getPaymentProvider(): PaymentProvider {
    return object : PaymentProvider {
        override fun charge(invoice: Invoice): Boolean {
            if (Random.nextInt(100) > 90) throw NetworkException()
            if (invoice.customerId == 404) throw CustomerNotFoundException(404)
            return Random.nextBoolean()
        }
    }
}

private val logger = KotlinLogging.logger {}

// Mocked telemetry, this will gather all alerts for ops team
internal fun getTelemetry(): Telemetry {
    return object : Telemetry {
        override fun sendAlert(domain: String, alertMessage: String) {
            // let's assume that his sends an Alert to something like Application Insights
            logger.info { "[$domain] $alertMessage" }
        }
    }
}
