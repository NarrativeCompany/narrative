package org.narrative.network.customizations.narrative.reputation.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.services.SendBulkNarrativeEmailTaskBase;

import java.util.List;

public class SendConductNegativeEndedEmailTask extends SendBulkNarrativeEmailTaskBase {
    private final List<OID> usersEndingConductNegative;

    SendConductNegativeEndedEmailTask(List<OID> usersEndingConductNegative) {
        this.usersEndingConductNegative = usersEndingConductNegative;
    }

    @Override
    protected List<OID> getUserOidsToNotify() {
        return usersEndingConductNegative;
    }

    @Override
    protected void setupForChunk(List<User> users) {

    }

    @Override
    public boolean isAlwaysSendEmail() {
        return true;
    }
}
