package org.narrative.network.core.narrative.rewards;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.HibernateNrveValueType;
import org.narrative.network.core.narrative.rewards.dao.NicheContentRewardDAO;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Date: 2019-05-16
 * Time: 08:25
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
        // jw: a single piece of content should only appear in the rewards once for a niche
        @UniqueConstraint(name = "nicheContentReward_nicheReward_contentReward_uidx", columnNames = {NicheContentReward.FIELD__NICHE_REWARD__COLUMN, NicheContentReward.FIELD__CONTENT_REWARD__COLUMN})
})
public class NicheContentReward implements DAOObject<NicheContentRewardDAO>, NicheRewardRef {
    public static final String FIELD__CONTENT_REWARD__NAME = "contentReward";
    public static final String FIELD__CONTENT_REWARD__COLUMN = FIELD__CONTENT_REWARD__NAME+"_"+ContentReward.FIELD__OID__NAME;

    // jw: since we will be inserting the records into this table via sql from ItemHourTrendingStats, let's just use an auto-increment PK:
    // https://thoughts-on-java.org/hibernate-tips-use-auto-incremented-column-primary-key/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = Fields.oid, updatable = false, nullable = false, insertable = false)
    private OID oid;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_nicheContentReward_contentReward")
    private ContentReward contentReward;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_nicheContentReward_nicheReward")
    private NicheReward nicheReward;

    // jw: note: this is nullable so that we know which ones need to be saturated as part of setting up the rewards period.
    @Type(type = HibernateNrveValueType.TYPE)
    private NrveValue reward;

    public static NicheContentRewardDAO dao() {
        return NetworkDAOImpl.getDAO(NicheContentReward.class);
    }
}
