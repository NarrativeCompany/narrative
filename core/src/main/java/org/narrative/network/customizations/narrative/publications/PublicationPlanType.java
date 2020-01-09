package org.narrative.network.customizations.narrative.publications;

import org.narrative.common.util.enums.*;

import java.math.BigDecimal;

/**
 * Date: 2019-07-23
 * Time: 10:24
 *
 * @author jonmark
 */
public enum PublicationPlanType implements IntegerEnum {
    BASIC(0, BigDecimal.valueOf(125), 2, 5, false)
    ,BUSINESS(1, BigDecimal.valueOf(299), 10, 30, true)
    ;

    private final int id;
    private final BigDecimal price;
    private final int maxEditors;
    private final int maxWriters;
    private final boolean supportsCustomDomain;

    PublicationPlanType(int id, BigDecimal price, int maxEditors, int maxWriters, boolean supportsCustomDomain) {
        this.id = id;
        this.price = price;
        this.maxEditors = maxEditors;
        this.maxWriters = maxWriters;
        this.supportsCustomDomain = supportsCustomDomain;
    }

    @Override
    public int getId() {
        return id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getMaxEditors() {
        return maxEditors;
    }

    public int getMaxWriters() {
        return maxWriters;
    }

    public boolean isSupportsCustomDomain() {
        return supportsCustomDomain;
    }

    public boolean isAvailableToPublication(Publication publication) {
        // jw: regardless of role counts, let's always support the last enum plan (the most expensive one)
        if (this == values()[values().length - 1]) {
            return true;
        }

        // jw: secondly, the current plan on a Publication should always be available to them.
        if (this == publication.getPlan()) {
            return true;
        }

        // jw: otherwise, the publication must be below the minimums
        if (publication.getUserCountForRole(PublicationRole.EDITOR) > getMaxEditors()) {
            return false;
        }

        if (publication.getUserCountForRole(PublicationRole.WRITER) > getMaxWriters()) {
            return false;
        }

        // jw: since they fit within the plan limits I guess they can use this plan!
        return true;
    }

    public boolean isUpgrade(PublicationPlanType plan) {
        // jw: the provided plan is an upgrade if it is worth more than this plan.
        return plan.getPrice().compareTo(getPrice()) > 0;
    }
}