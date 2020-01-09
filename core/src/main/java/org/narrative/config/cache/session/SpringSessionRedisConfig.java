package org.narrative.config.cache.session;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.data.redis.config.ConfigureRedisAction;

/**
 * Date: 2018-12-07
 * Time: 10:28
 *
 * @author brian
 */
@Configuration
public class SpringSessionRedisConfig {
    @Value("${spring.session.redis.pretty-print-json}")
    private boolean prettyPrint;

    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer(objectMapper());
    }

    private ObjectMapper objectMapper() {
        // start with Redisson's JsonJacksonCodec ObjectMapper as a baseline.
        ObjectMapper mapper = new JsonJacksonCodec().getObjectMapper();

        //Ignore getters/setters for transient fields
        mapper.enable(MapperFeature.PROPAGATE_TRANSIENT_MARKER);

        //Enable property mode to detect {@link JsonProperty} on constructors annotated with {@link JsonCreator}
        ParameterNamesModule pnm = new ParameterNamesModule(JsonCreator.Mode.PROPERTIES);
        mapper.registerModule(pnm);

        //Pretty print mode for debugging
        if (prettyPrint) {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        }
        return mapper;
    }

    @Bean
    public static ConfigureRedisAction configureRedisAction() {
        return ConfigureRedisAction.NO_OP;
    }

}
