package org.narrative.common.web.struts;

import org.narrative.common.util.IPBeanUtil;
import org.narrative.common.util.enums.EnumRegistry;
import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.common.util.enums.StringEnum;
import com.opensymphony.xwork2.conversion.impl.EnumTypeConverter;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * The GEnumTypeConverter allows us to use either ordinal values or String values for enums in our parameters.
 * <p>
 * Date: Oct 1, 2008
 * Time: 5:23:16 PM
 *
 * @author brian
 */
public class GEnumTypeConverter extends EnumTypeConverter {

    @Override
    public Object convertValue(Map context, Object target, Member member, String propertyName, Object value, Class toType) {
        // bl: special case to try to derive the proper return type from the generic interface since Struts doesn't
        // always seem to do it correctly.
        if (Enum.class.equals(toType)) {
            Type type = null;
            if (member instanceof Method) {
                Method method = (Method) member;
                if (IPBeanUtil.isSetter(method)) {
                    Type genericParamType = method.getGenericParameterTypes()[0];
                    if (Enum.class.equals(genericParamType)) {
                        Class<?> cls = method.getDeclaringClass();
                        while (type == null && cls != null) {
                            Type[] types = cls.getGenericInterfaces();
                            for (Type interfaceType : types) {
                                Class interfaceTypeClass = null;
                                if (interfaceType instanceof Class) {
                                    interfaceTypeClass = (Class) interfaceType;
                                } else if (interfaceType instanceof ParameterizedType) {
                                    interfaceTypeClass = (Class) ((ParameterizedType) interfaceType).getRawType();
                                }
                                if (interfaceTypeClass != null) {
                                    try {
                                        Method interfaceMethod = interfaceTypeClass.getMethod(method.getName(), method.getParameterTypes());
                                        type = interfaceMethod.getGenericParameterTypes()[0];
                                        break;
                                    } catch (NoSuchMethodException e) {
                                        // ignore
                                    }
                                }
                            }
                            cls = cls.getSuperclass();
                        }

                    }
                } else if (IPBeanUtil.isGetter(method)) {
                    Type genericReturnType = method.getGenericReturnType();
                    if (Enum.class.equals(genericReturnType)) {
                        Class<?> cls = method.getDeclaringClass();
                        while (type == null && cls != null) {
                            Type[] types = cls.getGenericInterfaces();
                            for (Type interfaceType : types) {
                                Class interfaceTypeClass = null;
                                if (interfaceType instanceof Class) {
                                    interfaceTypeClass = (Class) interfaceType;
                                } else if (interfaceType instanceof ParameterizedType) {
                                    interfaceTypeClass = (Class) ((ParameterizedType) interfaceType).getRawType();
                                }
                                if (interfaceTypeClass != null) {
                                    try {
                                        Method interfaceMethod = interfaceTypeClass.getMethod(method.getName());
                                        type = interfaceMethod.getGenericReturnType();
                                        break;
                                    } catch (NoSuchMethodException e) {
                                        // ignore
                                    }
                                }
                            }
                            cls = cls.getSuperclass();
                        }
                    }
                }
                if (type != null) {
                    toType = IPBeanUtil.extractTypeClass(target.getClass(), type, "GEnumTypeConverter");
                }
            }
        }
        return this.convertValue(context, value, toType);
    }

    public Object convertValue(Map context, Object o, Class toClass) {
        assert Enum.class.isAssignableFrom(toClass) : "According to our xwork-conversion.properties, we should only be converting enums! cls/" + toClass;
        String str = null;
        if (o instanceof String[]) {
            str = ((String[]) o)[0];
        } else if (o instanceof String) {
            str = (String) o;
        } else if (o instanceof Character) {
            // jw: XWork has the same issue with not honoring the Character, because of that lets make sure when we convert
            //     the Character into a String that we will also pass that forward to xwork in the event that we cannot
            //     handle the conversion.
            o = str = o.toString();
        }
        if (str != null) {
            Enum<?> ret = getEnumFromString(toClass, str);
            if (ret != null) {
                return ret;
            }
        }

        return super.convertValue(context, o, toClass);
    }

    public static <T extends Enum> T getEnumFromString(Class<T> toClass, String str) {
        try {
            int id = Integer.valueOf(str);
            if (IntegerEnum.class.isAssignableFrom(toClass)) {
                IntegerEnum anEnum = EnumRegistry.getForId((Class<? extends IntegerEnum>) toClass, id, false);
                if (anEnum != null) {
                    return (T) anEnum;
                }
            }
            // bl: fallback to treating the value as an ordinal.  hopefully we will completely
            // stop using this eventually so that we no longer have any reliance on ordinal values in our system.
            return toClass.getEnumConstants()[id];
        } catch (Throwable t) {
            // ignore
        }

        if (StringEnum.class.isAssignableFrom(toClass)) {
            StringEnum anEnum = EnumRegistry.getForId((Class<? extends StringEnum>) toClass, str, false);
            if (anEnum != null) {
                return (T) anEnum;
            }
        }
        return null;
    }
}
