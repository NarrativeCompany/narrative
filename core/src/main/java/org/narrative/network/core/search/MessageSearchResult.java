package org.narrative.network.core.search;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.composition.base.CompositionConsumer;
import org.narrative.network.core.composition.base.CompositionType;
import org.narrative.network.core.user.User;

import java.sql.Timestamp;

/**
 * Date: Nov 2, 2009
 * Time: 3:01:50 PM
 *
 * @author Steven Benitez
 */
public interface MessageSearchResult extends SearchResult {
    public User getAuthor();

    public OID getCompositionConsumerOid();

    public void setCompositionConsumer(CompositionConsumer consumer);

    public CompositionConsumer getCompositionConsumer();

    public CompositionType getCompositionType();

    public Timestamp getLiveDatetime();
}
