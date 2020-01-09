package org.narrative.network.core.moderation;

import org.narrative.network.core.composition.base.CompositionConsumerStats;

/**
 * Date: 9/30/11
 * Time: 1:58 PM
 *
 * @author brian
 */
public interface ModeratableStats extends CompositionConsumerStats {
    public int getReportCount();

    public void addModeratedReply();

    public void removeModeratedReply();

    public void addReport();
}
