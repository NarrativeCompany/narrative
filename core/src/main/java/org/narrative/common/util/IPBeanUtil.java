package org.narrative.common.util;

import org.apache.commons.beanutils.PropertyUtils;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Feb 15, 2006
 * Time: 8:55:04 AM
 *
 * @author Brian
 */
public class IPBeanUtil {

    private static final NarrativeLogger logger = new NarrativeLogger(IPBeanUtil.class);

    /**
     * returns true if the method starts with "get" or "is", is non-static,
     * and takes no parameters.
     *
     * @param method the method to test
     * @return true if the specified method is a getter.
     */
    public static boolean isGetter(Method method) {
        String methodName = method.getName();
        return (methodName.startsWith("get") || methodName.startsWith("is")) && !Modifier.isStatic(method.getModifiers()) && method.getParameterTypes().length == 0 && !void.class.equals(method.getReturnType());
    }

    /**
     * returns true if the method starts with "set", is non-static,
     * has exactly one parameter, and has a void return type.
     *
     * @param method the method to test
     * @return true if the specified method is a setter.
     */
    public static boolean isSetter(Method method) {
        return method.getName().startsWith("set") && !Modifier.isStatic(method.getModifiers()) && method.getParameterTypes().length == 1 && method.getReturnType() == Void.TYPE;
    }

    public static String getPropertyNameFromGetter(Method method) {
        if (!isGetter(method)) {
            return null;
        }

        return getPropertyNameFromGetter(method.getName());
    }

    public static String getPropertyNameFromGetter(String methodName) {
        if (methodName.startsWith("get")) {
            return Introspector.decapitalize(methodName.substring(3));
        }

        assert methodName.startsWith("is") : "Getter must start with get or is, right?";
        return Introspector.decapitalize(methodName.substring(2));
    }

    public static String getPropertyNameFromSetter(Method method) {
        if (!isSetter(method)) {
            return null;
        }

        return getPropertyNameFromSetter(method.getName());
    }

    public static String getPropertyNameFromSetter(String methodName) {
        return Introspector.decapitalize(methodName.substring(3));
    }

    public static String getPropertyNameFromGetterOrSetter(Method method) {
        String propertyName = getPropertyNameFromGetter(method);
        if (IPStringUtil.isEmpty(propertyName)) {
            propertyName = getPropertyNameFromSetter(method);
        }
        return propertyName;
    }

    // bl: don't want to include "callback" or "callbacks", both of which are used by CGLIB's Factory interface.
    // the Factory interface is used in class generation for PropertySetTypeUtil and BeanProxy.
    // also, don't count Object.getClass() as a bean property.
    private static final Set<String> IGNORED_PROPERTIES = Collections.unmodifiableSet(newHashSet(Arrays.asList("class", "callback", "callbacks")));

    public static Collection<String> getBeanPropertyNamesFromObject(Object o) {
        PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(o);

        HashSet<String> ret = new HashSet<String>(pds.length - 1);

        for (PropertyDescriptor pd : pds) {
            // ignore any properties that we know aren't really properties.
            if (!IGNORED_PROPERTIES.contains(pd.getName().toLowerCase())) {
                ret.add(pd.getName());
            }
        }
        return ret;
    }

    public static Map<String, Object> getBeanPropertyNameToValue(Object o) {
        Map<String, Object> ret = new HashMap<String, Object>();
        Collection<String> beanPropertyNames = IPBeanUtil.getBeanPropertyNamesFromObject(o);
        for (String beanPropertyName : beanPropertyNames) {
            ret.put(beanPropertyName, getValue(o, beanPropertyName));
        }
        return ret;
    }

    public static Map<String, String> getBeanPropertyNameToStringValue(Object o) {
        Map<String, String> ret = new HashMap<String, String>();
        Collection<String> beanPropertyNames = IPBeanUtil.getBeanPropertyNamesFromObject(o);
        for (String beanPropertyName : beanPropertyNames) {
            Object value = getValue(o, beanPropertyName);
            ret.put(beanPropertyName, SerializationUtil.getPropertyValue(value));
        }
        return ret;
    }

    public static <T> void applyBeanPropertiesFromOneObjectToAnother(T source, T dest) {
        Map<String, Object> sourceProperties = getBeanPropertyNameToValue(source);
        Collection<String> destProperties = getBeanPropertyNamesFromObject(dest);
        for (Map.Entry<String, Object> entry : sourceProperties.entrySet()) {
            String propertyName = entry.getKey();
            // only apply properties that actually exist on the destination object
            if (destProperties.contains(propertyName)) {
                Object propertyValue = entry.getValue();
                setValue(dest, propertyName, propertyValue);
            }
        }
    }

    /**
     * extract the return type of a method.  you should only use this if you do not
     * know what the runtime Class is of the object containing the method.  if you have
     * the runtime type of the object on which the method exists, then we can extract
     * the parameterized types (if any) from the return type of the method.
     *
     * @param method the method to extract the return type from
     * @return the Class representing the return type of the given method
     */
    public static Class extractReturnType(Method method) {
        return extractReturnType(method.getDeclaringClass(), method);
    }

    /**
     * bl: stealing this from Hibernate.  PropertyInferredData.extractType().
     *
     * @param rootEntity the root class that has the property that we are attempting to extract the type from
     * @param method     the method from which we are extracting the return type
     * @return the Class representing the type to use for the given
     */
    public static Class extractReturnType(Class<?> rootEntity, Method method) {
        return extractTypeClass(rootEntity, method.getGenericReturnType(), method.getName());
    }

    public static Class extractTypeClass(Class<?> rootEntity, final Type t, String debugName) {
        if (t != null) {
            if (t instanceof Class) {
                return (Class) t;
            } else if (t instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) t;
                Type rawType = pt.getRawType();
                if (rawType instanceof Class) {
                    return (Class) rawType;
                } else {
                    throw UnexpectedError.getRuntimeException("rawType of parameterized type is not a class for type " + t);
                }
            } else if (t instanceof TypeVariable) {
                final TypeVariable neededType = (TypeVariable) t;
                GenericDeclaration gd = neededType.getGenericDeclaration();
                // if this type variable is defined on the method, then all we can
                // do is get the first bound from the type variable and use it as the class.
                if (gd instanceof Method) {
                    return getFirstBoundOfTypeVariable(neededType, debugName, t);
                }
                assert gd instanceof Class : "Can't have TypeVariables for Constructors currently.  Only Method and Class TypeVariables are supported";
                final Class classDefiningTypeVariable = (Class) gd;

                final Map<Class, Map<String, Type>> classToActualTypeArguments = getClassToActualTypeArgumentsMap(rootEntity, classDefiningTypeVariable);

                // now, we need to start at the class on which the TypeVariable is defined
                // and work our way backwards to try to identify the most specific parameterization
                // of the TypeVariable that we can find.
                Class classToFind = classDefiningTypeVariable;
                String varName = neededType.getName();
                Type actualType = neededType;
                while (true) {
                    Map<String, Type> actualTypeArguments = classToActualTypeArguments.get(classToFind);
                    if (actualTypeArguments == null) {
                        break;
                    }
                    actualType = actualTypeArguments.get(varName);
                    assert actualType != null : "How was actualType null?  It should always be specified since the map always contains all parameterized values!";
                    if (actualType instanceof TypeVariable) {
                        // found a type variable at this level?  cool.  keep digging.
                        varName = ((TypeVariable) actualType).getName();
                        classToFind = (Class) ((TypeVariable) actualType).getGenericDeclaration();
                    } else if (actualType instanceof Class) {
                        // we found the type we're looking for.  nice!
                        break;
                    } else {
                        throw UnexpectedError.getRuntimeException("Unable to extract type of property " + debugName + ": " + t);
                    }
                }

                // the type we found is a class?  perfect!  that is the return type!
                if (actualType instanceof Class) {
                    return (Class) actualType;
                }

                // we couldn't identify the exact parameterized class?  then let's do the best we
                // can by extracting the bound of the TypeVariable that we found.
                if (actualType instanceof TypeVariable) {
                    return getFirstBoundOfTypeVariable((TypeVariable) actualType, debugName, t);
                }
            } else if (t instanceof GenericArrayType) {
                final GenericArrayType arrayType = (GenericArrayType) t;
                return extractTypeClass(rootEntity, arrayType.getGenericComponentType(), debugName);
            } else if (t instanceof WildcardType) {
                final WildcardType wildcardType = (WildcardType) t;
                // bl: first try the upper bounds
                Type[] upperBounds = wildcardType.getUpperBounds();
                if (!isEmptyOrNull(upperBounds)) {
                    return extractTypeClass(rootEntity, upperBounds[0], debugName);
                }
                // then, try the lower bounds if no upper bounds
                Type[] lowerBounds = wildcardType.getLowerBounds();
                if (!isEmptyOrNull(lowerBounds)) {
                    return extractTypeClass(rootEntity, lowerBounds[0], debugName);
                }
                // bl: otherwise, there are no bounds, so just assume Object class.
                return Object.class;
            }
        }
        throw UnexpectedError.getRuntimeException("Unable to extract type of property " + debugName + ": " + t);
    }

    private static Map<Class, Map<String, Type>> getClassToActualTypeArgumentsMap(Class<?> rootEntity, Class classDefiningTypeVariable) {
        Map<Class, Map<String, Type>> classToActualTypeArguments = new HashMap<Class, Map<String, Type>>();

        populateClassToActualTypeArgumentsMapForSuperclasses(classToActualTypeArguments, rootEntity, classDefiningTypeVariable);
        return classToActualTypeArguments;
    }

    private static void populateClassToActualTypeArgumentsMapForSuperclasses(Map<Class, Map<String, Type>> classToActualTypeArguments, Class<?> rootEntity, Class classDefiningTypeVariable) {
        populateClassToActualTypeArgumentsMapForInterfaces(classToActualTypeArguments, rootEntity, classDefiningTypeVariable);
        Type type = rootEntity.getGenericSuperclass();
        if (type == null) {
            return;
        }
        // go through and populate the map for all of the parameterized types through the class
        // hierarchy until we get to the class on which the needed type is defined.
        if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;
            addMapOfParameterizedNameToActualTypeArgument(paramType, classToActualTypeArguments);
            type = paramType.getRawType();
        }

        // got to the class on which the type variable is defined? then break out of the loop.
        if (classDefiningTypeVariable.equals(type)) {
            return;
        }

        if (type instanceof Class) {
            populateClassToActualTypeArgumentsMapForSuperclasses(classToActualTypeArguments, (Class) type, classDefiningTypeVariable);
        }
    }

    private static void populateClassToActualTypeArgumentsMapForInterfaces(Map<Class, Map<String, Type>> classToActualTypeArguments, Class<?> rootEntity, Class classDefiningTypeVariable) {
        Type[] types = rootEntity.getGenericInterfaces();
        for (Type type : types) {
            // go through and populate the map for all of the parameterized types through the class
            // hierarchy until we get to the class on which the needed type is defined.
            if (type instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) type;
                addMapOfParameterizedNameToActualTypeArgument(paramType, classToActualTypeArguments);
                type = paramType.getRawType();
            }

            // got to the class on which the type variable is defined? then break out of the loop.
            if (classDefiningTypeVariable.equals(type)) {
                return;
            }

            if (type instanceof Class) {
                populateClassToActualTypeArgumentsMapForInterfaces(classToActualTypeArguments, (Class) type, classDefiningTypeVariable);
            }
        }
    }

    private static Class getFirstBoundOfTypeVariable(TypeVariable typeVariable, String debugName, Type originalType) {
        Type[] bounds = typeVariable.getBounds();
        if (bounds != null && bounds.length > 0) {
            Type bound = bounds[0];
            if (bound instanceof Class) {
                return (Class) bound;
            }
            if (bound instanceof ParameterizedType) {
                Type rawBound = ((ParameterizedType) bound).getRawType();
                if (rawBound instanceof Class) {
                    return (Class) rawBound;
                }
                throw UnexpectedError.getRuntimeException("Unable to extract type of property " + debugName + ": " + originalType);
            }
            if (bound instanceof TypeVariable) {
                return (Class) ((TypeVariable) bound).getGenericDeclaration();
            }
        }
        throw UnexpectedError.getRuntimeException("Unable to extract type of property " + debugName + ": " + originalType);
    }

    private static void addMapOfParameterizedNameToActualTypeArgument(ParameterizedType paramType, Map<Class, Map<String, Type>> classToParameterizedTypes) {
        Type rawType = paramType.getRawType();
        if (!(rawType instanceof Class)) {
            return;
        }
        Type[] actualTypeArguments = paramType.getActualTypeArguments();
        Class rawClass = (Class) rawType;
        TypeVariable[] typeParameters = rawClass.getTypeParameters();
        // no parameters specified?  then just return
        if (typeParameters.length != actualTypeArguments.length) {
            return;
        }
        Map<String, Type> ret = new HashMap<String, Type>();
        classToParameterizedTypes.put(rawClass, ret);
        for (int i = 0; i < typeParameters.length; i++) {
            TypeVariable typeVariable = typeParameters[i];
            Type actualTypeArgument = actualTypeArguments[i];
            ret.put(typeVariable.getName(), actualTypeArgument);
        }
    }

    /**
     * for a method that has a collection or map as its return type
     *
     * @param method the method with a return type to extract the collection type from.  if the type is a Collection, then it will
     *               return the parameterized type of the collection.  if the type is a Map, then it will return
     *               the parameterized type of the values (not the keys) in the collection.
     * @return the type of objects in the specified collection
     */
    public static Class extractReturnTypeCollectionType(Method method) {
        return extractCollectionType(method.getDeclaringClass(), method.getGenericReturnType());
    }

    /**
     * bl: stole this from Hibernate as well.  PropertyInferredData.extractCollectionType().
     *
     * @param t the type to extract the collection type from.  if the type is a Collection, then it will
     *          return the parameterized type of the collection. otherwise, null will be returned.
     * @return the type of objects in a collection specified by the given Type.
     */
    public static Class extractCollectionType(Class<?> rootEntity, Type t) {
        if (t != null && t instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) t;
            Type[] genTypes = pt.getActualTypeArguments();
            if (genTypes.length == 1) {
                return extractTypeClass(rootEntity, genTypes[0], "extracting collection type");
            }
        }
        return null;
    }

    /**
     * for a method that has a map as its return type
     *
     * @param method the method with a return type to extract the collection type from.  if the type is a Collection, then it will
     *               return the parameterized type of the collection.  if the type is a Map, then it will return
     *               the parameterized type of the values (not the keys) in the collection.
     * @return the type of objects in the specified collection
     */
    public static Class extractReturnTypeMapKeyType(Method method) {
        return extractMapKeyType(method.getDeclaringClass(), method.getGenericReturnType());
    }

    /**
     * bl: stole this from Hibernate as well.  PropertyInferredData.extractCollectionType().
     *
     * @param t the type to extract the collection type from.  if the type is a Collection, then it will
     *          return the parameterized type of the collection.  if the type is a Map, then it will return
     *          the parameterized type of the values (not the keys) in the collection.
     * @return the type of objects in a collection specified by the given Type.
     */
    public static Class extractMapKeyType(Class<?> rootEntity, Type t) {
        if (t != null && t instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) t;
            Type[] genTypes = pt.getActualTypeArguments();
            if (genTypes.length == 2) {
                return extractTypeClass(rootEntity, genTypes[0], "extracting map key type");
            }
        }
        return null;
    }

    /**
     * for a method that has a map as its return type
     *
     * @param method the method with a return type to extract the collection type from.  if the type is a Collection, then it will
     *               return the parameterized type of the collection.  if the type is a Map, then it will return
     *               the parameterized type of the values (not the keys) in the collection.
     * @return the type of objects in the specified collection
     */
    public static Class extractReturnTypeMapValueType(Method method) {
        return extractMapValueType(method.getDeclaringClass(), method.getGenericReturnType());
    }

    /**
     * bl: stole this from Hibernate as well.  PropertyInferredData.extractCollectionType().
     *
     * @param t the type to extract the collection type from.  if the type is a Map, then it will return
     *          the parameterized type of the values (not the keys) in the collection.  otherwise
     *          it will return null.
     * @return the type of objects used for the values in the Map specified by the given Type.
     */
    public static Class extractMapValueType(Class<?> rootEntity, Type t) {
        if (t != null && t instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) t;
            Type[] genTypes = pt.getActualTypeArguments();
            if (genTypes.length == 2) {
                return extractTypeClass(rootEntity, genTypes[1], "extracting map value type");
            }
        }
        return null;
    }

    public static Object getValue(Object bean, String propertyName) {
        Object ret = null;

        try {
            ret = PropertyUtils.getProperty(bean, propertyName);
        } catch (Exception e) {
            if (logger.isInfoEnabled()) {
                logger.info("Could not get property, so moving on : " + propertyName);
            }
        }

        return ret;
    }

    public static boolean setValue(Object bean, String propertyName, Object propertyValue) {
        boolean ret = true;

        try {
            PropertyUtils.setProperty(bean, propertyName, propertyValue);
        } catch (Exception e) {
            ret = false;
            if (logger.isInfoEnabled()) {
                logger.info("Could not get property, so moving on : " + propertyName);
            }
        }

        return ret;
    }
}
