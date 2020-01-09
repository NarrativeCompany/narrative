package org.narrative.network.core.search;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.composition.base.CompositionConsumer;
import org.narrative.network.shared.security.PrimaryRole;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2/23/11
 * Time: 4:59 PM
 *
 * @author brian
 */
public abstract class MessageSearchResultImpl extends SearchResultImpl implements MessageSearchResult {
    private final OID compositionConsumerOid;
    private CompositionConsumer compositionConsumer;

    public MessageSearchResultImpl(OID oid, int resultIndex, OID compositionConsumerOid) {
        super(oid, resultIndex);
        this.compositionConsumerOid = compositionConsumerOid;
    }

    @Override
    public OID getCompositionConsumerOid() {
        return compositionConsumerOid;
    }

    @Override
    public CompositionConsumer getCompositionConsumer() {
        assert isHasSetData() : "Should only get CompositionConsumer once it's already been set!";
        return compositionConsumer;
    }

    public void setCompositionConsumer(CompositionConsumer consumer) {
        assert isEqual(consumer.getOid(), compositionConsumerOid) : "CompositionConsumer OID mismatch when setting Reply data!";
        this.compositionConsumer = consumer;
        setHasSetData(true);
    }

    @Override
    public boolean veto(PrimaryRole primaryRole) {
        return !exists(getCompositionConsumer()) || getCompositionConsumer().isVetoSearchResult(primaryRole);
    }
}
