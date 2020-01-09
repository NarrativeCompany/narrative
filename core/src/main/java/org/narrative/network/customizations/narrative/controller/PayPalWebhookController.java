package org.narrative.network.customizations.narrative.controller;

import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.enums.EnumRegistry;
import org.narrative.config.WebhookController;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.paypal.services.PayPalWebhookEventType;
import org.narrative.network.customizations.narrative.service.impl.StaticMethodWrapper;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.util.NetworkLogger;
import com.paypal.api.payments.Event;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.JSONFormatter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-01-25
 * Time: 14:48
 *
 * @author jonmark
 */
@RestController
@WebhookController
@RequestMapping("/paypal")
@Validated
public class PayPalWebhookController implements FiatPaymentWebhookController {
    private static final NetworkLogger logger = new NetworkLogger(PayPalWebhookController.class);

    private static final String NICHE_PAYMENTS_PATH = "/niche-payments";
    private static final String KYC_PAYMENTS_PATH = "/kyc-payments";

    private final NarrativeProperties narrativeProperties;
    private final StaticMethodWrapper staticMethodWrapper;

    public PayPalWebhookController(NarrativeProperties narrativeProperties, StaticMethodWrapper staticMethodWrapper) {
        this.narrativeProperties = narrativeProperties;
        this.staticMethodWrapper = staticMethodWrapper;
    }

    @PostMapping(NICHE_PAYMENTS_PATH)
    public ResponseEntity<String> postNichePaymentWebhook(@RequestBody String body, @RequestHeader HttpHeaders headers) {
        return processWebhookRequest(NICHE_PAYMENTS_PATH, narrativeProperties.getPayPal().getChannelPayments(), body, headers);
    }

    @PostMapping(KYC_PAYMENTS_PATH)
    public ResponseEntity<String> postKycPaymentWebhook(@RequestBody String body, @RequestHeader HttpHeaders headers) {
        return processWebhookRequest(KYC_PAYMENTS_PATH, narrativeProperties.getPayPal().getKycPayments(), body, headers);
    }

    private ResponseEntity<String> processWebhookRequest(String webhookPath, NarrativeProperties.PayPal.ApiConfig apiConfig, @RequestBody String body, @RequestHeader HttpHeaders httpHeaders) {
        assert apiConfig != null : "Should always be provided with a PayPal ApiConfig!";

        APIContext apiContext = apiConfig.getApiContext();

        try {
            boolean result = Event.validateReceivedEvent(
                    apiContext,
                    httpHeaders.toSingleValueMap(),
                    body
            );

            if (!result) {
                logError(webhookPath, "Validation failed for json/"+body, null);

                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            logError(webhookPath, "Error validating request with json/"+body, e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // jw: let's use the same JSON deserializer that PayPal is using internally.
        Event event = JSONFormatter.fromJSON(body, Event.class);

        // jw: let's see if we are handling this type of event. Let's not force it, so that the null value can represent
        //     all unhandled event types.
        PayPalWebhookEventType eventType = EnumRegistry.getForId(PayPalWebhookEventType.class, event.getEventType(), false);

        if (eventType==null) {
            logError(webhookPath, "Encountered unsupported eventType. Should we be handling this? et/"+event.getEventType()+" json/"+body, null);

            // jw: let's return a negative response in this case, so that PayPal will continue to send this message over
            //     three days and we will have time to add a missing Event if necessary.
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // jw: because the event.getResource is a "Object" which ultimately is a Map<String, Object>. we need to get
        //     that into a format we can reliably convert into an object.
        assert event.getResource() instanceof Map : "The gson processor should have made the unknown 'Object' type of Event.resource into a Map<String, Object>! not/"+event.getResource().getClass().getName();
        AreaTaskImpl<FiatPayment> eventProcessor = eventType.getEventProcessor((Map<String, Object>) event.getResource());

        // jw: if we failed to create a eventProcessor, that means we did not parse things out properly from the resource.
        if (eventProcessor==null) {
            String message = "Failed parsing processor from known eventType. et/"+eventType+" json/"+body;
            if (logger.isWarnEnabled()) {
                logger.warn(message);
            }
            StatisticManager.recordException(UnexpectedError.getRuntimeException(message), false, null);

            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // jw: process the handler for this eventType
        try {
            FiatPayment payment = staticMethodWrapper.getAreaContext().doAreaTask(eventProcessor);

            // jw: all processors should result in a payment object, if we did not get one then log an error and give a error response.
            if (!exists(payment)) {
                logError(webhookPath, "Processing of event/"+eventType+" did not result in expected payment object. json/" + body, null);

                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            logError(webhookPath, "Failed executing processor for PayPal webhook! json/"+body, e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // jw: At this point we have successful processed this handler. Let's let PayPal know that everything was AOK so that
        //     we do not get this event again.
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // jw: this utility function centralizes the pattern for logging webhook errors.
    private void logError(String webhookPath, String message, Exception error) {
        message = "PayPal webhook ("+webhookPath+") Error: "+message;
        error = error !=null
            ? UnexpectedError.getRuntimeException(message, error)
            : UnexpectedError.getRuntimeException(message);

        logger.error(message, error);
        StatisticManager.recordException(error, false, null);
    }
}
