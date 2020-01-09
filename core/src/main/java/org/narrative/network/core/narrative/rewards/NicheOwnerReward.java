package org.narrative.network.core.narrative.rewards;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.narrative.rewards.dao.NicheOwnerRewardDAO;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.WalletTransactionType;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.UserRef;
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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

/**
 * Date: 2019-05-15
 * Time: 21:09
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
        @UniqueConstraint(name = "nicheOwnerReward_nicheReward_uidx", columnNames = {NicheOwnerReward.FIELD__NICHE_REWARD__COLUMN})
})
public class NicheOwnerReward implements RewardTransactionRef<NicheOwnerRewardDAO>, NicheRewardRef, UserRef {

    // jw: since we will be inserting the records into this table via sql from ItemHourTrendingStats, let's just use an auto-increment PK:
    // https://thoughts-on-java.org/hibernate-tips-use-auto-incremented-column-primary-key/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = Fields.oid, updatable = false, nullable = false, insertable = false)
    private OID oid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ForeignKey(name = "fk_nicheOwnerReward_user")
    private User user;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_nicheOwnerReward_nicheReward")
    private NicheReward nicheReward;

    @ManyToOne(fetch = FetchType.EAGER)
    @ForeignKey(name = "fk_nicheOwnerReward_transaction")
    private WalletTransaction transaction;

    @Override
    @Transient
    public WalletTransactionType getExpectedTransactionType() {
        return WalletTransactionType.NICHE_OWNERSHIP_REWARD;
    }

    public static NicheOwnerRewardDAO dao() {
        return NetworkDAOImpl.getDAO(NicheOwnerReward.class);
    }
}
