package org.narrative.network.core.content.base.dao;

import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.network.core.area.base.AreaRlm;
import org.narrative.network.core.area.portfolio.Portfolio;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.content.base.ContentStatus;
import org.narrative.network.core.content.base.ContentType;
import org.narrative.network.core.content.base.SEOObjectDAO;
import org.narrative.network.core.moderation.ModerationStatus;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Dec 2, 2005
 * Time: 3:13:48 PM
 *
 * @author Brian
 */
public class ContentDAO extends GlobalDAOImpl<Content, OID> implements SEOObjectDAO<Content> {
    public ContentDAO() {
        super(Content.class);
    }

    public List<ObjectPair<OID, OID>> getAllOids() {
        return getGSession().getNamedQuery("content.getAllOids").list();
    }

    public List<Content> getAllLiveInOids(List<OID> contentOids) {
        return getGSession().getNamedQuery("content.getAllLiveInOids").setParameterList("contentOids", contentOids).list();
    }

    @Override
    public Content getForPrettyURLString(AreaRlm areaRlm, Portfolio portfolio, ContentType contentType, String prettyUrlString) {
        return getForPrettyURLString(portfolio, contentType, prettyUrlString);
    }

    public Content getForPrettyURLString(Portfolio portfolio, ContentType contentType, String prettyUrlString) {
        return getUniqueBy(newNameValuePair(Content.FIELD__PORTFOLIO__NAME, portfolio), newNameValuePair(Content.FIELD__CONTENT_TYPE__NAME, contentType), newNameValuePair(Content.FIELD__PRETTY_URL_STRING__NAME, prettyUrlString));
    }

    public List<OID> getEmptyNarrativePostDraftOids(Instant savedBefore, Instant savedAfter) {
        assert savedBefore != null && savedBefore.isBefore(Instant.now()) : "Specified invalid savedBefore/"+savedBefore;
        assert savedAfter != null && savedAfter.isBefore(savedBefore) : "Specified invalid savedAfter/"+savedAfter+" -> "+savedBefore;

        return getGSession().getNamedQuery("content.getEmptyNarrativePostDraftOids")
                .setParameter("narrativePostType", ContentType.NARRATIVE_POST)
                .setParameter("savedBefore", new Date(savedBefore.toEpochMilli()))
                .setParameter("savedAfter", new Date(savedAfter.toEpochMilli()))
                .list();
    }

    public long getCountAllLiveContent(ContentType contentType) {
        return getCountForAllBy(new NameValuePair<>(Content.FIELD__CONTENT_TYPE__NAME, contentType), new NameValuePair<>(Content.FIELD__CONTENT_STATUS__NAME, ContentStatus.ACTIVE.getBitmaskType()));
    }

    public long getCountCreatedByUserAfter(User user, ContentType contentType, Instant after) {
        return getGSession().createNamedQuery("content.getCountCreatedByUserAfter", Number.class)
                .setParameter("author", AreaUser.getAreaUserRlm(user.getLoneAreaUser()))
                .setParameter("activeStatus", ContentStatus.ACTIVE.getBitmaskType())
                .setParameter("contentType", contentType)
                .setParameter("after", new Date(after.toEpochMilli()))
                .uniqueResult().longValue();
    }

    public List<Content> getAllCreatedByUser(User user, ContentType contentType) {
        return getGSession().createNamedQuery("content.getAllCreatedByUser", Content.class)
                .setParameter("author", AreaUser.getAreaUserRlm(user.getLoneAreaUser()))
                .setParameter("contentType", contentType)
                .list();
    }

    public List<Content> getNonActiveContentFromOidList(List<OID> contentOids) {
        if (isEmptyOrNull(contentOids)) {
            return Collections.emptyList();
        }

        return getGSession().createNamedQuery("content.getNonActiveContentFromOidList", Content.class)
                .setParameter("activeStatus", ContentStatus.ACTIVE.getBitmaskType())
                .setParameter("moderatedModerationStatus", ModerationStatus.MODERATED)
                .setParameterList("contentOids", contentOids)
                .list();
    }
}
