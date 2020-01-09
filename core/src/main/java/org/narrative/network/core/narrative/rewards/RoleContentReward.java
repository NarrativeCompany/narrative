package org.narrative.network.core.narrative.rewards;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.narrative.rewards.dao.RoleContentRewardDAO;
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
import javax.validation.constraints.NotNull;

/**
 * Date: 2019-09-30
 * Time: 08:18
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
        // bl: each user should only be able to get one reward per role per post-period (ContentReward)
        @UniqueConstraint(name = "roleContentReward_contentReward_role_user_uidx", columnNames = {RoleContentReward.FIELD__CONTENT_REWARD__COLUMN, RoleContentReward.FIELD__ROLE__COLUMN, RoleContentReward.FIELD__USER__COLUMN})
})
public class RoleContentReward implements RewardTransactionRef<RoleContentRewardDAO>, UserRef {
    public static final String FIELD__CONTENT_REWARD__NAME = "contentReward";
    public static final String FIELD__CONTENT_REWARD__COLUMN = FIELD__CONTENT_REWARD__NAME + "_" + ContentReward.FIELD__OID__NAME;
    public static final String FIELD__ROLE__NAME = "role";
    public static final String FIELD__ROLE__COLUMN = FIELD__ROLE__NAME;

    // jw: since we will be inserting the records into this table via sql from ItemHourTrendingStats, let's just use an auto-increment PK:
    // https://thoughts-on-java.org/hibernate-tips-use-auto-incremented-column-primary-key/
    // bl: have to type it with a type that Hibernate knows how to use, so using long here.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = FIELD__OID__NAME, updatable = false, nullable = false, insertable = false)
    private long id;

    @ManyToOne(optional=false)
    @ForeignKey(name = "fk_roleContentReward_contentReward")
    private ContentReward contentReward;

    @NotNull
    private ContentCreatorRewardRole role;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_roleContentReward_user")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @ForeignKey(name = "fk_roleContentReward_transaction")
    private WalletTransaction transaction;

    public RoleContentReward(ContentReward contentReward, @NotNull ContentCreatorRewardRole role, User user) {
        this.contentReward = contentReward;
        this.role = role;
        this.user = user;
    }

    @Override
    public OID getOid() {
        return OID.valueOf(getId());
    }

    @Override
    public void setOid(OID oid) {
        setId(oid.getValue());
    }

    @Override
    @Transient
    public WalletTransactionType getExpectedTransactionType() {
        return WalletTransactionType.CONTENT_REWARD;
    }

    public static RoleContentRewardDAO dao() {
        return NetworkDAOImpl.getDAO(RoleContentReward.class);
    }
}
