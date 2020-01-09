package org.narrative.network.customizations.narrative.niches.niche;

import org.narrative.network.customizations.narrative.channels.ChannelRole;

/**
 * Date: 2019-07-31
 * Time: 08:01
 *
 * @author jonmark
 */
public enum NicheRole implements ChannelRole {
    MODERATOR(0)
    ;

    private final int id;

    NicheRole(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }
}