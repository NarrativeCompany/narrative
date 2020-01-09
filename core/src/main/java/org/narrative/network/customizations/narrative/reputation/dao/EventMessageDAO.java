package org.narrative.network.customizations.narrative.reputation.dao;

import org.narrative.network.customizations.narrative.reputation.EventMessage;
import org.narrative.network.customizations.narrative.reputation.EventMessageStatus;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Date: 2018-12-11
 * Time: 12:54
 *
 * @author jonmark
 */
public class EventMessageDAO extends GlobalDAOImpl<EventMessage, UUID> {
    public EventMessageDAO() {
        super(EventMessage.class);
    }

    public List<EventMessage> getQueuedEventMessages(int maxResults) {
        return getGSession().getNamedQuery("eventMessage.getQueuedEventMessages")
                .setParameter("queuedStatus", EventMessageStatus.QUEUED)
                .setParameter("sendDatetime", Instant.now())
                .setMaxResults(maxResults)
                .list();
    }

    public List<EventMessage> getStalledEventMessages(int maxResults) {
        return getGSession().getNamedQuery("eventMessage.getStalledEventMessages")
                .setParameter("sentStatus", EventMessageStatus.SENT)
                .setParameter("lastSentDatetimeCutoff", Instant.now().minus(5, ChronoUnit.MINUTES))
                .setMaxResults(maxResults)
                .list();
    }
}
