package org.narrative.network.customizations.narrative.niches.referendum.services;

import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.customizations.narrative.niches.services.SendSingleNarrativeEmailTaskBase;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 3/6/18
 * Time: 1:53 PM
 */
public class SendReferendumResultToOwnerEmail extends SendSingleNarrativeEmailTaskBase {
    private Referendum referendum;
    private final boolean passed;

    public SendReferendumResultToOwnerEmail(Referendum referendum, boolean passed, User owner) {
        super(owner);
        this.referendum = referendum;
        this.passed = passed;
    }

    public Referendum getReferendum() {
        return referendum;
    }

    public boolean isPassed() {
        return passed;
    }
}
