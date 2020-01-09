package org.narrative.network.core.user.services;

import org.narrative.common.util.IPHTMLUtil;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.JSONMap;
import org.narrative.common.util.ValidationHandler;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.shared.context.NetworkContext;

import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * User: jonmark
 * Date: 8/6/15
 * Time: 11:27 AM
 */
public class RecaptchaValidation {
    private static final String G_RECAPTCHA_RESPONSE_PARAM = "g-recaptcha-response";

    private static final String VERIFICATION_URL = "https://www.google.com/recaptcha/api/siteverify";
    private static final String SECRET_PARAM = "secret";
    private static final String RESPONSE_PARAM = "response";
    private static final String REMOTE_IP_PARAM = "remoteip";

    private static final String SUCCESS_PROPERTY = "success";
    private static final String HOSTNAME_PROPERTY = "hostname";

    public static boolean validate(String recaptchaResponse, NetworkContext networkContext) {
        if (isEmpty(recaptchaResponse)) {
            return false;
        }

        Map<String, String> params = newHashMap();
        params.put(SECRET_PARAM, NetworkRegistry.getInstance().getReCaptchaPrivateKey());
        params.put(RESPONSE_PARAM, recaptchaResponse);

        String ip = networkContext.getReqResp().getRemoteHostIp();
        if (!isEmpty(ip)) {
            params.put(REMOTE_IP_PARAM, ip);
        }

        JSONMap json = JSONMap.getJsonMap(IPHTMLUtil.getPostResponse(VERIFICATION_URL, params));

        // bl: success must be true first and foremost!
        if (json == null || !json.getBoolean(SUCCESS_PROPERTY)) {
            return false;
        }

        // finally, make sure that the request actually came from the proper hostname.
        // we need to do this validation now that we are going to be disabling Domain Name Validation
        // in the reCAPTCHA configuration.
        return IPStringUtil.isStringEqualIgnoreCase(networkContext.getAuthZone().getArea().getPrimaryAreaDomainName(), json.getString(HOSTNAME_PROPERTY));
    }

    public static boolean validateRecaptcha(NetworkContext networkContext, ValidationHandler validationHandler) {
        boolean reCaptchaSuccess = RecaptchaValidation.validate(networkContext.getReqResp().getParamValue(G_RECAPTCHA_RESPONSE_PARAM), networkContext);

        if (!reCaptchaSuccess) {
            validationHandler.addWordletizedActionError("reCaptcha.error");
        }

        return reCaptchaSuccess;
    }

    public static boolean validateRecaptcha(String recaptchaResponse, NetworkContext networkContext, ValidationContext validationContext) {
        boolean reCaptchaSuccess = RecaptchaValidation.validate(recaptchaResponse, networkContext);

        if (!reCaptchaSuccess) {
            validationContext.addMethodError("reCaptcha.error");
        }

        return reCaptchaSuccess;
    }
}
