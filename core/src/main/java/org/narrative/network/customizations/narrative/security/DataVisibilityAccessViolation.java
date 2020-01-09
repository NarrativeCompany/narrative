package org.narrative.network.customizations.narrative.security;

import org.narrative.network.shared.security.AccessViolation;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

public class DataVisibilityAccessViolation extends AccessViolation {
    public DataVisibilityAccessViolation(String titleKey, String messageKey) {
        super(wordlet(titleKey), wordlet(messageKey));
    }
}
