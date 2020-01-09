package org.narrative.network.core.narrative.rewards;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.narrative.rewards.dao.NicheRewardDAO;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import java.util.List;

/**
 * Date: 2019-05-13
 * Time: 14:50
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
        @UniqueConstraint(name = "nicheReward_niche_period_uidx", columnNames = {NicheReward.FIELD__NICHE__COLUMN, NicheReward.FIELD__PERIOD__COLUMN})
})
public class NicheReward implements DAOObject<NicheRewardDAO>, RewardPeriodRef {
    public static final String FIELD__NICHE__NAME = "niche";
    public static final String FIELD__NICHE__COLUMN = FIELD__NICHE__NAME+"_"+Niche.FIELD__OID__NAME;

    // jw: since we will be inserting the records into this table via sql from ItemHourTrendingStats, let's just use an auto-increment PK:
    // https://thoughts-on-java.org/hibernate-tips-use-auto-incremented-column-primary-key/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = Fields.oid, updatable = false, nullable = false, insertable = false)
    private OID oid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ForeignKey(name = "fk_nicheReward_niche")
    private Niche niche;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ForeignKey(name = "fk_nicheReward_period")
    private RewardPeriod period;

    @OneToMany(fetch=FetchType.LAZY, mappedBy=NicheContentReward.Fields.nicheReward)
    private List<NicheContentReward> nicheContentRewards;

    @OneToMany(fetch=FetchType.LAZY, mappedBy=NicheModeratorReward.Fields.nicheReward)
    private List<NicheModeratorReward> nicheModeratorRewards;

    public static NicheRewardDAO dao() {
        return NetworkDAOImpl.getDAO(NicheReward.class);
    }
}
