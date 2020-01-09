package org.narrative.network.customizations.narrative.services;

import org.narrative.network.shared.context.NetworkContext;
import org.narrative.network.shared.util.NetworkCoreUtils;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Date: 10/23/18
 * Time: 10:35 PM
 *
 * @author brian
 */
public class ReactRouteTest {

    private static final String FAKE_URL = "http://localhost";

    @Mocked
    private NetworkContext networkContext;

    @BeforeEach
    void setup() {
        new Expectations(NetworkCoreUtils.class) {{
            NetworkCoreUtils.networkContext();
            result = networkContext;

            networkContext.getBaseUrl();
            result = FAKE_URL;
        }};
    }

    @Test
    void validate__staticRoute__unmodified() {
        assertEquals(FAKE_URL + "/sign-in", ReactRoute.SIGN_IN.getUrl());
    }

    @Test
    void validate__staticRouteWithParam__throwsException() {
        assertThrows(AssertionError.class, () -> ReactRoute.SIGN_IN.getUrl("param"));
    }

    @Test
    void validate__dynamicRouteWithSingleParam__replaced() {
        assertEquals(FAKE_URL + "/hq/approval/ref123", ReactRoute.APPROVAL_DETAILS.getUrl("ref123"));
    }

    @Test
    void validate__dynamicRouteWithMultipleParams__replaced() {
        assertEquals(FAKE_URL + "/confirm-email/123/abc123", ReactRoute.CONFIRM_EMAIL.getUrl("123", "abc123"));
    }

    @Test
    void validate__dynamicRouteWithIncorrectParams__throwsException() {
        assertThrows(AssertionError.class, () -> ReactRoute.CONFIRM_EMAIL.getUrl("param"));
        assertThrows(AssertionError.class, () -> ReactRoute.CONFIRM_EMAIL.getUrl("param1", "param2", "param3"));
    }
}
