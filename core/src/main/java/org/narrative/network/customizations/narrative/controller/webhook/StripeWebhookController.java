package org.narrative.network.customizations.narrative.controller.webhook;

import com.google.gson.JsonSyntaxException;
import org.narrative.common.util.UnexpectedError;
import org.narrative.config.WebhookController;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.customizations.narrative.controller.FiatPaymentWebhookController;
import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.invoices.services.ProcessReversedFiatPaymentTask;
import org.narrative.network.customizations.narrative.service.impl.StaticMethodWrapper;
import org.narrative.network.customizations.narrative.service.impl.invoice.FiatPaymentProcessorType;
import org.narrative.network.shared.util.NetworkLogger;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Charge;
import com.stripe.model.Dispute;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-01-08
 * Time: 13:18
 *
 * @author brian
 */
@RestController
@WebhookController
@RequestMapping("/stripe")
@Validated
public class StripeWebhookController implements FiatPaymentWebhookController {
    private static final NetworkLogger logger = new NetworkLogger(StripeWebhookController.class);

    private static final String CHARGE_REFUNDED__EVENT_TYPE = "charge.refunded";

    private static final String DISPUTE_CLOSED__EVENT_TYPE = "charge.dispute.closed";
    private static final String LOST__DISPUTE_STATUS = "lost";

    private static final String SIGNATURE__HEADER = "Stripe-Signature";

    private final NarrativeProperties narrativeProperties;
    private final StaticMethodWrapper staticMethodWrapper;

    public StripeWebhookController(NarrativeProperties narrativeProperties, StaticMethodWrapper staticMethodWrapper) {
        this.narrativeProperties = narrativeProperties;
        this.staticMethodWrapper = staticMethodWrapper;
    }

    @PostMapping("/niche-payments")
    public ResponseEntity<String> postNichePaymentWebhook(@RequestBody String json, @RequestHeader(SIGNATURE__HEADER) String signature) {
        // jw: Use stripe's API package to convert the json String into an Event
        Event event;
        try {
            event = Webhook.constructEvent(json, signature, narrativeProperties.getStripe().getNichePayments().getWebhookSigningSecret());
        } catch (JsonSyntaxException | SignatureVerificationException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // jw: in all cases currently, we want to revoke niche ownership from a charge, so lets see if we can find a charge
        //     that we want to roll back ownership from.
        String chargeId = null;
        boolean isChargeback = false;

        // jw: first, let's see if this is a lost dispute.
        if (DISPUTE_CLOSED__EVENT_TYPE.equals(event.getType())) {
            assert event.getData().getObject() instanceof Dispute : "Expected dispute data for this event!";

            Dispute dispute = (Dispute) event.getData().getObject();

            // jw: only capture the chargeId if we lost the dispute
            if (LOST__DISPUTE_STATUS.equals(dispute.getStatus())) {
                chargeId = dispute.getCharge();
                isChargeback = true;
            }

            // jw: the only other event we care about is refund.
        } else if (CHARGE_REFUNDED__EVENT_TYPE.equals(event.getType())) {
            assert event.getData().getObject() instanceof Charge : "Expected Charge data for this event!";

            Charge charge = (Charge) event.getData().getObject();
            chargeId = charge.getId();

        } else {
            String message = "Unexpected Stripe eventType/" + event.getType() + ". Why are these being sent? Should we be handling this?";
            StatisticManager.recordException(UnexpectedError.getRuntimeException(message), false, null);
            if (logger.isWarnEnabled()) {
                logger.warn(message);
            }
        }

        // jw: if we go here without finding a charge from the event, let's short out.
        if (isEmpty(chargeId)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // jw: process the chargeback!
        FiatPayment payment = staticMethodWrapper.getAreaContext().doAreaTask(new ProcessReversedFiatPaymentTask(
                FiatPaymentProcessorType.STRIPE
                , chargeId
                , isChargeback
        ));

        // jw: If we don't find a payment let's give an error response to Stripe, since we should always have a payment on our end.
        if (!exists(payment)) {
            String message = "No payment found by charge.id/" + chargeId;
            StatisticManager.recordException(UnexpectedError.getRuntimeException(message), false, null);
            logger.error(message);

            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // jw: At this point we have successful processed this dispute. Let's let Stripe know that everything was AOK
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
