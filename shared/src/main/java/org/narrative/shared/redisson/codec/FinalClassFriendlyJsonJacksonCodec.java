package org.narrative.shared.redisson.codec;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.Value;
import org.redisson.codec.JsonJacksonCodec;

import javax.xml.datatype.XMLGregorianCalendar;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Customized JSON Jackson codec for Redisson.
 *
 * This codec is friendly final classes (such as Lombok @Value classes) and adds some serialization features to the
 * base {@link JsonJacksonCodec}.  See comments inline for details.
 */
public class FinalClassFriendlyJsonJacksonCodec extends JsonJacksonCodec {
    private final String[] ignoreFinalOnClassesInPackages;
    private final Map<Class, Boolean> whitelistedClassMap = new ConcurrentHashMap<>();

    /**
     * Pass an array of package names that contain classes that can safely be serialized to JSON regardless of whether
     * they are final.
     *
     * @param ignoreFinalOnClassesInPackages Package names in which to ignore whether a class is final
     */
    public FinalClassFriendlyJsonJacksonCodec(String... ignoreFinalOnClassesInPackages) {
        this.ignoreFinalOnClassesInPackages = ignoreFinalOnClassesInPackages;
    }

    private boolean isWhitelistedFinalClass(Class rawClass) {
        return whitelistedClassMap.computeIfAbsent(rawClass, (rawClass_) -> {
            String fqcn = rawClass_.getName();
            for (String curPackageName : ignoreFinalOnClassesInPackages) {
                if (fqcn.startsWith(curPackageName)) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * Override the out of the box behavior to be friendly to final classes (i.e. Lombok {@link Value} types).  The
     * default behavior for the superclass will not render type information for final classes which breaks
     * serialization for Lombok classes annotated with @Value.  This modification will treat any class in the
     * specified packahes as if they were not final.
     *
     * Based on Redisson 3.9.1 library source
     *
     * This implementation is a clone of the parent class code since the behavior is not easily overridden.  All code
     * modified from parent behavoir is blocked in ### BEGIN MODIFICATION/### END MODIFICATION comments for ease of
     * modification when {@link JsonJacksonCodec} changes.
     */
    @Override
    protected void initTypeInclusion(ObjectMapper mapObjectMapper) {
        TypeResolverBuilder<?> mapTyper = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL) {
            public boolean useForType(JavaType t) {
                switch (_appliesFor) {
                    case NON_CONCRETE_AND_ARRAYS:
                        while (t.isArrayType()) {
                            t = t.getContentType();
                        }
                        // fall through
                    case OBJECT_AND_NON_CONCRETE:
                        return (t.getRawClass() == Object.class) || !t.isConcrete();
                    case NON_FINAL:
                        while (t.isArrayType()) {
                            t = t.getContentType();
                        }
                        // to fix problem with wrong long to int conversion
                        if (t.getRawClass() == Long.class) {
                            return true;
                        }
                        if (t.getRawClass() == XMLGregorianCalendar.class) {
                            return false;
                        }
                        //### BEGIN MODIFICATION
                        if (isWhitelistedFinalClass(t.getRawClass())) {
                            return true;
                        }
                        //### END MODIFICATION
                        return !t.isFinal(); // includes Object.class
                    default:
                        // case JAVA_LANG_OBJECT:
                        return (t.getRawClass() == Object.class);
                }
            }
        };
        mapTyper.init(JsonTypeInfo.Id.CLASS, null);
        mapTyper.inclusion(JsonTypeInfo.As.PROPERTY);
        mapObjectMapper.setDefaultTyping(mapTyper);

        //### BEGIN MODIFICATION
        initTypeInclusionExtraSettings(mapObjectMapper);
        //### END MODIFICATION

        //### BEGIN MODIFICATION
        // warm up codec
        //try {
        //    byte[] s = mapObjectMapper.writeValueAsBytes(1);
        //    mapObjectMapper.readValue(s, Object.class);
        //} catch (IOException e) {
        //    throw new IllegalStateException(e);
        //}
        //### END MODIFICATION
    }

    /**
     * Extra mapper settings provided by this codec
     */
    protected void initTypeInclusionExtraSettings(ObjectMapper mapper) {
        //Enable property mode to detect {@link JsonProperty} on constructors annotated with {@link JsonCreator}
        ParameterNamesModule pnm = new ParameterNamesModule(JsonCreator.Mode.PROPERTIES);
        mapper.registerModule(pnm);

        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        mapper.configure(MapperFeature.AUTO_DETECT_CREATORS, true);
        mapper.registerModule(new JavaTimeModule());
    }
}
