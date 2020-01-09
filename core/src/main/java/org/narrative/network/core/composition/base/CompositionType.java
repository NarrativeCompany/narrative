package org.narrative.network.core.composition.base;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.OID;
import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.content.base.ContentStats;
import org.narrative.network.core.moderation.ModeratableStats;
import org.narrative.network.core.search.ReplyIndexHandler;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;

import java.util.Collections;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * User: barry
 * Date: Mar 20, 2009
 * Time: 10:07:57 AM
 */
public enum CompositionType implements IntegerEnum {
    CONTENT(0, Content.class, ContentStats.class),
    REFERENDUM(6, Referendum.class, null);

    private final int id;
    private final Class<? extends CompositionConsumer> compositionConsumerClass;
    private final Class<? extends CompositionConsumerStats> compositionConsumerStatsClass;

    CompositionType(int id, Class<? extends CompositionConsumer> compositionConsumerClass, Class<? extends CompositionConsumerStats> compositionConsumerStatsClass) {
        this.id = id;
        assert compositionConsumerClass != null : "All CompositionTypes should have a compositionConsumerClass now!";
        this.compositionConsumerClass = compositionConsumerClass;
        this.compositionConsumerStatsClass = compositionConsumerStatsClass;
    }

    public static final String REPLY_PARAM_NAME = "reply";
    public static final String COMMENT_PARAM_NAME = "comment";

    public static final Set<CompositionType> ALL_SEARCHABLE_REPLY_TYPES = Collections.singleton(CompositionType.CONTENT);

    @Override
    public int getId() {
        return id;
    }

    public Class<? extends CompositionConsumer> getCompositionConsumerClass() {
        return compositionConsumerClass;
    }

    public NetworkDAOImpl<CompositionConsumer, OID> getDAO() {
        // bl: this is a little bit funky, but that's life.  since CompositionConsumer doesn't/can't
        // implement DAOObject (since it doesn't have an associated DAO), we need to use an unparameterized
        // call to getDAO to get the generic DAO.
        // nb. just like we do in ContentType.getDAO().
        return (NetworkDAOImpl<CompositionConsumer, OID>) DAOImpl.getDAO(getCompositionConsumerClass());
    }

    public boolean isSupportsStats() {
        return compositionConsumerStatsClass != null;
    }

    public boolean isSupportsModeration() {
        return isSupportsStats() && ModeratableStats.class.isAssignableFrom(compositionConsumerStatsClass);
    }

    public String getNameForDisplay() {
        assert !isContent() : "Should get wording from ContentType!";
        return wordlet(getNameForDisplayWordletKey());
    }

    public String getNameForDisplayWordletKey() {
        assert !isContent() : "Should get wording from ContentType!";
        return newString("compositionType.", this);
    }

    public String getNameForDisplayPlural() {
        assert !isContent() : "Should get plural wording from ContentType!";
        return wordlet("compositionType.plural." + this);
    }

    public String getNameForDisplayLowercase() {
        assert !isContent() : "Should get lowercase wording from ContentType!";
        return wordlet("compositionType.lc." + this);
    }

    public String getDeletedErrorMessage() {
        return wordlet("error.deleted." + this);
    }

    public TermQuery getTermQuery() {
        return new TermQuery(new Term(ReplyIndexHandler.FIELD_COMPOSITION_TYPE, Integer.toString(getId())));
    }

    public boolean isContent() {
        return this == CONTENT;
    }

    public boolean isReferendum() {
        return this == REFERENDUM;
    }

}
