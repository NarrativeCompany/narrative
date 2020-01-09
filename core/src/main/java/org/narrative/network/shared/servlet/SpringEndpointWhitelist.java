package org.narrative.network.shared.servlet;

import org.narrative.config.properties.NarrativeProperties;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Date: 11/8/18
 * Time: 11:02 AM
 *
 * @author brian
 */
@Component
public class SpringEndpointWhitelist {
    private final List<String> uriIgnorePrefixes;

    public SpringEndpointWhitelist(NarrativeProperties narrativeProperties, WebEndpointProperties webEndpointProperties) {
        List<String> uriIgnorePrefixes = new ArrayList<>(2);
        String apiUri = narrativeProperties.getSpring().getMvc().getBaseUri();
        if(StringUtils.isNotEmpty(apiUri)) {
            uriIgnorePrefixes.add(apiUri);
        }
        String webhooksUri = narrativeProperties.getSpring().getMvc().getWebhooksBaseUri();
        if(StringUtils.isNotEmpty(webhooksUri)) {
            uriIgnorePrefixes.add(webhooksUri);
        }
        String actuatorBasePath = webEndpointProperties.getBasePath();
        if(StringUtils.isNotEmpty(actuatorBasePath)) {
            uriIgnorePrefixes.add(actuatorBasePath);
        }
        this.uriIgnorePrefixes = Collections.unmodifiableList(uriIgnorePrefixes);
    }

    boolean isWhitelisted(String uri) {
        for (String uriIgnorePrefix : uriIgnorePrefixes) {
            if(uri.startsWith(uriIgnorePrefix)) {
                return true;
            }
        }
        return false;
    }
}
