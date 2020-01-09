package org.narrative.common.util;

import au.com.bytecode.opencsv.CSVWriter;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.persistence.ObjectQuadruplet;
import org.narrative.common.persistence.ObjectTriplet;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.common.util.html.HTMLTruncator;
import org.narrative.common.util.posting.AnchorTextMassager;
import org.narrative.network.core.content.base.SEOObject;
import com.opensymphony.xwork2.ActionContext;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.Nullable;

import javax.servlet.jsp.PageContext;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Jun 13, 2005
 * Time: 12:48:12 AM
 */
public class CoreUtils {

    private static final NarrativeLogger logger = new NarrativeLogger(CoreUtils.class);

    private static long seq = 0;
    // jw: since javascript cannot support a large long as a number variable, so lets include a integer version so that
    //     we ca nuse it and increment it internally
    private static int seqInt = 0;
    public final static Pattern TWITTER_USER_PATTERN = Pattern.compile("@(\\w*)", Pattern.CASE_INSENSITIVE);

    public static final Boolean[] BOOLEAN_VALUES = new Boolean[]{Boolean.TRUE, Boolean.FALSE};

    public static ActionContext actionContext() {
        return ActionContext.getContext();
    }

    /**
     * Returns a sequential number unique to this instance of the JVM.  Usefull for creating unique ids that don't need
     * to span the life of a jvm.
     */
    public static long seq() {
        return seq++;
    }

    public static int seqInt() {
        return seqInt++;
    }

    public static OID seqOid() {
        return OID.valueOf(seq());
    }

    /**
     * Truncates a string givin a specified lenght and adds an eliplse, if the string is over the length.
     *
     * @param string
     * @param length
     */
    public static String elipse(String string, int length) {
        if (string.length() > length) {
            // jw: this will ensure that we only add the ellipses if the last character is not punctuation.
            return IPStringUtil.getStringWithEllipsis(string.substring(0, length), false);
        } else {
            return string;
        }
    }

    public static boolean exists(Object obj) {
        return HibernateUtil.doesObjectExist(obj);
    }

    public static Collection mapValues(Map map) {
        if (map == null) {
            return Collections.emptyList();
        }
        return map.values();
    }

    // jw: this method is necessary because JSP does not resolve null map keys properly.
    public static Object mapValue(Map map, Object key) {
        if (map == null) {
            return null;
        }

        return map.get(key);
    }

    public static Set mapKeySet(Map map) {
        if (map == null) {
            return Collections.emptySet();
        }
        return map.keySet();
    }

    public static Object firstItemInCol(Collection col) {
        if (isEmptyOrNull(col)) {
            return null;
        }
        return col.iterator().next();
    }

    public static Object itemFromIndexOfList(List list, int index) {
        if (isEmptyOrNull(list) || list.size() <= index) {
            return null;
        }

        return list.get(index);
    }

    public static boolean exists(Object obj, boolean debugBadIfNotFound) {
        boolean found = HibernateUtil.doesObjectExist(obj);
        if (!found && debugBadIfNotFound) {
            if (obj != null) {
                if (logger.isErrorEnabled()) {
                    logger.error("Unable to find entity object of type " + obj.getClass());
                } else if (logger.isErrorEnabled()) {
                    logger.error("Unable to find entity object of unspecified type");
                }
            }

        }
        return found;
    }

    /**
     * Returns a an object cast to the type specified or null if the object is not assignable to that type
     */
    @Nullable
    public static <T, C extends T> C cast(T obj, Class<C> cls) {
        return HibernateUtil.castObject(obj, cls);
    }

    public static <T> boolean isOfType(T obj, Class<? extends T> cls) {
        return HibernateUtil.isObjectOfType(obj, cls);
    }

    public static <T> T concrete(T obj) {
        return HibernateUtil.getConcreteClassInstance(obj);
    }

    public static Object concreteForJsp(Object obj) {
        return HibernateUtil.getConcreteClassInstance(obj);
    }

    public static boolean isEmpty(String str) {
        return IPStringUtil.isEmpty(str);
    }

    public static boolean isEmptyOrNull(Collection col) {
        return col == null || col.isEmpty();
    }

    public static boolean isEmptyOrNull(Map map) {
        return map == null || map.isEmpty();
    }

    public static boolean isEmptyOrNull(Object[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmptyOrNull(byte[] array) {
        return array == null || array.length == 0;
    }


    /* bl: probably shouldn't be executing ognl like this anymore since we want
       to minimize the use of ognl in our code & jsp.
    public static <T> T ognl(String expression, Class<T> cls) {
        return (T) ActionContext.getContext().getValueStack().findValue(expression, cls);
    }

    public static Object ognl(String expression) {
        return ActionContext.getContext().getValueStack().findValue(expression);
    }*/

    /**
     * @deprecated for use with the taglib only since it can't resolve T.
     * use either IPUtil.isEqual() or CoreUtils.isEqual(T,T) instead.
     */
    public static boolean isEqualForJsp(Object obj1, Object obj2) {
        // bl: for some reason, just calling isEqual doesn't seem to be using polymorphism
        // to identify the proper method to call.  i'm not really sure why, but this will
        // ensure that the DAOObject version of isEqual will be called when necessary.
        if (obj1 instanceof DAOObject && obj2 instanceof DAOObject) {
            return isEqual((DAOObject) obj1, (DAOObject) obj2);
        }
        return isEqual(obj1, obj2);
    }

    public static boolean isEqual(Object obj1, Object obj2) {
        return IPUtil.isEqual(obj1, obj2);
    }

    public static <T extends DAOObject> boolean isEqualOrNull(T obj1, T obj2) {
        if (obj1 == null && obj2 == null) {
            return true;
        }

        return isEqual(obj1, obj2);
    }

    public static <T extends DAOObject> boolean isEqual(T obj1, T obj2) {
        // todo: for consistency with IPUtil.isEqual, should this return true if both references are null?
        // jw: if we ever do make the change mentioned above we can remove isEqualOrNull
        if (obj1 == null || obj2 == null) {
            return false;
        }

        // need to update to select the uppermost super class that implements DAOObject for each of the arguments and test
        // is equals against those two.
        // don't want to get the concrete implementations because that may cause an unnecessary db read (proxy init).
        if (!getRootDAOObjectClass(obj1.getClass()).equals(getRootDAOObjectClass(obj2.getClass()))) {
            return false;
        }

        // bl: when initially creating new objects, we may still want to do comparisons on them.  in this case,
        // they may not yet have OIDs.  but one thing we know for sure.  anytime the two object references are equal,
        // the two objects truly are equal.
        if (obj1 == obj2) {
            return true;
        }

        // todo: does calling getOid initialize Hibernate proxies?
        if (obj1.getOid() == null || obj2.getOid() == null) {
            return false;
        }
        return obj1.getOid().equals(obj2.getOid());
    }

    private static Class<? extends DAOObject> getRootDAOObjectClass(Class<? extends DAOObject> daoObjectClass) {
        Class<? extends DAOObject> ret = daoObjectClass;
        while (true) {
            if (!DAOObject.class.isAssignableFrom(ret.getSuperclass())) {
                return ret;
            }
            ret = (Class<? extends DAOObject>) ret.getSuperclass();
        }
    }

    public static String attr(String name, String value) {
        StringBuilder sb = new StringBuilder();
        XMLUtil.addNameEqualsValueAttribute(sb, name, value, true /* for html */);
        return sb.toString();
    }

    public static String condAttr(String name, Object value) {
        if (value == null) {
            return "";
        }
        String valueStr = value.toString();
        if (IPStringUtil.isEmpty(valueStr)) {
            return "";
        }
        return attr(name, valueStr);
    }

    public static boolean isArray(Object unkown) {
        return unkown != null && unkown.getClass().isArray();
    }

    public static boolean contains(Object col, Object obj) {
        // note: despite the fact that some collections can contain null,
        // i don't think it's useful for purposes of this method, so
        // if the object we are testing for is null, return false.
        if (col == null || obj == null) {
            return false;
        }

        if (col instanceof Collection) {
            return ((Collection) col).contains(obj);
        } else if (col instanceof Map) {
            return ((Map) col).containsKey(obj);
        } else if (col.getClass().isArray()) {
            Object[] objs = (Object[]) col;
            for (Object o : objs) {
                if (o.equals(obj)) {
                    return true;
                }
            }
            return false;
        }
        throw UnexpectedError.getRuntimeException("contains only supports Collections, Maps, and arrays!  supplied: " + col.getClass() + " val: " + col);
    }

    public static boolean containsAny(Collection col1, Collection col2) {
        // jw: since the apache4.CollectionUtils does not handle nulls for us, I am going to use this method to handle it.
        if (col1 == null || col2 == null) {
            return false;
        }

        return CollectionUtils.containsAny(col1, col2);
    }

    public static <T> List<T> singletonList(T obj) {
        return Collections.singletonList(obj);
    }

    public static Map singletonMap(Object key, Object value) {
        return Collections.singletonMap(key, value);
    }

    public static List list2(Object obj1, Object obj2) {
        return Arrays.asList(obj1, obj2);
    }

    public static List list3(Object obj1, Object obj2, Object obj3) {
        return Arrays.asList(obj1, obj2, obj3);
    }

    public static String concat(Object obj1, Object obj2) {
        return concat(new Object[]{obj1, obj2});
    }

    public static String concat3(Object obj1, Object obj2, Object obj3) {
        return concat(obj1, obj2, obj3);
    }

    public static String concat4(Object obj1, Object obj2, Object obj3, Object obj4) {
        return concat(obj1, obj2, obj3, obj4);
    }

    public static String concat5(Object obj1, Object obj2, Object obj3, Object obj4, Object obj5) {
        return concat(obj1, obj2, obj3, obj4, obj5);
    }

    public static String concat6(Object obj1, Object obj2, Object obj3, Object obj4, Object obj5, Object obj6) {
        return concat(obj1, obj2, obj3, obj4, obj5, obj6);
    }

    public static String concat7(Object obj1, Object obj2, Object obj3, Object obj4, Object obj5, Object obj6, Object obj7) {
        return concat(obj1, obj2, obj3, obj4, obj5, obj6, obj7);
    }

    public static String concat8(Object obj1, Object obj2, Object obj3, Object obj4, Object obj5, Object obj6, Object obj7, Object obj8) {
        return concat(obj1, obj2, obj3, obj4, obj5, obj6, obj7, obj8);
    }

    private static String concat(Object... objs) {
        if (objs == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object obj : objs) {
            // don't want nulls to show up in the output for jsp
            if (obj == null) {
                continue;
            }
            sb.append(obj.toString());
        }
        return sb.toString();
    }

    public static String strutsActionName(String actionString) {
        // /action-name!method
        String ret = IPStringUtil.getStringAfterLastIndexOf(actionString, "/");
        // action-name!method
        ret = IPStringUtil.getStringBeforeLastIndexOf(ret, "!");
        // action-name
        ret = HYPHEN_PATTERN.matcher(ret).replaceAll("");
        // actionname
        return ret;
    }

    public static String getStringForJavascriptAndNullIfEmpty(String val) {
        // return the literal string null if the value is empty
        if (IPStringUtil.isEmpty(val)) {
            return "null";
        }
        // not empty?  then return the value in quotes
        return '"' + IPHTMLUtil.getJavascriptLiteralStringFromString(val, true) + '"';
    }

    public static String getBooleanForJavascriptAndNullIfEmpty(String val) {
        // return the literal string null if the value is empty
        if (IPStringUtil.isEmpty(val)) {
            return "null";
        }

        return Boolean.valueOf(val).toString();
    }

    /**
     * this is method is used to evaluate a string expression to dynamically execute a getter
     * on an object.  nested properties are supported.
     * [ and ( are not supported, so mapped and indexed lookups won't work.
     *
     * @param obj          the object on which to get the specified property.
     * @param propertyName the property name to get
     * @return the object representing the value of the specified bean property.
     */
    public static Object evaluateBeanProperty(Object obj, String propertyName) {
        if (obj == null || IPStringUtil.isEmpty(propertyName)) {
            return "";
        }
        assert !propertyName.contains("[") && !propertyName.contains("(") : "Property names can not contain [ or (.  Prop name: " + propertyName;
        List<String> subProperties = StrutsUtil.getSubPropertyListForParameterName(propertyName);
        Object current = obj;
        for (String subProperty : subProperties) {
            try {
                current = PropertyUtils.getProperty(current, subProperty);
            } catch (Throwable t) {
                throw UnexpectedError.getRuntimeException("Failed lookup of property.  Check expression: " + propertyName + " on class: " + obj.getClass(), t);
            }
        }
        // return whatever the last value was that we got
        return current;
    }

    /**
     * determine if a variable has been set in the page context
     *
     * @param pageContext  the page context
     * @param variableName the variable name
     * @param scope
     * @return true if the specified variable name has been set as an attribute on the specified page context
     */
    public static boolean isInPageContext(PageContext pageContext, String variableName, String scope) {
        return pageContext.getAttribute(variableName, org.apache.taglibs.standard.tag.common.core.Util.getScope(scope)) != null;
    }

    public static void throwUnexpectedError(String message) {
        throw UnexpectedError.getRuntimeException(message, true);
    }

    public static void assertTrue(boolean val, String message) {
        if (!val) {
            throwUnexpectedError(message);
        }
    }

    public static List removeFromCollection(Collection c, Object o) {
        List ret = new ArrayList(c);
        ret.remove(o);
        return ret;
    }

    private static final Pattern QUOTE_PATTERN = Pattern.compile("\"", Pattern.LITERAL);
    private static final Pattern APOSTROPHE_PATTERN = Pattern.compile("'", Pattern.LITERAL);
    private static final Pattern HYPHEN_PATTERN = Pattern.compile("-", Pattern.LITERAL);
    private static final Pattern PLUS_PATTERN = Pattern.compile("+", Pattern.LITERAL);
    private static final Pattern EQUAL_PATTERN = Pattern.compile("=", Pattern.LITERAL);
    private static final Pattern OPEN_PAREN_PATTERN = Pattern.compile("(", Pattern.LITERAL);
    private static final Pattern CLOSE_PAREN_PATTERN = Pattern.compile(")", Pattern.LITERAL);
    private static final Pattern SEMICOLON_PATTERN = Pattern.compile(":", Pattern.LITERAL);

    public static String makeSafeJavascriptIdentifier(String str) {
        // todo: what about square brackets [ and ] ?
        str = QUOTE_PATTERN.matcher(str).replaceAll("");
        str = APOSTROPHE_PATTERN.matcher(str).replaceAll("");
        str = HYPHEN_PATTERN.matcher(str).replaceAll("");
        str = PLUS_PATTERN.matcher(str).replaceAll("");
        str = EQUAL_PATTERN.matcher(str).replaceAll("");
        str = OPEN_PAREN_PATTERN.matcher(str).replaceAll("");
        str = CLOSE_PAREN_PATTERN.matcher(str).replaceAll("");
        str = SEMICOLON_PATTERN.matcher(str).replaceAll("");
        return str;
    }

    public static StringBuilder newStringBuilder() {
        return new StringBuilder();
    }

    public static void addCssClass(StringBuilder cssClasses, String cssClass) {
        assert cssClasses != null : "cssClasses should always be set when calling this function!";
        if (cssClass != null) {
            cssClass = cssClass.trim();
        }
        if (isEmpty(cssClass)) {
            return;
        }

        if (cssClasses.length() > 0) {
            cssClasses.append(' ');
        }
        cssClasses.append(cssClass);
    }

    public static Map newMap() {
        return new LinkedHashMap();
    }

    private static final Set<String> UNMODIFIABLE_MAP_CLASS_NAMES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            // jw: we use both of these from java.util.Collections all the time
            "UnmodifiableMap", "SingletonMap")));

    public static Map getModifiableMap(Map map) {
        if (map == null) {
            return new LinkedHashMap();
        }
        if (UNMODIFIABLE_MAP_CLASS_NAMES.contains(map.getClass().getSimpleName())) {
            return new LinkedHashMap(map);
        }
        return map;
    }

    public static void mapPut(Map map, Object key, Object value) {
        map.put(key, value);
    }

    public static void mapPutNoNulls(Map map, Object key, Object value) {
        if (key == null || value == null) {
            return;
        }
        map.put(key, value);
    }

    public static void mapPutNonEmpty(Map map, Object key, String value) {
        if (key == null || isEmpty(value)) {
            return;
        }
        map.put(key, value);
    }

    public static void mapPutAll(Map map, Map mapToAdd) {
        if (map == null || mapToAdd == null) {
            return;
        }
        map.putAll(mapToAdd);
    }

    public static void mapRemove(Map map, Object key) {
        if (map == null) {
            return;
        }
        map.remove(key);
    }

    public static List newList() {
        return new LinkedList();
    }

    public static Set newSet() {
        return new LinkedHashSet();
    }

    public static void listAddToPosition(List list, int position, Object element) {
        list.add(position, element);
    }

    public static void collectionAdd(Collection col, Object element) {
        col.add(element);
    }

    public static void collectionAddAll(Collection col, Collection elements) {
        if (!isEmptyOrNull(elements)) {
            col.addAll(elements);
        }
    }

    public static void collectionRemove(Collection col, Object element) {
        col.remove(element);
    }

    public static void collectionRemoveAll(Collection col, Collection colToRemove) {
        col.removeAll(colToRemove);
    }

    public static long round(double val) {
        return (long) val;
    }

    public static int currentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    public static <T> List<T> getListWithMaxSize(List<T> objs, int max) {
        if (objs == null || objs.isEmpty()) {
            return objs;
        }
        if (objs.size() <= max) {
            return objs;
        }
        return objs.subList(0, max);
    }

    public static String newline() {
        return "\n";
    }

    public static String newlines(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void appendPathParam(StringBuilder sb, String paramPrefix, String paramName, Object paramValue) {
        sb.append("/");
        if (!isEmpty(paramPrefix)) {
            sb.append(paramPrefix);
            sb.append(".");
        }
        sb.append(paramName);
        sb.append("/");
        sb.append(IPHTMLUtil.getURLEncodedStringButDontEncodeSpacesToPlus(paramValue.toString()));
    }

    public static <T> HashSet<T> newHashSet() {
        return new HashSet<T>();
    }

    public static <T> HashSet<T> newHashSet(Collection<T> col) {
        return new HashSet<T>(col);
    }

    public static <T> HashSet<T> newHashSet(T... vals) {
        return new HashSet<T>(Arrays.asList(vals));
    }

    public static <T> Set<T> newConcurrentHashSet() {
        return Collections.newSetFromMap(new ConcurrentHashMap<T, Boolean>());
    }

    public static <T> TreeSet<T> newTreeSet() {
        return new TreeSet<T>();
    }

    public static <T> TreeSet<T> newTreeSet(Comparator<T> comparer) {
        return new TreeSet<T>(comparer);
    }

    public static <T> LinkedHashSet<T> newLinkedHashSet() {
        return new LinkedHashSet<T>();
    }

    public static <T> LinkedHashSet<T> newLinkedHashSet(T... vals) {
        return new LinkedHashSet<T>(Arrays.asList(vals));
    }

    public static <T> SortedSet<T> newSortedSet(Comparator<T> comparator, T[] values) {
        SortedSet<T> set = new TreeSet<T>(comparator);
        if (values != null) {
            for (T value : values) {
                set.add(value);
            }
        }

        return set;
    }

    public static <T> LinkedHashSet<T> newLinkedHashSet(Collection<T> col) {
        if (isEmptyOrNull(col)) {
            return new LinkedHashSet<T>();
        }
        return new LinkedHashSet<T>(col);
    }

    public static <T> LinkedList<T> newLinkedList() {
        return new LinkedList<T>();
    }

    public static <T> LinkedList<T> newLinkedList(Collection<T> values) {
        if (isEmptyOrNull(values)) {
            return newLinkedList();
        }
        return new LinkedList<T>(values);
    }

    public static <T> ArrayList<T> newArrayList() {
        return new ArrayList<T>();
    }

    public static <T> ArrayList<T> newArrayList(int size) {
        return new ArrayList<T>(size);
    }

    public static <T> ArrayList<T> newArrayList(Collection<T> col) {
        return new ArrayList<T>(col);
    }

    public static <T> ArrayList<T> newArrayList(Collection<T>... cols) {
        if (cols == null) {
            return new ArrayList<T>();
        }
        int size = 0;
        for (Collection<T> col : cols) {
            if (col != null) {
                size += col.size();
            }
        }

        ArrayList<T> result = newArrayList(size);
        for (Collection<T> col : cols) {
            if (col != null) {
                result.addAll(col);
            }
        }

        return result;
    }

    public static <V> List<V> asList(V... values) {
        if (values == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(values);
    }

    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<K, V>();
    }

    public static <K, V> HashMap<K, V> newHashMap(Map<K, V> map) {
        if (map == null) {
            return new HashMap<K, V>();
        }
        return new HashMap<K, V>(map);
    }

    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap() {
        return new LinkedHashMap<K, V>();
    }

    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap(int initialCapacity) {
        return new LinkedHashMap<K, V>(initialCapacity);
    }

    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap(Map<K, V> map) {
        return new LinkedHashMap<K, V>(map);
    }

    public static <K, V> TreeMap<K, V> newTreeMap() {
        return new TreeMap<K, V>();
    }

    public static <K, V> TreeMap<K, V> newTreeMap(Comparator<K> comparator) {
        return new TreeMap<K, V>(comparator);
    }

    public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap() {
        return new ConcurrentHashMap<K, V>();
    }

    public static <V> Stack<V> newStack() {
        return new Stack<V>();
    }

    public static <V> Collection<V> intersection(Collection<V> col1, Collection<V> col2) {
        if (isEmptyOrNull(col1) || isEmptyOrNull(col2)) {
            return Collections.emptyList();
        }

        HashSet<V> result = newHashSet(col1);
        result.retainAll(col2);

        return result;
    }

    public static <V> SubListIterator<V> newSubListIterator(Collection<V> col) {
        if (col instanceof List) {
            return new SubListIterator<V>((List<V>) col);
        }

        return new SubListIterator<V>(newArrayList(col));
    }

    public static <V> SubListIterator<V> newSubListIterator(Collection<V> col, int chunkSize) {
        if (col instanceof List) {
            return new SubListIterator<V>((List<V>) col, chunkSize);

        }
        return new SubListIterator<V>(newArrayList(col), chunkSize);
    }

    public static <V> void processListInChunks(List<V> list, SubListProcessor<V> processor) {
        processSubListIterator(newSubListIterator(list), processor);
    }

    public static <V> void processListInChunks(List<V> list, int chunkSize, SubListProcessor<V> processor) {
        processSubListIterator(newSubListIterator(list, chunkSize), processor);
    }

    public static <V> void processSubListIterator(SubListIterator<V> subListIterator, SubListProcessor<V> processor) {
        if (subListIterator == null) {
            return;
        }
        while (subListIterator.hasNext()) {
            processor.processSubList(subListIterator.next());
        }
    }

    public static <V> NameValuePair<V> newNameValuePair(String name, V value) {
        return new NameValuePair<V>(name, value);
    }

    public static ObjectPair<Object, Object> newObjectPairForJsp(Object one, Object two) {
        return newObjectPair(one, two);
    }

    public static <O, T> ObjectPair<O, T> newObjectPair(O one, T two) {
        return new ObjectPair<O, T>(one, two);
    }

    public static <U, D, T> ObjectTriplet<U, D, T> newObjectTriplet(U un, D deux, T trois) {
        return new ObjectTriplet<U, D, T>(un, deux, trois);
    }

    public static ObjectTriplet<Object, Object, Object> newObjectTripletForJsp(Object un, Object deux, Object trois) {
        return newObjectTriplet(un, deux, trois);
    }

    public static <U, D, T, Q> ObjectQuadruplet<U, D, T, Q> newObjectQuadruplet(U un, D deux, T trois, Q quatre) {
        return new ObjectQuadruplet<U, D, T, Q>(un, deux, trois, quatre);
    }

    public static ObjectQuadruplet<Object, Object, Object, Object> newObjectQuadrupletForJsp(Object un, Object deux, Object trois, Object quatre) {
        return newObjectQuadruplet(un, deux, trois, quatre);
    }

    public static String linkifyTwitterMessage(String message) {
        StringBuffer sb = new StringBuffer();
        Matcher matcher = TWITTER_USER_PATTERN.matcher(message);

        while (matcher.find()) {
            String theMatch = matcher.group(1);
            matcher.appendReplacement(sb, "@<a href=\"http://twitter.com/" + theMatch + "\">" + theMatch + "</a>");
        }
        matcher.appendTail(sb);

        return AnchorTextMassager.applyAutoLinkAnchorReplacements(sb.toString());
    }

    /**
     * if the HTML disabled version of the tweet is bigger then the subject max we need to truncate the HTML
     *
     * @param twitterMsg Full HTML Disabled Tweet
     * @param size       Size to truncate tweet to
     * @return potentially truncated tweet
     */
    public static String enforceHTMLSize(String twitterMsg, int size) {
        if (twitterMsg.length() > size) {
            twitterMsg = HTMLTruncator.truncateHTML(twitterMsg, null, false, size);
        }

        return twitterMsg;
    }

    public static Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }

    private static final String STYLE_TAG_START = "<style type=\"text/css\">";
    private static final String STYLE_TAG_END = "</style>";

    public static String stripCssStyleTag(String css) {
        if (isEmpty(css)) {
            return css;
        }
        css = css.trim();
        String newCss = IPStringUtil.getStringAfterStripFromStart(css, STYLE_TAG_START);
        assert css.length() > newCss.length() : "Should only use stripCssStyleTag when you're actually going to use it! Need to make sure style tag follows the proper format!";
        css = newCss;
        newCss = IPStringUtil.getStringAfterStripFromEnd(css, STYLE_TAG_END);
        assert css.length() > newCss.length() : "Should only use stripCssStyleTag when you're actually going to use it! Need to make sure style tag follows the proper format!";
        return newCss.trim();
    }

    private static final String SCRIPT_TAG_START = "<script type=\"text/javascript\">";
    private static final String SCRIPT_TAG_END = "</script>";

    public static String stripScriptTag(String javascript) {
        if (isEmpty(javascript)) {
            return javascript;
        }
        javascript = javascript.trim();
        String newJavascript = IPStringUtil.getStringAfterStripFromStart(javascript, SCRIPT_TAG_START);
        assert javascript.length() > newJavascript.length() : "Should only use stripScriptTag when you're actually going to use it! Need to make sure script tag follows the proper format!";
        javascript = newJavascript;
        newJavascript = IPStringUtil.getStringAfterStripFromEnd(javascript, SCRIPT_TAG_END);
        assert javascript.length() > newJavascript.length() : "Should only use stripScriptTag when you're actually going to use it! Need to make sure script tag follows the proper format!";
        return newJavascript.trim();
    }

    public static String getPageContextOid(Object object) {
        if (object == null) {
            return null;
        }

        if (SEOObject.class.isAssignableFrom(object.getClass())) {
            String prettyUrlString = ((SEOObject) object).getPrettyUrlString();
            // bl: only use the prettyUrlString if there actually is one to use.
            if (!isEmpty(prettyUrlString)) {
                return prettyUrlString;
            }
        }

        if (DAOObject.class.isAssignableFrom(object.getClass())) {
            return ((DAOObject) object).getOid().toString();
        }

        if (object.getClass().isEnum()) {
            return ((Enum) object).name();
        }

        return object.toString();
    }

    public static String getPageContextType(Object object) {
        if (object == null) {
            return null;
        }

        // jw: we need to prevent javassist objects from being displayed
        Class objectClass = object.getClass();
        if (DAOObject.class.isAssignableFrom(objectClass)) {
            objectClass = concrete(object).getClass();
        }

        return objectClass.getSimpleName();
    }

    public static Date getDateFromLong(long dateLong) {
        return new Date(dateLong);
    }

    public static <T> T getLastObjectFromList(List<T> list) {
        if (isEmptyOrNull(list)) {
            return null;
        }

        return list.get(list.size() - 1);
    }

    public static <K, V> void addObjectToMapsValueSet(Map<K, Set<V>> map, K key, V value) {
        Set<V> values = map.get(key);
        if (values == null) {
            map.put(key, values = newLinkedHashSet());
        }
        values.add(value);
    }

    public static <K, V> void makeMapValueSetsUnmodifiable(Map<K, Set<V>> map) {
        if (isEmptyOrNull(map)) {
            return;
        }
        for (K key : newArrayList(map.keySet())) {
            makeMapsValueSetUnmodifiable(map, key);
        }
    }

    public static <K, NK, NV> void makeNestedMapsUnmodifiable(Map<K, Map<NK, NV>> map) {
        if (isEmptyOrNull(map)) {
            return;
        }
        for (K key : newArrayList(map.keySet())) {
            makeNestedMapsUnmodifiable(map, key);
        }
    }

    public static <K, NK, NV> void makeNestedMapsUnmodifiable(Map<K, Map<NK, NV>> map, K key) {
        Map<NK, NV> nestedMap = map.get(key);
        if (nestedMap != null) {
            map.put(key, Collections.unmodifiableMap(nestedMap));
        }
    }

    public static <K, V> void makeMapsValueSetUnmodifiable(Map<K, Set<V>> map, K key) {
        Set<V> values = map.get(key);
        if (values != null) {
            map.put(key, Collections.unmodifiableSet(values));
        }
    }

    public static <K, V> void makeMapValueListsUnmodifiable(Map<K, List<V>> map) {
        if (isEmptyOrNull(map)) {
            return;
        }
        for (K key : newArrayList(map.keySet())) {
            makeMapsValueListUnmodifiable(map, key);
        }
    }

    public static <K, V> void makeMapsValueListUnmodifiable(Map<K, List<V>> map, K key) {
        List<V> values = map.get(key);
        if (values != null) {
            map.put(key, Collections.unmodifiableList(values));
        }
    }

    public static String newString(Object... objs) {
        if (isEmptyOrNull(objs)) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (Object obj : objs) {
            if (obj != null) {
                result.append(obj);
            }
        }

        return result.toString();
    }

    public static <K, V> void addMapListLookupValue(Map<K, List<V>> lookup, K key, V value) {
        List<V> values = lookup.get(key);
        if (values == null) {
            lookup.put(key, values = newLinkedList());
        }
        values.add(value);
    }

    public static <K, V> void addMapListLookupValues(Map<K, List<V>> lookup, K key, Collection<V> values) {
        List<V> currentValues = lookup.get(key);
        if (currentValues == null) {
            lookup.put(key, currentValues = newLinkedList());
        }
        currentValues.addAll(values);
    }

    public static <K, V> void addMapSetLookupValue(Map<K, Set<V>> lookup, K key, V value) {
        Set<V> values = lookup.get(key);
        if (values == null) {
            lookup.put(key, values = newLinkedHashSet());
        }
        values.add(value);
    }

    public static <K, V> void addMapSetLookupValues(Map<K, Set<V>> lookup, K key, Collection<V> values) {
        Set<V> currentValues = lookup.get(key);
        if (currentValues == null) {
            lookup.put(key, currentValues = newLinkedHashSet());
        }
        currentValues.addAll(values);
    }

    public static <K, V> V getMapValue(Map<K, V> lookup, K key, V defaultValue) {
        V value = lookup.get(key);

        return value == null ? defaultValue : value;
    }

    /**
     * JW: these could be useful methods if we find ourselves doing this a lot,
     */
    public static <K, VK, VV> Map<K, Map<VK, VV>> addUninitializedLookupMapValue(Map<K, Map<VK, VV>> map, K key, VK valueKey, VV valueValue) {
        if (map == null) {
            map = newLinkedHashMap();
        }
        addLookupMapValue(map, key, valueKey, valueValue);

        return map;
    }

    public static <K, VK, VV> void addLookupMapValue(Map<K, Map<VK, VV>> map, K key, VK valueKey, VV valueValue) {
        assert map != null : "Should always provide a map!";

        Map<VK, VV> value = map.get(key);
        if (value == null) {
            map.put(key, value = newLinkedHashMap());
        }
        value.put(valueKey, valueValue);
    }

    public static <T> ConcurrentLinkedQueue<T> newConcurrentLinkedQueue() {
        return new ConcurrentLinkedQueue<T>();
    }

    public static void writeCsvRow(CSVWriter writer, Collection<String> data) {
        writer.writeNext(data.toArray(new String[data.size()]));
    }

    public static void writeCsvRowAndClear(CSVWriter writer, Collection<String> data) {
        writeCsvRow(writer, data);
        // jw: after we write lets go ahead and clear the data.
        data.clear();
    }

    // jw: Hibernate maps have issues when accessed prior to initialization, so often we need to force initialization
    //     through a init method next to the getter.  This method will centralize that rather simple logic, but should
    //     make it easier to adjust if we ever change this logic.
    //     See this topic for just one example of why this type of logic is necessary: https://forum.hibernate.org/viewtopic.php?f=9&t=978174
    public static <K, V> Map<K, V> initHibernateMap(Map<K, V> map) {
        if (map != null) {
            map.size();
        }
        return map;
    }

    public static <C extends Collection<V>, V> C initHibernateCollection(C col) {
        if (col != null) {
            col.size();
        }
        return col;
    }

    // jw: see: http://stackoverflow.com/a/25853507/5656622
    private static final Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[{}()\\[\\].+*?^$\\\\|]");

    public static String escapeSpecialPatternCharacters(String literal) {
        return SPECIAL_REGEX_CHARS.matcher(literal).replaceAll("\\\\$0");
    }

    public static int getEnumOrdinal(Enum aEnum) {
        return aEnum.ordinal();
    }

    public static <K, V> K getFirstKeyByValue(Map<K, V> lookup, V value) {
        if (isEmptyOrNull(lookup) || value == null) {
            return null;
        }

        for (Map.Entry<K, V> entry : lookup.entrySet()) {
            if (isEqual(entry.getValue(), value)) {
                return entry.getKey();
            }
        }

        return null;
    }

    public static <T extends Enum<T>> EnumSet<T> complementOfEnumSet(Class<T> enumClass, Collection<T> currentValues) {
        // jw: because EnumSet.copy/complimentOf do not handle sets with no values, we will need to handle that manually.
        if (isEmptyOrNull(currentValues)) {
            return EnumSet.allOf(enumClass);
        }

        // jw: since we have values, EnumSet can just handle the heavy lifting now!
        return EnumSet.complementOf(EnumSet.copyOf(currentValues));
    }
}
