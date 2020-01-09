package org.narrative.network.core.user;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.network.core.user.dao.UserKycEventDAO;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import java.time.Instant;

@Data
@FieldNameConstants
@ToString(exclude = {"userKyc"})
@EqualsAndHashCode(exclude = {"userKyc"})
@Entity
public class UserKycEvent implements DAOObject<UserKycEventDAO> {
    public static final String FIELD__USERKYC__NAME = "userKyc";
    public static final String FIELD__CREATED__NAME = "created";

    private UserKycEvent() {
    }

    @Builder
    public UserKycEvent(UserKyc userKyc, @NotNull UserKycEventType type, @NotNull String actorDisplayName, String note) {
        this.userKyc = userKyc;
        this.type = type;
        this.actorDisplayName = actorDisplayName;
        this.note = note;
    }

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    private OID oid;

    @ManyToOne(optional=false)
    @ForeignKey(name = "fk_userKycEvent_userKyc")
    private UserKyc userKyc;

    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    private UserKycEventType type;

    @NotNull
    private String actorDisplayName;

    @CreationTimestamp
    private Instant created;

    private String note;

    public static UserKycEventDAO dao() {
        return NetworkDAOImpl.getDAO(UserKycEvent.class);
    }
}
