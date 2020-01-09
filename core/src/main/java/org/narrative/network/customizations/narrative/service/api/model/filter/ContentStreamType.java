package org.narrative.network.customizations.narrative.service.api.model.filter;

/**
 * Date: 2019-04-04
 * Time: 22:32
 *
 * @author brian
 */
public enum ContentStreamType {
    PERSONALIZED,
    CHANNEL,
    NETWORK_WIDE
    ;

    public boolean isPersonalized() {
        return this==PERSONALIZED;
    }

    public boolean isNetworkWide() {
        return this==NETWORK_WIDE;
    }
}
