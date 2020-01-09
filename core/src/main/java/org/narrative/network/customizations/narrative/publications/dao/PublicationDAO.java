package org.narrative.network.customizations.narrative.publications.dao;

import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.persistence.ObjectQuadruplet;
import org.narrative.network.core.area.base.AreaRlm;
import org.narrative.network.core.area.portfolio.Portfolio;
import org.narrative.network.core.content.base.ContentType;
import org.narrative.network.core.content.base.SEOObjectDAO;
import org.narrative.network.core.content.base.services.CreateContentTask;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.ChannelUser;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.PublicationStatus;
import org.narrative.network.customizations.narrative.service.api.model.FollowScrollParamsDTO;
import org.narrative.network.shared.daobase.GlobalDAOImpl;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;

import java.time.Instant;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-07-23
 * Time: 10:11
 *
 * @author jonmark
 */
public class PublicationDAO extends GlobalDAOImpl<Publication, OID> implements SEOObjectDAO<Publication> {
    public PublicationDAO() {
        super(Publication.class);
    }

    @Override
    public Publication getForPrettyURLString(AreaRlm areaRlm, Portfolio portfolio, ContentType contentType, String prettyUrlString) {
        // jw: unlike most of these methods the only thing we need to be concerned with is the prettyUrlString, since publication
        //     prettyUrlStrings are unique across the entire environment.
        return getUniqueBy(new NameValuePair<>(Publication.Fields.prettyUrlString, prettyUrlString));
    }

    public Publication getForPrettyURLString(String prettyUrlString) {
        return getForPrettyURLString(null, null, null, prettyUrlString);
    }

    public String getAvailablePrettyUrlString(String name) {
        return CreateContentTask.getPrettyUrlStringValue(this, null, null, null, name);
    }

    public ScrollableResults getAllOidsAndAreaOids() {
        return getGSession().getNamedQuery("publication.getAllOidsAndAreaOids")
                .scroll(ScrollMode.FORWARD_ONLY);
    }

    public List<ObjectQuadruplet<OID, String, String, Instant>> getIndexRecordChunked(OID lastOid, int chunkSize) {
        return getGSession().createNamedQuery("publication.getIndexRecordChunked", (Class<ObjectQuadruplet<OID, String, String, Instant>>)(Class)ObjectQuadruplet.class)
                .setParameter("lastOid", lastOid)
                .setParameter("activeStatus", PublicationStatus.ACTIVE)
                .setMaxResults(chunkSize)
                .list();
    }

    public int getCountOwnedPublications(User user) {
        Number count = getGSession().createNamedQuery("publication.getCountOwnedPublications", Number.class)
                .setParameter("user", user)
                .setParameter("activeStatus", PublicationStatus.ACTIVE)
                .uniqueResult();
        return count==null ? 0 : count.intValue();
    }

    public List<Publication> getOwnedPublications(User user) {
        return getGSession().createNamedQuery("publication.getOwnedPublications", Publication.class)
                .setParameter("user", user)
                .setParameter("activeStatus", PublicationStatus.ACTIVE)
                .list();
    }

    public List<Publication> getPublicationsFollowedByUser(User follower, FollowScrollParamsDTO params, int maxResults) {
        assert exists(follower) : "Expect to get a user at this point!";

        return getGSession().createNamedQuery("publication.getPublicationsFollowedByUser", Publication.class)
                .setParameter("follower", follower)
                .setParameter("lastName", params == null ? null : params.getLastItemName())
                .setParameter("lastOid", params == null ? null : params.getLastItemOid())
                .setMaxResults(maxResults)
                .list();
    }

    public List<ObjectPair<Publication, ChannelUser>> getAssociatedPublications(User user) {
        return getGSession().createNamedQuery("publication.getAssociatedPublications", (Class<ObjectPair<Publication, ChannelUser>>)(Class) ObjectPair.class)
                .setParameter("user", user)
                .setParameter("userOid", user.getOid())
                .setParameter("activeStatus", PublicationStatus.ACTIVE)
                .list();
    }

    public List<OID> getDeletablePublicationOids() {
        return getGSession().createNamedQuery("publication.getDeletablePublicationOids", OID.class)
                .setParameter("endedBefore", Instant.now().minus(Publication.NON_ACTIVE_DURATION))
                .list();
    }
}
