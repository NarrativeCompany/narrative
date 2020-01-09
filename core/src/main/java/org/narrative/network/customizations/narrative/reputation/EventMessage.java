package org.narrative.network.customizations.narrative.reputation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.HibernateInstantType;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.customizations.narrative.reputation.dao.EventMessageDAO;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;
import org.narrative.shared.event.Event;
import org.narrative.shared.event.reputation.ReputationEvent;
import org.narrative.shared.redisson.codec.FinalClassFriendlyJsonJacksonCodec;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Date: 2018-12-11
 * Time: 12:48
 *
 * @author jonmark
 */
@Entity
@Proxy
@Getter
@Setter
public class EventMessage implements DAOObject<EventMessageDAO> {

    private static final ObjectMapper serializationMapper = new FinalClassFriendlyJsonJacksonCodec().getObjectMapper();

    @Id
    @Column(columnDefinition = "binary(16)")
    private UUID eventId;

    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    private EventMessageStatus status;

    @NotNull
    @Column(columnDefinition = "mediumtext")
    private String eventJson;

    @NotNull
    @Type(type = HibernateInstantType.TYPE)
    private Instant creationDatetime;

    @Type(type = HibernateInstantType.TYPE)
    private Instant sendDatetime;

    @Type(type = HibernateInstantType.TYPE)
    private Instant lastSentDatetime;

    private int sendCount;
    private int failCount;
    private int retryCount;

    /**
     * @deprecated for hibernate use only
     */
    public EventMessage() {}

    public EventMessage(Event event) {
        // jw: let's use the same PK as the event.
        this.eventId = event.getEventId();
        // jw: easy enough, we just created this object.
        this.creationDatetime = Instant.now();
        // jw: let's queue this for sending right now.
        this.status = EventMessageStatus.QUEUED;
        this.sendDatetime = Instant.now();
        // jw: let's serialize the event into a string so that it can be read out on the other side. After this point,
        //     we should have no reason to deserialize this object ourselves.
        try {
            // jw: using the JsonJacksonCodecs object mapper so that we are consistent with Redis
            this.eventJson = serializationMapper.writeValueAsString(event);
        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Failed creating json from event/" + event.getClass().getSimpleName(), e);
        }
    }

    @Transient
    @Override
    @Deprecated
    public OID getOid() {
        throw UnexpectedError.getRuntimeException("Should never attempt to get OID for ReputationEvent!");
    }

    @Override
    public void setOid(OID oid) {
        throw UnexpectedError.getRuntimeException("Should never attempt to set OID for ReputationEvent!");
    }

    private transient Event event;

    @Transient
    public Event getEvent() {
        if (event == null) {
            try {
                // jw: using the JsonJacksonCodecs object mapper so that we are consistent with Redis
                event = serializationMapper.readValue(getEventJson(), ReputationEvent.class);
            } catch (IOException e) {
                throw UnexpectedError.getRuntimeException("Failed creating Event from json/" + getEventJson(), e);
            }
        }

        return event;
    }

    public void requeueWithExponentialBackoff(int originalCount) {
        // jw: if we shift with the original count then the multiplier will be accurate:
        //     First: 1 << 0 = 1
        //     Second: 1 << 1 = 2
        //     Third: 1 << 2 = 4
        //     and so on. at 10 this should max out at 512
        int multiplier = 1 << originalCount;
        setSendDatetime(Instant.now().plus(multiplier, ChronoUnit.MINUTES));
        setStatus(EventMessageStatus.QUEUED);
    }

    public static EventMessageDAO dao() {
        return NetworkDAOImpl.getDAO(EventMessage.class);
    }
}
