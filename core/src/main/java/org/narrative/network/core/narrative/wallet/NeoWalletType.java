package org.narrative.network.core.narrative.wallet;

import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.network.core.narrative.rewards.ProratedRevenueType;
import org.narrative.network.customizations.narrative.neo.services.NeoUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2019-06-11
 * Time: 09:14
 *
 * @author brian
 */
public enum NeoWalletType implements IntegerEnum {
    NRVE_SMART_CONTRACT(12, true, "AZXKdEc2M73qeQMmPj2zmKJFDWmzQf8Gyr")
    ,NARRATIVE_COMPANY(4, true, "ARQuibeqbVjEdSBy1cL5K6x9wzYGpJqGSV")
    ,TEAM_TOKEN(7, true, "AZKtbNXjLyzG8LEYVthnsKaZC2ntkp65q5")
    ,REFERRALS_AND_INCENTIVES(6, true, "AG6tDeWBfCo9SiHNWZ7QdBsA56HNmj3UTi")
    ,TOKEN_MINT(5, true, "AJykYaFJavczgo7N8hZoMzDukB9XXN5K1E")
    ,NICHE_PAYMENT(0, true, "AKrm9BxdkQcqhnaRyCraLxyDXFepAwnjRA") {
        @Override
        public ProratedRevenueType getNrvePaymentWalletForProratedRevenueType() {
            return ProratedRevenueType.NICHE_REVENUE;
        }
    }
    ,PUBLICATION_PAYMENT(2, true, "AQ9td7mELMvVpohg1QYdifg31qX6SWvceC") {
        @Override
        public ProratedRevenueType getNrvePaymentWalletForProratedRevenueType() {
            return ProratedRevenueType.PUBLICATION_REVENUE;
        }
    }
    ,CHANNEL_FIAT_HOLDING(1, true, "AJcSEre4FUqunT6N89tqwVYsrjTbu9k4GL") {
        @Override
        public Collection<ProratedRevenueType> getFiatPaymentWalletForProratedRevenueTypes() {
            return EnumSet.of(ProratedRevenueType.NICHE_REVENUE, ProratedRevenueType.PUBLICATION_REVENUE);
        }
    }
    ,ADVERTISING_PAYMENT(13, true, "ALdYDv8QwKHsm7cGqzVZZSjLJahTxM1brV")
    ,MONTHLY_REWARDS(8, true, "AeSW3xsRnavUMfJFdm9KD2KuxR5qUpHNkV")
    ,MEMBER_CREDITS(9, true, "AYk7Y8r83Eg9yDKzZ7wJHwwoU4fX4FVpb8")
    ,PRORATED_NICHE_REVENUE(10, false, null)
    ,PRORATED_PUBLICATION_REVENUE(11, false, null)
    ,USER(14, false, null)
    ,REDEMPTION_TEMP(15, false, null)
    ;

    private final int id;
    private final boolean singleton;
    private final String productionDefaultNeoAddress;

    NeoWalletType(int id, boolean singleton, String productionDefaultNeoAddress) {
        assert singleton ? NeoUtils.isValidAddress(productionDefaultNeoAddress) : productionDefaultNeoAddress==null : "Should only specify a valid default production NEO address for all singleton types! type/" + this;

        this.id = id;
        this.singleton = singleton;
        this.productionDefaultNeoAddress = productionDefaultNeoAddress;
    }

    public static final Set<NeoWalletType> SINGLETON_TYPES = Collections.unmodifiableSet(EnumSet.allOf(NeoWalletType.class).stream().filter(NeoWalletType::isSingleton).collect(Collectors.toSet()));
    public static final Collection<NeoWalletType> MANAGEABLE_TYPES = Collections.unmodifiableSet(EnumSet.allOf(NeoWalletType.class).stream().filter(NeoWalletType::isAllowsWalletManagement).collect(Collectors.toSet()));

    @Override
    public int getId() {
        return id;
    }

    public String getProductionDefaultNeoAddress() {
        assert isSingleton() : "Should only use this method with singleton types! type/" + this;
        return productionDefaultNeoAddress;
    }

    public String getNameForDisplay() {
        return wordlet("neoWalletType." + this);
    }

    public WalletType getWalletType() {
        return WalletType.getWalletTypeForNeoWalletType(this);
    }

    public boolean isSingleton() {
        return singleton;
    }

    public boolean isAllowsWalletManagement() {
        return !isRedemptionTemp() && !isUser();
    }

    public boolean isProratedMonthRevenue() {
        WalletType walletType = getWalletType();
        return walletType!=null && walletType.isProratedMonthRevenue();
    }

    public boolean isActive() {
        // bl: advertising is not live yet
        return !isAdvertisingPayment();
    }

    public ProratedRevenueType getNrvePaymentWalletForProratedRevenueType() {
        return null;
    }

    public Collection<ProratedRevenueType> getFiatPaymentWalletForProratedRevenueTypes() {
        return null;
    }

    public boolean isNrveSmartContract() {
        return this==NRVE_SMART_CONTRACT;
    }

    public boolean isNarrativeCompany() {
        return this==NARRATIVE_COMPANY;
    }

    public boolean isMonthlyRewards() {
        return this==MONTHLY_REWARDS;
    }

    public boolean isMemberCredits() {
        return this==MEMBER_CREDITS;
    }

    public boolean isAdvertisingPayment() {
        return this==ADVERTISING_PAYMENT;
    }

    public boolean isUser() {
        return this == USER;
    }

    public boolean isRedemptionTemp() {
        return this == REDEMPTION_TEMP;
    }
}
