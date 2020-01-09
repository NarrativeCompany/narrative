package org.narrative.network.customizations.narrative.service.impl.comment;

import org.narrative.network.core.composition.base.CompositionConsumer;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

/**
 * Date: 2019-02-14
 * Time: 12:21
 *
 * @author jonmark
 */
public abstract class CommentTaskBase<T> extends AreaTaskImpl<T> {
    protected final CompositionConsumer consumer;
    protected final Reply reply;

    protected CommentTaskBase(CompositionConsumer consumer, Reply reply) {
        this.consumer = consumer;
        this.reply = reply;
    }
}
