package org.narrative.network.core.narrative.rewards;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.narrative.rewards.dao.PublicationRewardDAO;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.PublicationContentRewardRecipientType;
import org.narrative.network.customizations.narrative.publications.PublicationContentRewardWriterShare;
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
 * Date: 2019-09-30
 * Time: 11:45
 *
 * @author brian
 */
@Getter
@Setter
@Entity
@Proxy
@FieldNameConstants
@NoArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "publicationReward_publicationOid_period_uidx", columnNames = {PublicationReward.FIELD__PUBLICATION_OID__COLUMN, PublicationReward.FIELD__PERIOD__COLUMN})
})
public class PublicationReward implements DAOObject<PublicationRewardDAO>, RewardPeriodRef {
    public static final String FIELD__PUBLICATION_OID__NAME = "publicationOid";
    public static final String FIELD__PUBLICATION_OID__COLUMN = FIELD__PUBLICATION_OID__NAME;

    // jw: since we will be inserting the records into this table via sql from ItemHourTrendingStats, let's just use an auto-increment PK:
    // https://thoughts-on-java.org/hibernate-tips-use-auto-incremented-column-primary-key/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = Fields.oid, updatable = false, nullable = false, insertable = false)
    private OID oid;

    @NotNull
    private OID publicationOid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ForeignKey(name = "fk_publicationReward_period")
    private RewardPeriod period;

    @NotNull
    private PublicationContentRewardWriterShare contentRewardWriterShare;

    private PublicationContentRewardRecipientType contentRewardRecipient;

    public Publication getPublication() {
        return Publication.dao().get(getPublicationOid());
    }

    public static PublicationRewardDAO dao() {
        return NetworkDAOImpl.getDAO(PublicationReward.class);
    }
}
