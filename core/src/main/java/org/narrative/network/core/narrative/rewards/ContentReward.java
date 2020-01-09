package org.narrative.network.core.narrative.rewards;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.narrative.rewards.dao.ContentRewardDAO;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

/**
 * Date: 2019-05-16
 * Time: 08:15
 *
 * @author jonmark
 */
@Getter
@Setter
@Entity
@Proxy
@FieldNameConstants
@NoArgsConstructor
@Table(uniqueConstraints = {
        // jw: content should only be awarded once within a period
        @UniqueConstraint(name = "contentReward_contentOid_period_uidx", columnNames = {ContentReward.FIELD__CONTENT_OID__COLUMN, ContentReward.FIELD__PERIOD__COLUMN})
})
public class ContentReward implements DAOObject<ContentRewardDAO>, RewardPeriodRef {
    public static final String FIELD__CONTENT_OID__NAME = "contentOid";
    public static final String FIELD__CONTENT_OID__COLUMN = FIELD__CONTENT_OID__NAME;

    // jw: since we will be inserting the records into this table via sql from ItemHourTrendingStats, let's just use an auto-increment PK:
    // https://thoughts-on-java.org/hibernate-tips-use-auto-incremented-column-primary-key/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = Fields.oid, updatable = false, nullable = false, insertable = false)
    private OID oid;

    @NotNull
    private OID contentOid;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_contentReward_user")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_contentReward_period")
    private RewardPeriod period;

    private long points;

    @ManyToOne
    @ForeignKey(name = "fk_contentReward_publicationReward")
    private PublicationReward publicationReward;

    public static ContentRewardDAO dao() {
        return NetworkDAOImpl.getDAO(ContentReward.class);
    }
}
