package org.narrative.network.core.content.base;

import org.narrative.network.core.composition.base.Composition;

import javax.persistence.MappedSuperclass;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Feb 10, 2006
 * Time: 1:23:38 PM
 */
@MappedSuperclass
public abstract class ContentWithAttachmentsConsumer extends ContentConsumer {

    @Deprecated
    public ContentWithAttachmentsConsumer() {
        super(null);
    }

    public ContentWithAttachmentsConsumer(Composition composition) {
        super(composition);
    }
}