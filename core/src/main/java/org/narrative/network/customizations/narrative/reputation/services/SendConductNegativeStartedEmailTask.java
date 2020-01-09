package org.narrative.network.customizations.narrative.reputation.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.services.SendBulkNarrativeEmailTaskBase;
import org.narrative.shared.event.reputation.ConductEventType;

import java.util.Date;
import java.util.List;

public class SendConductNegativeStartedEmailTask extends SendBulkNarrativeEmailTaskBase {
    private final List<OID> usersStartingConductNegative;
    private final ConductEventType conductEventType;

    SendConductNegativeStartedEmailTask(List<OID> usersStartingConductNegative, ConductEventType conductEventType) {
        this.usersStartingConductNegative = usersStartingConductNegative;
        this.conductEventType = conductEventType;
    }

    @Override
    protected List<OID> getUserOidsToNotify() {
        return usersStartingConductNegative;
    }

    public ConductEventType getConductEventType() {
        return conductEventType;
    }

    public Date getConductNegativeExpirationDatetime() {
        // bl: make sure we get the reputation without cache so that we have the latest date from the reputation db.
        return Date.from(getUser().getReputationWithoutCache().getNegativeConductExpirationTimestamp());
    }

    @Override
    protected void setupForChunk(List<User> users) {

    }

    @Override
    public boolean isAlwaysSendEmail() {
        return true;
    }
}
