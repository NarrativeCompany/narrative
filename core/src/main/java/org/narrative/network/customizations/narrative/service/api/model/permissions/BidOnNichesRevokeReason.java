package org.narrative.network.customizations.narrative.service.api.model.permissions;

/**
 * Date: 10/17/18
 * Time: 5:30 PM
 *
 * @author brian
 */
public enum BidOnNichesRevokeReason implements RevokeReason {
    NICHE_SLOTS_FULL
    ,SECURITY_DEPOSIT_REQUIRED
    ;

    public boolean isNicheSlotsFull() {
        return this==NICHE_SLOTS_FULL;
    }

    public boolean isSecurityDepositRequired() {
        return this==SECURITY_DEPOSIT_REQUIRED;
    }
}
