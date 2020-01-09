package org.narrative.network.core.narrative.rewards;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.network.core.narrative.rewards.dao.NarrativeCompanyRewardDAO;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.WalletTransactionType;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

/**
 * Date: 2019-05-16
 * Time: 07:54
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
        @UniqueConstraint(name = "narrativeCompanyReward_period_uidx", columnNames = {NarrativeCompanyReward.FIELD__PERIOD__COLUMN})
})
public class NarrativeCompanyReward implements RewardTransactionRef<NarrativeCompanyRewardDAO>, RewardPeriodRef {
    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    private OID oid;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_narrativeCompanyReward_period")
    private RewardPeriod period;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_narrativeCompanyReward_transaction")
    private WalletTransaction transaction;

    public NarrativeCompanyReward(RewardPeriod period, WalletTransaction transaction) {
        this.period = period;
        updateTransaction(transaction);
    }

    @Override
    @Transient
    public WalletTransactionType getExpectedTransactionType() {
        return WalletTransactionType.NARRATIVE_COMPANY_REWARD;
    }

    public static NarrativeCompanyRewardDAO dao() {
        return NetworkDAOImpl.getDAO(NarrativeCompanyReward.class);
    }
}
