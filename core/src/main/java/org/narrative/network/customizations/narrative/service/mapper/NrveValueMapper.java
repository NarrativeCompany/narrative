package org.narrative.network.customizations.narrative.service.mapper;

import org.narrative.network.customizations.narrative.NrveUsdValue;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.service.mapper.util.ServiceMapperConfig;
import org.mapstruct.Mapper;

import javax.validation.constraints.NotNull;

/**
 * Date: 2019-06-03
 * Time: 15:56
 *
 * @author brian
 */
@Mapper(config = ServiceMapperConfig.class)
public interface NrveValueMapper {
    /**
     * Map from {@link NrveValue} entity to {@link NrveUsdValue}.
     *
     * @param nrveValue incoming value to map
     * @return The mapped {@link NrveUsdValue}
     */
    default NrveUsdValue mapNrveValueToNrveUsdValue(@NotNull NrveValue nrveValue) {
        return new NrveUsdValue(nrveValue);
    }
}
