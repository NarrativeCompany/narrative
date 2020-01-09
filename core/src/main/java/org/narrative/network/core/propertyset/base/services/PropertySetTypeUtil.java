package org.narrative.network.core.propertyset.base.services;

import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.util.IPBeanUtil;
import org.narrative.common.util.IPUtil;
import org.narrative.common.util.SerializationUtil;
import org.narrative.common.util.UnexpectedError;
import net.sf.cglib.core.NamingPolicy;
import net.sf.cglib.core.Predicate;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.commons.beanutils.PropertyUtils;

import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Dec 6, 2005
 * Time: 2:08:10 PM
 *
 * @author Brian
 */
public class PropertySetTypeUtil {

    private static final Object NULL_OBJECT = new Object();
    private static final Map<Class, Factory> CACHED_OBJECT_PROTOTYPES = newHashMap();

    private PropertySetTypeUtil() {}

    public static <T> T getPropertyWrapper(Class<T> cls, PropertyMap propertyMap) {
        // if no PropertyMap is specified, then use the default.
        if (propertyMap == null) {
            propertyMap = new DefaultPropertySet();
        }
        return getPropertyWrapperFromPropertyMap(cls, propertyMap, null);
    }

    public static <T> T getEmptyPropertyWrapper(Class<T> cls) {
        return getPropertyWrapperFromPropertyMap(cls, new DefaultPropertySet(), null);
    }

    private static <T> T getPropertyWrapperFromPropertyMap(Class<T> cls, PropertyMap propertyMap, String parentPropertyName) {
        assert propertyMap != null;

        Factory prototype = CACHED_OBJECT_PROTOTYPES.get(cls);
        if (prototype == null) {
            synchronized (PropertySetTypeUtil.class) {
                prototype = CACHED_OBJECT_PROTOTYPES.get(cls);
                if (prototype == null) {
                    Enhancer enhancer = new Enhancer();
                    enhancer.setUseCache(false);
                    // set the superclass
                    enhancer.setSuperclass(cls);
                    enhancer.setNamingPolicy(PropertySetTypeUtilNamingPolicy.INSTANCE);
                    // bl: if the class implements Serializable, then we should look up the serialVersionUID of the class
                    // and use it as the serialVersionUID of the generated CGLIB class in order to ensure
                    // proper interoperability with new releases.
                    if (Serializable.class.isAssignableFrom(cls)) {
                        enhancer.setSerialVersionUID(ObjectStreamClass.lookup(cls).getSerialVersionUID());
                    }
                    // bl: the callback here is just a dummy placeholder so that the Enhancer knows the proper type of callback
                    // to use on future object creations.  we create the prototype, which is an implementation of the Factory
                    // interface by default.  the Factory interface exposes methods that allow us to create new instances
                    // of the objects with ease (and with significantly better performance since we won't need to create
                    // a new class each time we want an instance of an object.
                    enhancer.setCallback(new PropertySetInterceptor(null, !cls.isInterface(), parentPropertyName));
                    try {
                        prototype = (Factory) enhancer.create();
                    } catch (Throwable t) {
                        throw UnexpectedError.getRuntimeException("Failed instantiating the PropertySetTypeUtil class.  cls/" + cls.getName() + ". Does it have the necessary no-arg constructor that is required?", t, true);
                    }
                    CACHED_OBJECT_PROTOTYPES.put(cls, prototype);
                }
            }
        }

        // use an instance of PropertySetTypeUtil as the interceptor.
        return (T) prototype.newInstance(new PropertySetInterceptor(propertyMap, !cls.isInterface(), parentPropertyName));
    }

    /**
     * an internal class to handle the interception of PropertySet methods.
     * <p>
     * todo: PropertySetInterceptor supports collections, maps, and other mutable types.
     * the String representation of those values, however, are stored on the call to the setter.
     * thus, the following code will be problematic:
     * Collection c = new HashSet();
     * c.add("Value");
     * propertySet.setValue(c);
     * // here, "Value2" will be in the collection, but will not be properly represented in the property set
     * c.add("Value2");
     * <p>
     * Do we need to consider having a "persistValues()" method that will take all of the Objects
     * stored in cachedPropertyNameToObject and convert them to Strings and set them on the
     * PropertySet accordingly?  or do we just not care and make the rule that when you call the setter,
     * you better have the object populated to its fullest.
     */
    private static class PropertySetInterceptor implements MethodInterceptor, Serializable {
        private static final long serialVersionUID = -4783952154789136423L;
        private final PropertyMap propertyMap;
        private final String parentPropertyPrefix;
        /**
         * todo: is this internal cache a good idea?  it will save on reconstructing multiple
         * objects if getters are called multiple times.
         */
        private transient Map<String, ObjectPair<String, Object>> cachedPropertyNameToObject;
        private final boolean isAbstractClass;

        private PropertySetInterceptor(PropertyMap propertySet, boolean isAbstractClass, String parentPropertyName) {
            this.propertyMap = propertySet;
            this.isAbstractClass = isAbstractClass;
            this.parentPropertyPrefix = !isEmpty(parentPropertyName) ? parentPropertyName + "." : "";
        }

        public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            if (isAbstractClass) {
                // if this isn't an abstract method and this isn't a method from an interface,
                // then let's just invoke the method directly.  this enables PropertySetTypeBase implementors
                // to define abstract base classes that provide some level of functionality, as opposed to
                // purely getters and setters.
                if (!Modifier.isAbstract(method.getModifiers())) {
                    return methodProxy.invokeSuper(object, args);
                }
            }

            String methodName = method.getName();
            // bl: special handling for the PropertySetTypeBase.wrappedPropertyMap method.  just return
            // the PropertyMap that we have wrapped.
            if (methodName.equals("wrappedPropertyMap")) {
                return propertyMap;
            } else if (IPBeanUtil.isSetter(method)) {
                String propertyName = IPBeanUtil.getPropertyNameFromSetter(method.getName());
                Object arg = args[0];

                // jw: if this a nested property set we need to apply the properties from the provided property set on the
                //     property set for this object.
                if (PropertySetTypeBase.class.isAssignableFrom(method.getParameterTypes()[0])) {
                    assert arg != null : "When setting a nested property set we expect the value to never be null. property/" + propertyName;
                    IPBeanUtil.applyBeanPropertiesFromOneObjectToAnother(arg, PropertyUtils.getProperty(object, propertyName));
                    return null;
                }

                String propertyValue = SerializationUtil.getPropertyValue(arg);
                propertyMap.setPropertyValue(parentPropertyPrefix + propertyName, propertyValue);
                // cache the put as well so that subsequent gets will work.
                if (arg == null) {
                    arg = NULL_OBJECT;
                }
                // bl: must handle initializing the cache since it won't be serialized (it's transient).
                if (cachedPropertyNameToObject == null) {
                    cachedPropertyNameToObject = newConcurrentHashMap();
                }
                cachedPropertyNameToObject.put(propertyName, new ObjectPair<String, Object>(propertyValue, arg));
                return null;
            } else if (IPBeanUtil.isGetter(method)) {
                String propertyName = IPBeanUtil.getPropertyNameFromGetter(method.getName());
                // bl: must handle initializing the cache since it won't be serialized (it's transient).
                if (cachedPropertyNameToObject == null) {
                    cachedPropertyNameToObject = newConcurrentHashMap();
                }
                // if we found the object in the cache, use it.
                ObjectPair<String, Object> op = cachedPropertyNameToObject.get(propertyName);
                String propertyValue;
                String propName = parentPropertyPrefix + propertyName;
                try {
                    propertyValue = propertyMap.getPropertyValueByName(propName);
                } catch (Exception e) {
                    throw new RuntimeException("Error retrieving property value " + propName, e);
                }
                Object ret;
                // if we didn't find the cached object, or if the string in the underlying PropertyMap has changed,
                // then we need to look up a new return value.
                if (op == null || !IPUtil.isEqual(op.getOne(), propertyValue)) {
                    if (PropertySetTypeBase.class.isAssignableFrom(method.getReturnType())) {
                        ret = getPropertyWrapperFromPropertyMap(method.getReturnType(), propertyMap, parentPropertyPrefix + propertyName);
                    } else {
                        ret = SerializationUtil.getReturnValue(object, method, propertyValue);
                    }
                    // bl: be sure to cache null references as well.
                    if (ret == null) {
                        ret = NULL_OBJECT;
                    }
                    cachedPropertyNameToObject.put(propertyName, new ObjectPair<String, Object>(propertyValue, ret));
                } else {
                    // found the object and the value in the underlying map hasn't changed, so just return the object
                    // that we found.
                    ret = op.getTwo();
                }
                if (ret.equals(NULL_OBJECT)) {
                    return null;
                }
                return ret;
            } else {
                // not a getter or a setter?  then just invoke the method directly.
                return methodProxy.invokeSuper(object, args);
            }
        }
    }

    /**
     * PropertySetTypeUtil-specific NamingPolicy in order to ensure Enhanced class name consistency between servlets.
     * Based off of CGLIB's DefaultNamingPolicy.
     */
    private static class PropertySetTypeUtilNamingPolicy implements NamingPolicy {
        public static final PropertySetTypeUtilNamingPolicy INSTANCE = new PropertySetTypeUtilNamingPolicy();

        public String getClassName(String prefix, String source, Object key, Predicate names) {
            if (prefix == null) {
                prefix = "net.sf.cglib.empty.Object";
            } else if (prefix.startsWith("java")) {
                prefix = "$" + prefix;
            }
            String name = prefix + "$$" + source.substring(source.lastIndexOf('.') + 1) + "ByPropertySetTypeUtil";
            if (names.evaluate(name)) {
                throw UnexpectedError.getRuntimeException("Found duplicate class definition!  Should never happen! name/" + name + " for key/" + key);
            }
            return name;
        }
    }
}
