package org.narrative.network.core.narrative.rewards;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.network.core.narrative.rewards.dao.UserActivityRewardDAO;
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
import org.hibernate.annotations.Type;

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
import javax.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Date: 2019-05-13
 * Time: 14:59
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
        @UniqueConstraint(name = "userActivityReward_user_period_uidx", columnNames = {UserActivityReward.FIELD__USER__COLUMN, UserActivityReward.FIELD__PERIOD__COLUMN})
})
public class UserActivityReward implements RewardTransactionRef<UserActivityRewardDAO>, RewardPeriodRef, UserRef {

    public static final BigDecimal FOUNDERS_BONUS_MULTIPLIER = new BigDecimal("1.1");

    // jw: since we will be inserting the records into this table via sql from ItemHourTrendingStats, let's just use an auto-increment PK:
    // https://thoughts-on-java.org/hibernate-tips-use-auto-incremented-column-primary-key/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = Fields.oid, updatable = false, nullable = false, insertable = false)
    private OID oid;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_userActivityReward_user")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_userActivityReward_period")
    private RewardPeriod period;

    @ManyToOne(fetch = FetchType.EAGER)
    @ForeignKey(name = "fk_userActivityReward_transaction")
    private WalletTransaction transaction;

    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    private UserActivityBonus bonus;

    // jw: represents the number of activity points this user accrued over the period, ultimately adjusted by their bonus and founder status
    private long points;

    public void applyBonus(UserActivityBonus bonus) {
        assert !bonus.isNone() : "Should never apply the " + UserActivityBonus.NONE + " bonus!";
        assert bonus.getBonusMultiplier()!=null : "Should always have a bonus multiplier! bonus/" + bonus;
        setBonus(bonus);
        BigDecimal points = BigDecimal.valueOf(getPoints());
        points = points.multiply(bonus.getBonusMultiplier()).setScale(0, RoundingMode.HALF_UP);
        setPoints(points.longValueExact());
    }

    @Override
    @Transient
    public WalletTransactionType getExpectedTransactionType() {
        return WalletTransactionType.ACTIVITY_REWARD;
    }

    public static UserActivityRewardDAO dao() {
        return NetworkDAOImpl.getDAO(UserActivityReward.class);
    }
}
