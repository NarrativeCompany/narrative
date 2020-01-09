package org.narrative.network.core.settings.global;

import org.narrative.network.core.propertyset.base.services.PropertySetTypeBase;
import org.narrative.network.core.propertyset.base.services.annotations.IsDefaultRequired;
import org.narrative.network.core.propertyset.base.services.annotations.PropertySetTypeDef;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.neo.services.CryptoCurrencyType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * Date: Dec 7, 2005
 * Time: 1:59:22 PM
 *
 * @author Brian
 */
@PropertySetTypeDef(name = GlobalSettings.GLOBAL_SETTINGS_NAME, defaultProvider = DefaultGlobalSettings.class, global = true)
public abstract class GlobalSettings implements PropertySetTypeBase {

    public static final String GLOBAL_SETTINGS_NAME = "GlobalSettings";

    @IsDefaultRequired(true)
    public abstract long getSolrIndexVersion();

    public abstract void setSolrIndexVersion(long version);

    public long incrementSolrIndexVersion() {
        long ret = getSolrIndexVersion() + 1;
        setSolrIndexVersion(ret);
        return ret;
    }

    @IsDefaultRequired(false)
    public abstract BigDecimal getNrveUsdPrice();
    public abstract void setNrveUsdPrice(BigDecimal price);

    public void refreshCurrentNrveCachedValues() {
/* jw: we are no longer basing the minimum Niche price off of NEO, and instead allowing us to set that price
        BigDecimal usdPerNeo = CryptoCurrencyType.NEO.getCurrentUsdValue();
        assert usdPerNeo!=null : "We should always have a usdPerNeo by here!";

        BigDecimal neoPerUsd = BigDecimal.ONE.divide(usdPerNeo, 8, RoundingMode.CEILING);
        BigDecimal neuronsPerUsd = NrveValue.NEURONS_PER_NEO.multiply(neoPerUsd);
        BigDecimal neuronMinimumBid = neuronsPerUsd.multiply(TARGET_MINIMUM_BID_VALUE_USD);

        long neurons = neuronMinimumBid.longValue();
*/
        setNrveUsdPrice(CryptoCurrencyType.NRVE.getCurrentUsdValue());
    }

    public NrveValue getNrveValue(BigDecimal usdAmount, int scale, RoundingMode roundingMode) {
        return NrveValue.getNrveValueFromUsd(usdAmount, getNrveUsdPrice(), scale, roundingMode);
    }

    @IsDefaultRequired(false)
    public abstract Long getCurrentTrendingContentBuildTimeMs();
    public abstract void setCurrentTrendingContentBuildTimeMs(Long ms);

    public Instant getCurrentTrendingContentBuildTime() {
        Long ms = getCurrentTrendingContentBuildTimeMs();
        if (ms == null) {
            return null;
        }

        return Instant.ofEpochMilli(ms);
    }
}
