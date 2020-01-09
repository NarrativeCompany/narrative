package org.narrative.network.core.content.base;

import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.composition.base.CompositionDetail;
import org.jetbrains.annotations.NotNull;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * Date: Dec 1, 2005
 * Time: 10:31:47 AM
 *
 * @author Brian
 */
@MappedSuperclass
public abstract class ContentConsumer extends CompositionDetail {

    /**
     * @deprecated for hibernate use only
     */
    protected ContentConsumer() {}

    public ContentConsumer(Composition composition) {
        super(composition);
    }

    @Transient
    @NotNull
    public abstract ContentType getContentType();

}