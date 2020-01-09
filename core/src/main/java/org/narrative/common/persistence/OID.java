package org.narrative.common.persistence;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.narrative.common.persistence.jackson.OIDJacksonDeserializer;
import org.narrative.common.persistence.jackson.OIDJacksonSerializer;
import org.narrative.common.util.NarrativeLogger;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.Sorting;
import org.narrative.common.util.UnexpectedError;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Sep 2, 2004
 * Time: 5:12:40 PM
 * This class encapsulates our oids;
 */
@JsonSerialize(using = OIDJacksonSerializer.class)
@JsonDeserialize(using = OIDJacksonDeserializer.class)
public class OID implements Serializable, Comparable<OID> {
    // the serialVersionUID value reported when there was a compatibility issue.
    private static final long serialVersionUID = -8929103349374866873L;

    public static final String TYPE = "org.narrative.common.persistence.OID";

    private static final NarrativeLogger logger = new NarrativeLogger(OID.class);

    public static final OID DUMMY_OID = new OID(0);

    protected final long oid;
    /**
     * bl: don't want the cache String OID to be Serialized,
     * so making it transient.
     */
    protected transient String sOID;

    /**
     * Creates an OID object with a null value
     *
     * @deprecated shouldn't ever use this.  it's only here for Hibernate's purposes.
     */
    public OID() {
        this(-1);
    }

    /**
     * Creates an OID based on a long integer.
     *
     * @param oid The long integer that representes the value of this oid.
     */
    public OID(long oid) {
        this.oid = oid;
    }

    /**
     * Creates an OID based on a long integer.
     *
     * @param oid The long integer that representes the value of this oid.
     */
    public OID(BigInteger oid) {
        this(oid.longValue());
    }

    /**
     * Creates an OID based on a string.
     * Will throw a NumberFormatException if the specified string is empty.
     *
     * @param oid The string that representes the value of this oid.
     * @throws NumberFormatException if the specified string can't be parsed into a long.
     */
    public OID(String oid) throws NumberFormatException {
        this(Long.parseLong(oid));
        // bl: used to set the sOID here as well.  i'm choosing not to.  just in case
        // the string has leading zeros, this would cause problems.  let's let toString()
        // handle converting the long to a string.
        //this.sOID = oid;
    }

    /**
     * get an OID object from a Long object.
     *
     * @param oidValue the oid Long object to get an OID object for.
     * @return an OID object representing the supplied oid Long object.
     * if the supplied Long object is null, this method will also return null.
     */
    @Nullable
    public static OID valueOf(@Nullable Number oidValue) {
        if (oidValue == null) {
            return null;
        }
        return new OID(oidValue.longValue());
    }

    public static OID valueOf(@Nullable Number oidValue, boolean positiveOnly) {
        if (oidValue == null || (positiveOnly && oidValue.longValue() <= 0)) {
            return null;
        }
        return new OID(oidValue.longValue());
    }

    /**
     * get an OID object from an oid value string.
     * if the oidValue is null, returns the NULL_OID.
     * otherwise, returns an OID instance.
     * if the String contains a non-parseable long,
     * a NumberFormatException will be raised.
     *
     * @param oidValue the oid value to get an OID object for
     * @return an OID object representing the supplied oid value string
     * @throws NumberFormatException if the String is non-empty and is a non-parseable long
     */
    @Nullable
    public static OID valueOf(@Nullable String oidValue) throws NumberFormatException {
        if (IPStringUtil.isEmpty(oidValue)) {
            return null;
        }
        return new OID(oidValue);
    }

    /**
     * This function will return either a null, if oidValue is null, an OID based on the number suplied if oidValue is
     * a numericString or an OID version of the hashCode of oidValue if its not a number
     *
     * @param oidValue
     * @return
     * @throws NumberFormatException
     */
    @Nullable
    @SuppressWarnings("squid:S2259")
    public static OID valueOfNonNumeric(@Nullable String oidValue) throws NumberFormatException {
        if (IPStringUtil.isEmpty(oidValue)) {
            return null;
        }
        if (IPStringUtil.isNumber(oidValue)) {
            return new OID(oidValue);
        }

        return new OID(oidValue.hashCode()); // squid:S2259 Null pointer is not dereferenced here. Verified by unit test.
    }

    /**
     * get an OID object from an oid value String.  if the string
     * is empty or the String doesn't contain a parseable long,
     * null will be returned
     *
     * @param oidValue the String value of the OID
     * @return a null reference if the string is empty or doesn't contain a parseable OID.
     * otherwise, returns an OID instance.
     */
    @Nullable
    public static OID getOIDFromString(@Nullable String oidValue) {
        try {
            return valueOf(oidValue);
        } catch (NumberFormatException nfe) {
            // if the string wasn't a parseable long, then
            // return null.
            return null;
        }
    }

    /**
     * The primitive long representation of the oid.
     *
     * @return the long value of the oid
     */
    public long getValue() {
        return oid;
    }

    public BigInteger getBigInt() {
        return BigInteger.valueOf(oid);
    }

    /**
     * Returns the value of the oid as a string.  Will return empty string when the oid is null.
     *
     * @return the string representation of this OID.  cached internally so that converting the same
     * OID to a string multiple times will not result in multiple Long.toString() calls.
     */
    public String toString() {
        //doing this for performance because this method is called tons of times.
        if (sOID == null) {
            sOID = Long.toString(oid);
        }

        return sOID;
    }

    /**
     * Returns true if the oid passed in has the same underlying long value.
     *
     * @param o
     * @return true if the supplied oid has the same value as this oid
     */
    public boolean equals(OID o) {
        //if either value is null then return false
        if (o == null) {
            return false;
        }

        return o.oid == this.oid;
    }

    /**
     * Returns true if the oid passed in has the same underlying long value.
     *
     * @param o
     * @return true if the supplied object has the same oid value as this oid.
     * will compare string and long values as necessary based on the supplied object's type.
     */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof OID) {
            return equals((OID) o);
        }
        if (o instanceof String) {
            return toString().equalsIgnoreCase((String) o);
        }
        if (o instanceof Long) {
            return oid == ((Long) o).longValue();
        }

        return false;
    }

    /**
     * bl: just return the hashCode used by the Long object.
     * direct copy and paste from the Long javadoc.
     * The result is the exclusive OR of the two halves of
     * the primitive long value held by this OID's long value.
     *
     * @return the hashCode for this OID
     */
    public int hashCode() {
        return Long.hashCode(oid);
    }

    public boolean hasNiceEquals() {
        return false;
    }

    public Class returnedClass() {
        return OID.class;
    }

    /**
     * Returns a string array of oids given a collection of OIDs
     *
     * @param oidCollection the Collection of OIDs to convert to a String[]
     * @return a String[] with the OID values
     */
    public static String[] getStringArrayFromOIDCollection(Collection<OID> oidCollection) {
        if (oidCollection == null || oidCollection.isEmpty()) {
            return IPStringUtil.EMPTY_STRING_ARRAY;
        }
        return getStringArrayFromOIDArray(oidCollection.toArray(new OID[]{}));
    }

    public static String[] getStringArrayFromOIDArray(OID[] oids) {
        return IPStringUtil.getStringArrayFromObjectArray(oids);
    }

    /**
     * Returns an OID array given an array of strings representing oids
     *
     * @param sOIDs the array of Strings representing OIDs
     * @return an OID array containing the OIDs in the String array
     */
    public static OID[] getOIDArray(List<String> sOIDs) {
        if (sOIDs == null || sOIDs.size() == 0) {
            return new OID[]{};
        }

        OID[] oids = new OID[sOIDs.size()];

        for (int i = 0; i < sOIDs.size(); i++) {
            oids[i] = OID.valueOf(sOIDs.get(i));
        }
        return oids;
    }

    /**
     * Returns an OID list given an array of strings representing oids
     *
     * @param sOIDs a String array of OIDs
     * @return a List of OID objects
     */
    public static List<OID> getOIDList(Collection<String> sOIDs) {
        if (sOIDs == null) {
            return Collections.emptyList();
        }
        List<OID> oids = new ArrayList<OID>(sOIDs.size());
        for (String oidStr : sOIDs) {
            oids.add(OID.valueOf(oidStr));
        }
        return oids;
    }

    public static List<OID> getOIDListFromUnknownValues(List<String> sOIDs) {
        if (sOIDs == null) {
            return Collections.emptyList();
        }
        List<OID> oids = new ArrayList<OID>(sOIDs.size());
        for (String oidStr : sOIDs) {
            OID oid = OID.getOIDFromString(oidStr);
            if (oid != null) {
                oids.add(oid);
            }
        }
        return oids;
    }

    /**
     * Creates a List of OIDs given a comma delimited string of oids
     *
     * @param oids the delimited string of OIDs
     * @return a List with the OIDs
     */
    public static List<OID> getOIDListFromDelimitedString(String oids) {
        return getOIDList(IPStringUtil.getListFromDelimitedString(oids));
    }

    public int compareTo(OID o) {
        return Long.valueOf(this.oid).compareTo(Long.valueOf(o.oid));
    }

    public static Set<OID> getOIDSet(OID... oids) {
        Set<OID> set = new HashSet<OID>();
        for (OID oid : oids) {
            set.add(oid);
        }

        return set;

    }

    public static Set<OID> getOIDSet(String... oids) {
        Set<OID> set = new HashSet<OID>();
        for (String oid : oids) {
            set.add(OID.valueOf(oid));
        }

        return set;

    }

    public static List<OID> getOIDsFromCollection(Collection<Number> numbers) {
        if (numbers == null) {
            return Collections.emptyList();
        }
        List<OID> ret = newArrayList(numbers.size());
        for (Number number : numbers) {
            ret.add(valueOf(number));
        }
        return ret;
    }

    public static OID valueOf(Object oid) {
        if (oid == null) {
            return null;
        }

        if (oid instanceof OID) {
            logger.error("Called OID.valueOf(Object) and supplied an OID!  unnecessary call.  may as well remove.", new Throwable());
            return (OID) oid;
        }

        if (oid instanceof Number) {
            return valueOf((Number) oid);
        }

        if (oid instanceof Object[]) {
            Object[] objs = (Object[]) oid;
            if (objs.length == 0 || objs[0] == null) {
                return null;
            }

            if (objs.length > 1) {
                throw UnexpectedError.getRuntimeException("Called OID.valueOf with multi value array.  Not supported");
            }
            return valueOf(objs[0]);
        }
        if (oid instanceof Collection) {
            Collection values = (Collection) oid;
            if (values.isEmpty()) {
                return null;
            }

            if (values.size() > 1) {
                throw UnexpectedError.getRuntimeException("Called OID.valueOf with multi value collection.  Not supported");
            }

            return valueOf(values.iterator().next());
        }

        return valueOf(oid.toString());
    }

    /**
     * get an array of OIDs from a String of delimited OIDs
     *
     * @param oids the delimited string of OIDs
     * @return an OID array containing oids represented in the supplied delimited string
     */
    public static OID[] getOIDArrayFromDelimitedString(String oids) {
        return getOIDArray(IPStringUtil.getListFromDelimitedString(oids));
    }

    /**
     * get an array of sorted OIDs.  sorts the OIDs by
     * their String representation (not long values)
     *
     * @param oids the OID array to sort
     * @return the sorted array of OIDs
     */
    public static OID[] getSortedOIDArray(OID[] oids) {
        if (oids == null || oids.length < 2) {
            return oids;
        }
        Sorting sorter = Sorting.getInstance();
        Sorting.Comparer comp = sorter.getComparer(OID.class);
        oids = (OID[]) Sorting.getSortedArray(oids, comp, OID.class);
        return oids;
    }

    public static int compareOids(OID oid1, OID oid2) {
        if (oid1 == null && oid2 == null) {
            return 0;
        }
        // bl: changing so that all null OID values go to the end of the list instead of the beginning
        if (oid1 == null) {
            return 1;
        }
        if (oid2 == null) {
            return -1;
        }
        return oid1.compareTo(oid2);
    }

    /**
     * bl: this shouldn't be necessary.  should just be able to use the OID directly.
     * A bean friendly string value for oid.  for JSP expressions
     * @return
     */
    /*public String getValueAsString() {
        return toString();
    }*/
}
