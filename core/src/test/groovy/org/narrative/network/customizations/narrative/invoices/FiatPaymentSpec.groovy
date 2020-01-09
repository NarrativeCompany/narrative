package org.narrative.network.customizations.narrative.invoices

import org.narrative.network.customizations.narrative.NrveValue
import spock.lang.Specification
import spock.lang.Unroll

class NicheAuctionInvoiceFiatPaymentSpec extends Specification {

    @Unroll
    def "Calculate fiat payment ensuring a minimum of \$75"() {
        given:
            def fiatPayment = new FiatPayment()
            NrveValue nrveAmount = Mock()

        when:
            nrveAmount.getValue() >> BigDecimal.valueOf(amount)
            BigDecimal calculatedAmount = fiatPayment.calculateUsdAmount(new BigDecimal(price), nrveAmount)

        then:
            1 * nrveAmount.getValue() >> BigDecimal.valueOf(amount)
            calculatedAmount.compareTo(result) == 0

        where:
            price  | amount || result
            "0.01" | 8000   || 80
            "0.01" | 7812   || 78.12
            "0.02" | 5000   || 100.00
            "0.03" | 1000   || 75.00
            "0.01" | 1000   || 75.00
            "0.01" | 7500   || 75.00
            "0.05" | 8000   || 400.00
    }
}
