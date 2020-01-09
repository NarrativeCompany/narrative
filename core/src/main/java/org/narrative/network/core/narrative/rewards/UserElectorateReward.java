package org.narrative.network.core.narrative.rewards;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.narrative.rewards.dao.UserElectorateRewardDAO;
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
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

/**
 * Date: 2019-05-16
 * Time: 08:35
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
        @UniqueConstraint(name = "userElectorateReward_user_period_uidx", columnNames = {UserElectorateReward.FIELD__USER__COLUMN, UserElectorateReward.FIELD__PERIOD__COLUMN})
})
public class UserElectorateReward implements RewardTransactionRef<UserElectorateRewardDAO>, RewardPeriodRef, UserRef {
    // jw: since we will be inserting the records into this table via sql from ItemHourTrendingStats, let's just use an auto-increment PK:
    // https://thoughts-on-java.org/hibernate-tips-use-auto-incremented-column-primary-key/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = Fields.oid, updatable = false, nullable = false, insertable = false)
    private OID oid;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_userElectorateReward_user")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_userElectorateReward_period")
    private RewardPeriod period;

    // jw: we will need to process through all null entities in this until they all have a transaction
    @ManyToOne(fetch = FetchType.EAGER)
    @ForeignKey(name = "fk_userElectorateReward_transaction")
    private WalletTransaction transaction;

    private long points;

    @Override
    @Transient
    public WalletTransactionType getExpectedTransactionType() {
        return WalletTransactionType.ELECTORATE_REWARD;
    }

    public static UserElectorateRewardDAO dao() {
        return NetworkDAOImpl.getDAO(UserElectorateReward.class);
    }
}
