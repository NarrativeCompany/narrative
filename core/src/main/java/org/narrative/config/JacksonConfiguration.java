package org.narrative.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.customizations.narrative.service.mapper.util.BigDecimalDeserializer;
import org.narrative.network.customizations.narrative.service.mapper.util.BigDecimalSerializer;
import org.narrative.network.customizations.narrative.service.mapper.util.TimeZoneDeserializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.TimeZone;

/**
 * Date: 9/5/18
 * Time: 9:30 PM
 *
 * @author brian
 */
@Configuration
public class JacksonConfiguration {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return jacksonObjectMapperBuilder -> {
            jacksonObjectMapperBuilder
                    // bl: excluding non-null values will help reduce payload size
                    .serializationInclusion(JsonInclude.Include.NON_NULL)
                    // bl: use toString to write enums so we can control the values used (where appropriate)
                    .featuresToEnable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
                    // bl: jackson provides a TimeZoneSerializer, but no deserializer, so register our custom version here.
                    .deserializerByType(TimeZone.class, new TimeZoneDeserializer())
                    // jw: jackson serializes BigDecimals to a number, which for javascript could cause rounding issues.
                    //     We will get around that by serializing to a String.
                    .serializerByType(BigDecimal.class, new BigDecimalSerializer())
                    // bl: we want to support deserializing numbers optionally with a thousands separator, which isn't
                    // supported by Spring's NumberUtils, which just uses the BigDecimal(String) constructor.
                    .deserializerByType(BigDecimal.class, new BigDecimalDeserializer());

            // bl: only pretty-print on local and dev servers
            if(NetworkRegistry.getInstance().isLocalOrDevServer()) {
                jacksonObjectMapperBuilder.featuresToEnable(SerializationFeature.INDENT_OUTPUT);
            } else {
                jacksonObjectMapperBuilder.featuresToDisable(SerializationFeature.INDENT_OUTPUT);
            }

            // bl: Jackson2ObjectMapperBuilder doesn't support configuring this, so omitting for now.
            // bl: no point in writing null map values
            //mapper.setDefaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.ALWAYS, JsonInclude.Include.NON_NULL));
        };
    }
}
