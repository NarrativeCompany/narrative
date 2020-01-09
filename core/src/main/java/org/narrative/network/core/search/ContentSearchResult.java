package org.narrative.network.core.search;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.composition.base.CompositionType;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.user.User;

import java.sql.Timestamp;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: May 19, 2006
 * Time: 6:06:19 PM
 */
public class ContentSearchResult extends MessageSearchResultImpl {
    private final Timestamp liveDatetime;

    public ContentSearchResult(OID contentOid, int resultIndex, Timestamp liveDatetime) {
        super(contentOid, resultIndex, contentOid);
        this.liveDatetime = liveDatetime;
    }

    public Timestamp getLiveDatetime() {
        return liveDatetime;
    }

    @Override
    public Content getAuthorProvider() {
        return (Content) getCompositionConsumer();
    }

    @Override
    public User getAuthor() {
        return getCompositionConsumer().getAuthor();
    }

    @Override
    public IndexType getIndexType() {
        return IndexType.CONTENT;
    }

    @Override
    public CompositionType getCompositionType() {
        return CompositionType.CONTENT;
    }

}
