package org.narrative.network.customizations.narrative.posts.dao;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.core.content.base.ContentStatus;
import org.narrative.network.core.moderation.ModerationStatus;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.channels.ChannelType;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.niche.NicheStatus;
import org.narrative.network.customizations.narrative.posts.ChannelContent;
import org.narrative.network.customizations.narrative.posts.NarrativePostStatus;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.sql.Date;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-01-03
 * Time: 12:11
 *
 * @author jonmark
 */
public class ChannelContentDAO extends GlobalDAOImpl<ChannelContent, OID> {
    public ChannelContentDAO() {
        super(ChannelContent.class);
    }

    public List<Niche> getNichesMostPostedToByUser(AreaUserRlm areaUserRlm, Instant postedAfter, int count) {
        assert exists(areaUserRlm) : "Should never call this method without specifying an author!";

        if (postedAfter==null) {
            postedAfter = Instant.ofEpochMilli(0);
        }

        return getGSession().getNamedQuery("channelContent.getNichesMostPostedToByUser")
                .setParameter("author", areaUserRlm)
                .setParameter("postedAfter", new Date(postedAfter.toEpochMilli()))
                .setParameter("nicheChannelType", ChannelType.NICHE)
                .setParameter("activeNicheStatus", NicheStatus.ACTIVE)
                .setMaxResults(count)
                .list();
    }

    public List<ObjectPair<Niche,Number>> getNichesMostPostedTo(int count) {
        return getGSession().getNamedQuery("channelContent.getNichesMostPostedTo")
                .setParameter("approvedStatus", NarrativePostStatus.APPROVED)
                .setParameter("nicheChannelType", ChannelType.NICHE)
                .setParameter("activeNicheStatus", NicheStatus.ACTIVE)
                .setParameter("contentStatus", ContentStatus.ACTIVE.getBitmaskType())
                .setParameter("moderatedStatus", ModerationStatus.MODERATED)
                .setMaxResults(count)
                .list();
    }

    public List<ObjectPair<Niche,Number>> getNichesMostPostedToInChannel(Channel channel, int count) {
        return getGSession().getNamedQuery("channelContent.getNichesMostPostedToInChannel")
                .setParameter("channelOid", channel.getOid())
                .setParameter("approvedStatus", NarrativePostStatus.APPROVED)
                .setParameter("nicheChannelType", ChannelType.NICHE)
                .setParameter("activeNicheStatus", NicheStatus.ACTIVE)
                .setParameter("contentStatus", ContentStatus.ACTIVE.getBitmaskType())
                .setParameter("moderatedStatus", ModerationStatus.MODERATED)
                .setMaxResults(count)
                .list();
    }

    public List<OID> getContentOidsInChannelByStatuses(Channel channel, Collection<NarrativePostStatus> statuses) {
        return getGSession().getNamedQuery("channelContent.getContentOidsInChannelByStatuses")
                .setParameter("channel", channel)
                .setParameterList("statuses", statuses)
                .list();
    }

    public int removePostsFromChannelByStatuses(Channel channel, Collection<NarrativePostStatus> statuses) {
        return getGSession().getNamedQuery("channelContent.removePostsFromChannelByStatuses")
                .setParameter("channel", channel)
                .setParameterList("statuses", statuses)
                .executeUpdate();
    }

    public Map<OID,Number> getPostCountsByChannelOidForAuthor(ChannelType channelType, AreaUserRlm areaUserRlm, Collection<NarrativePostStatus> statuses) {
        List<ObjectPair<OID,Number>> pairs = getGSession().createNamedQuery("channelContent.getPostCountsByChannelOidForAuthor", (Class<ObjectPair<OID,Number>>)(Class)ObjectPair.class)
                .setParameter("channelType", channelType)
                .setParameter("areaUserRlm", areaUserRlm)
                .setParameterList("statuses", statuses)
                .setParameter("activeContentStatus", ContentStatus.ACTIVE.getBitmaskType())
                .list();
        return ObjectPair.getAsMap(pairs);
    }
}
