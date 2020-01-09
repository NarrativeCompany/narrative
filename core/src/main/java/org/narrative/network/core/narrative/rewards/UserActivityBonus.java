package org.narrative.network.core.narrative.rewards;

import org.narrative.common.util.enums.IntegerEnum;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Date: 2019-05-15
 * Time: 19:41
 *
 * @author jonmark
 */
public enum UserActivityBonus implements IntegerEnum {
    // jw: for processing purposes we need to always store a value in the DB for UserActivityReward.bonus in order to
    //     know all of the users we have to calculate it for.
    NONE(0, 0)
    ,TIER_1(1, 30)
    ,TIER_2(2, 20)
    ,TIER_3(3, 10)
    ;

    private final int id;
    private final BigDecimal bonusMultiplier;
    private final int bonusPercentage;

    UserActivityBonus(int id, int bonusPercentage) {
        assert bonusPercentage >= 0 : "the bonus percentage should always be a non-negative value!";
        assert bonusPercentage < 100 : "the bonus percentage should always be under 100%!";

        this.id = id;

        // jw: because we are dividing integer by 100 the result should never have more than 2 decimal places
        this.bonusMultiplier = bonusPercentage==0 ? null : BigDecimal.valueOf(bonusPercentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.UNNECESSARY).add(BigDecimal.ONE);
        this.bonusPercentage = bonusPercentage;
    }

    public static final List<UserActivityBonus> BONUS_TIERS = Collections.unmodifiableList(Arrays.stream(values()).filter(b -> b.getBonusMultiplier()!=null).collect(Collectors.toList()));

    @Override
    public int getId() {
        return id;
    }

    public BigDecimal getBonusMultiplier() {
        return bonusMultiplier;
    }

    public int getBonusPercentage() {
        return bonusPercentage;
    }

    public boolean isNone() {
        return this == NONE;
    }
}