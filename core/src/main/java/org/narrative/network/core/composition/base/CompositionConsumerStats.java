package org.narrative.network.core.composition.base;

/**
 * Date: 9/27/11
 * Time: 3:19 PM
 *
 * @author brian
 */
public interface CompositionConsumerStats {
    public int getReplyCount();

    public void syncStats(CompositionStats compositionStats);

    public int getLiveReplyCount();
}
