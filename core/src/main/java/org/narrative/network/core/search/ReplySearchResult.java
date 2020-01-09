package org.narrative.network.core.search;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectQuadruplet;
import org.narrative.common.util.enums.EnumRegistry;
import org.narrative.network.core.composition.base.CompositionType;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.core.composition.base.ReplyAuthorProvider;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.security.PrimaryRole;
import org.narrative.network.shared.services.AuthorProvider;

import java.sql.Timestamp;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Sep 23, 2009
 * Time: 3:16:20 PM
 *
 * @author brian
 */
public class ReplySearchResult extends MessageSearchResultImpl implements ReplyAuthorProvider {
    private final OID compositionPartitionOid;
    private final CompositionType compositionType;
    private final Timestamp liveDatetime;

    private Reply reply;
    private boolean isHasSetReply;

    public ReplySearchResult(OID replyOid, int resultIndex, OID compositionConsumerOid, CompositionType compositionType, OID compositionPartitionOid, Timestamp liveDatetime) {
        super(replyOid, resultIndex, compositionConsumerOid);
        this.compositionPartitionOid = compositionPartitionOid;
        this.compositionType = compositionType;
        this.liveDatetime = liveDatetime;
    }

    public OID getCompositionPartitionOid() {
        return compositionPartitionOid;
    }

    public CompositionType getCompositionType() {
        return compositionType;
    }

    public Timestamp getLiveDatetime() {
        return liveDatetime;
    }

    @Override
    public AuthorProvider getAuthorProvider() {
        return getReply();
    }

    public Reply getReply() {
        assert isHasSetReply : "Should have already set the Reply before attempting to get it!";
        return reply;
    }

    public void setReply(Reply reply) {
        assert isEqual(reply.getOid(), getOid()) : "Reply OID mismatch when setting Reply data!";
        this.reply = reply;
        isHasSetReply = true;
    }

    public boolean isHasSetReply() {
        return isHasSetReply;
    }

    @Override
    public String getGuestNameResolved() {
        return getReply().getGuestNameResolved();
    }

    @Override
    public PrimaryRole getPrimaryRole() {
        return getReply().getPrimaryRole();
    }

    @Override
    public PrimaryRole getRealAuthorPrimaryRole() {
        return getReply().getRealAuthorPrimaryRole();
    }

    @Override
    public User getAuthor() {
        return getUser();
    }

    public User getUser() {
        return getReply().getUser();
    }

    @Override
    public User getRealAuthor() {
        return getReply().getRealAuthor();
    }

    @Override
    public IndexType getIndexType() {
        return IndexType.REPLY;
    }

    public String getFormId() {
        StringBuilder id = new StringBuilder();
        id.append(getCompositionPartitionOid().toString());
        id.append(".");
        id.append(getOid().toString());

        return id.toString();
    }

    @Override
    public boolean isValidSearchResult() {
        return super.isValidSearchResult() && isHasSetReply();
    }

    @Override
    public boolean veto(PrimaryRole primaryRole) {
        return super.veto(primaryRole) || !exists(getReply()) || (exists(getCompositionConsumer()) && !getCompositionConsumer().hasViewRepliesRight(primaryRole.getAreaRoleForArea(getReply().getArea())));
    }

    @Override
    public Object getIdForManageContent() {
        // ${replyResult.compositionPartitionOid}_${replyResult.oid}_${consumer.compositionType.id}_${consumer.oid}
        return newString(getCompositionPartitionOid(), "_", getOid(), "_", getCompositionType().getId(), "_", getCompositionConsumerOid());
    }

    public static ObjectQuadruplet<OID, OID, CompositionType, OID> getDataFromIdForManageContent(String replyId) {
        if (isEmpty(replyId)) {
            return null;
        }
        String[] replyData = replyId.split("\\_");
        if (replyData.length != 4) {
            return null;
        }
        OID partitionOid = OID.getOIDFromString(replyData[0]);
        OID replyOid = OID.getOIDFromString(replyData[1]);
        CompositionType compositionType = EnumRegistry.getForId(CompositionType.class, Integer.parseInt(replyData[2]));
        OID compositionOid = OID.getOIDFromString(replyData[3]);

        return newObjectQuadruplet(partitionOid, replyOid, compositionType, compositionOid);
    }

}