package org.narrative.network.core.rating.model;

import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.core.rating.QualityRating;
import org.narrative.network.core.rating.dao.UserQualityRatedReplyDAO;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@FieldNameConstants
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "userQualityRatedReply_reply_userOid_uidx", columnNames = {UserQualityRatedReply.FIELD__REPLY__COLUMN, UserQualityRatedReply.FIELD__USER_OID__COLUMN})
})
public class UserQualityRatedReply extends UserQualityRatedObject<UserQualityRatedReplyDAO> {
    public static final String FIELD__REPLY__NAME = "reply";
    public static final String FIELD__REPLY__COLUMN = FIELD__REPLY__NAME+"_"+Reply.FIELD__OID__NAME;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ForeignKey(name = "fk_userQualityRatedReply_reply")
    private Reply reply;

    public UserQualityRatedReply(User user, QualityRating qualityRating, Reply reply) {
        super(user, qualityRating);
        this.reply = reply;
    }

    public static UserQualityRatedReplyDAO dao() {
        return NetworkDAOImpl.getDAO(UserQualityRatedReply.class);
    }
}
