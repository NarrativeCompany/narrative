package org.narrative.network.core.security;

import lombok.Builder;
import lombok.Value;

/**
 * Date: 9/30/18
 * Time: 12:12 PM
 *
 * @author brian
 */
@Value
@Builder
public class NarrativeLoginDetails {
    private final boolean rememberMe;
}
