package org.narrative.network.customizations.narrative.converters;

import org.narrative.common.util.enums.EnumRegistry;
import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.common.util.enums.StringEnum;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalConverter;
import org.springframework.core.convert.converter.GenericConverter;

import java.util.Set;

/**
 * Date: 9/13/18
 * Time: 3:02 PM
 *
 * @author brian
 */
public class EnumConverter implements GenericConverter, ConditionalConverter {
    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        Class<?> targetClass = targetType.getType();
        if(!Enum.class.isAssignableFrom(targetClass)) {
            return false;
        }

        Class<?> sourceClass = sourceType.getType();
        if(Number.class.isAssignableFrom(sourceClass)) {
            // bl: we can only convert Numbers to IntegerEnum
            return IntegerEnum.class.isAssignableFrom(targetClass);
        }

        // the only other type we can convert from is String
        if(sourceClass!=String.class) {
            return false;
        }

        // if it's a String, then it could be either StringEnum or IntegerEnum.
        return StringEnum.class.isAssignableFrom(targetClass) || IntegerEnum.class.isAssignableFrom(targetClass);
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return null;
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if(source==null) {
            return null;
        }

        Class<?> sourceClass = sourceType.getType();
        Class<? extends Enum> targetClass = (Class<? extends Enum>)targetType.getType();

        if(Number.class.isAssignableFrom(sourceClass)) {
            return EnumRegistry.getForId((Class<? extends IntegerEnum>)targetClass, ((Number)source).intValue());
        }

        assert sourceClass==String.class : "ConditionalConverter.matches() above should have handled this!";

        String val = (String)source;

        if(StringEnum.class.isAssignableFrom(targetClass)) {
            Object ret = EnumRegistry.getForId((Class<? extends StringEnum>)targetClass, val, false);
            // bl: only return a value if we found one. otherwise, fall through to handle an Integer value
            if(ret!=null) {
                return ret;
            }
        }

        // bl: if we couldn't identify a value above, see if we can convert a String to an IntegerEnum
        if(IntegerEnum.class.isAssignableFrom(targetClass)) {
            try {
                int intVal = Integer.parseInt(val);
                return EnumRegistry.getForId((Class<? extends IntegerEnum>)targetClass, intVal);
            } catch(NumberFormatException nfe) {
                // ignore
            }
        }


        // bl: last but not least, try valueOf
        try {
            return Enum.valueOf(targetClass, val);
        } catch(IllegalArgumentException e) {
            // ignore
        }

        return null;
    }
}
