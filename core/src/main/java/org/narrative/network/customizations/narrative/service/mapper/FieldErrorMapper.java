package org.narrative.network.customizations.narrative.service.mapper;

import org.narrative.network.customizations.narrative.service.api.model.FieldErrorDTO;
import org.narrative.network.customizations.narrative.service.mapper.util.ServiceMapperConfig;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Date: 9/11/18
 * Time: 3:39 PM
 *
 * @author brian
 */
@Mapper(config = ServiceMapperConfig.class)
public interface FieldErrorMapper {
    default List<FieldErrorDTO> mapFieldErrorsMapToFieldErrorDTO(Map<String,List<String>> fieldErrors) {
        if(fieldErrors==null) {
            return null;
        }

        List<FieldErrorDTO> ret = new ArrayList<>(fieldErrors.size());
        for (Map.Entry<String, List<String>> entry : fieldErrors.entrySet()) {
            ret.add(FieldErrorDTO.builder().name(entry.getKey()).messages(entry.getValue()).build());
        }
        return ret;
    }
}
