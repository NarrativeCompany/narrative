package org.narrative.common.util;

import org.narrative.common.persistence.OID;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Sorting implements the quickSort algortithm.  It also
 * provides comparers for different classes.
 *
 * @author Peter Bryant(pbryant@bigfoot.com)
 */
public final class Sorting {

    private static final NarrativeLogger logger = new NarrativeLogger(Sorting.class);

    /**
     * the interface used to compare (for sorting /searching purposes
     * a and b
     *
     * @author Peter Bryant(pbryant@bigfoot.com)
     */
    public static abstract class Comparer {
        /**
         * return negative number if a < b, 0 if a == b or +positive number if a > b
         */
        abstract public int compare(Object a, Object b);
    }

    /**
     * reversing the order of the given comparer
     */
    public final static class ReverseComparer extends Comparer {
        Comparer c;

        public ReverseComparer(Comparer c) {
            this.c = c;
        }

        public int compare(Object a, Object b) {
            return -1 * c.compare(a, b);
        }
    }

    /**
     * join together multiple comparers
     */
    public final static class ChainedComparers extends Comparer {
        Comparer c[];

        public ChainedComparers(Comparer _c[]) {
            this.c = _c;
        }

        public int compare(Object a, Object b) {
            for (int i = 0; i < c.length; i++) {
                int ret = c[i].compare(a, b);
                // return first mismatch
                if (ret != 0) {
                    return ret;
                }
            }
            return 0;
        }
    }

    /**
     * a noisy comparer
     */
    public final static class NoisyComparer extends Comparer {
        Comparer c;

        public NoisyComparer(Comparer c) {
            this.c = c;
        }

        public int compare(Object a, Object b) {
            int ret = c.compare(a, b);
            if (logger.isInfoEnabled()) {
                logger.info("" + a + " vs. " + b + " = " + ret);
            }
            return ret;
        }
    }

    /**
     * FieldComparer permits a data structure to be sorted by a particular field.
     */
    public static class FieldComparer extends Sorting.Comparer {
        Sorting.Comparer c;
        Field f;

        public FieldComparer(Field _f, Sorting.Comparer _c) {
            f = _f;
            c = _c;
        }

        public int compare(Object a, Object b) {
            try {
                a = a == null ? null : f.get(a);
                b = b == null ? null : f.get(b);
            } catch (IllegalAccessException iae) {
                Debug.assertMsg(logger, false, "Failed getting field " + f.getName(), iae);
            }
            return c.compare(a, b);
        }
    }

    public static class OIDComparer extends StringComparer {
        public OIDComparer() {
            super(false);
        }

        public int compare(Object a, Object b) {
            return super.compare(IPStringUtil.nullSafeToString(a), IPStringUtil.nullSafeToString(b));
        }
    }

    public static class StringComparer extends Comparer {
        boolean isCaseInsensitive = false;

        public StringComparer() {
            this(false);
        }

        /**
         * @param _isCaseInsensitive when true means "a"=="A" and
         *                           Ordering for strings that differ only by case is a coin-toss.
         */
        public StringComparer(boolean _isCaseInsensitive) {
            isCaseInsensitive = _isCaseInsensitive;
        }

        public int compare(Object a, Object b) {
            // compare based on nulls
            int nullCompare = (a == null ? (b == null ? 0 : -1) : (b == null ? 1 : 2));
            if (nullCompare < 2) {
                return nullCompare;
            }
            String sA = (String) a;
            String sB = (String) b;
            int len1 = sA.length();
            int len2 = sB.length();
            int n = Math.min(len1, len2);
            int i = 0;
            int j = 0;

            char ca[] = sA.toCharArray();
            char cb[] = sB.toCharArray();
            int caseRet = 0;
            while (i < n) {
                char c1 = ca[i++];
                char c2 = cb[j++];
                if (c1 == c2) {
                    continue;
                }

                // A != a.  but A is followed by a, not B
                char lc1 = Character.toLowerCase(c1);
                char lc2 = Character.toLowerCase(c2);
                if (lc1 != lc2) {
                    return lc1 - lc2 < 0 ? -1 : 1;
                }
                if (caseRet == 0 && !isCaseInsensitive) {
                    caseRet = c1 - c2;
                }
            }
            // longer string later
            if (len1 != len2) {
                return len1 - len2 < 0 ? -1 : 1;
            }
            // lower case string before
            return caseRet == 0 ? 0 : caseRet < 0 ? -1 : 1;
        }
    }

    /**
     * a compare method for plain old ints (cf. the Integer comparer)
     */
    static public int intCompare(int ia, int ib) {
        return (ia < ib ? -1 : ia == ib ? 0 : 1);
    }

    /**
     * adapts a comparer to expect an element at a given
     * index of an array.  i.e. c.compare(((Object[])a)[index], ((Object[])b)[index]);
     */
    public final static class ObjectArrayComparer extends Comparer {
        int index;
        Comparer c;

        public ObjectArrayComparer(Comparer c, int index) {
            this.c = c;
            this.index = index;
        }

        public int compare(Object a, Object b) {
            return c.compare(((Object[]) a)[index], ((Object[]) b)[index]);
        }
    }

    /**
     * get the singleton instance of Sorting
     */
    public static Sorting getInstance() {
        return instance;
    }

    /**
     * get a comparer for the given class
     */
    public Comparer getComparer(Class c) {
        return (Comparer) comparers.get(c);
    }

    /**
     * get the reverse order comparer for the given class
     */
    public Comparer getReverseComparer(Class c) {
        return (Comparer) reverseComparers.get(c);
    }

    /**
     * register your own comparer for a given class
     */
    public void registerComparer(Class c, Comparer comparer) {
        comparers.put(c, comparer);
    }

    static Sorting instance = new Sorting();
    Hashtable comparers = new Hashtable(10);
    Hashtable reverseComparers = new Hashtable(10);
    public static final Comparer hashcodeComparer = new Comparer() {
        public int compare(Object a, Object b) {
            int i = (a == null ? (b == null ? 0 : -1) : (b == null ? 1 : 2));
            if (i < 2) {
                return i;
            }
            if (a.equals(b)) {
                return 0;
            }
            int ia = a.hashCode();
            int ib = b.hashCode();
            if (ia == ib) {
                return Sorting.getInstance().getComparer(String.class).compare(a, b);
            }
            return (ia < ib ? -1 : ia == ib ? 0 : 1);
        }
    };

    /**
     * sets up comparers for String, Integer, Date, Boolean, Character
     */
    Sorting() {
        comparers.put(String.class, new StringComparer());
        comparers.put(OID.class, new OIDComparer());
        comparers.put(Integer.class, new Comparer() {
            public int compare(Object a, Object b) {
                int i = (a == null ? (b == null ? 0 : -1) : (b == null ? 1 : 2));
                if (i < 2) {
                    return i;
                }
                int ia = ((Integer) (a)).intValue();
                int ib = ((Integer) (b)).intValue();
                return (ia < ib ? -1 : ia == ib ? 0 : 1);
            }
        });
        comparers.put(Boolean.class, new Comparer() {
            public int compare(Object a, Object b) {
                int i = (a == null ? (b == null ? 0 : -1) : (b == null ? 1 : 2));
                if (i < 2) {
                    return i;
                }
                boolean ia = ((Boolean) (a)).booleanValue();
                boolean ib = ((Boolean) (b)).booleanValue();
                return (ia == ib ? 0 : !ia ? -1 : 1);
            }
        });
        comparers.put(Character.class, new Comparer() {
            public int compare(Object a, Object b) {
                int i = (a == null ? (b == null ? 0 : -1) : (b == null ? 1 : 2));
                if (i < 2) {
                    return i;
                }
                char ca = ((Character) (a)).charValue();
                char cb = ((Character) (b)).charValue();
                return (ca < cb ? -1 : ca == cb ? 0 : 1);
            }
        });
        comparers.put(Date.class, new Comparer() {
            public int compare(Object a, Object b) {
                int i = (a == null ? (b == null ? 0 : -1) : (b == null ? 1 : 2));
                if (i < 2) {
                    return i;
                }
                Date da = (Date) a;
                Date db = (Date) b;
                return (da.before(db) ? -1 : da.after(db) ? 1 : 0);
            }
        });
        reverseComparers = new Hashtable(10);
        reverseComparers.put(String.class, new Comparer() {
            public int compare(Object b, Object a) {
                int i = (a == null ? (b == null ? 0 : -1) : (b == null ? 1 : 2));
                if (i < 2) {
                    return i;
                }
                return ((String) a).compareTo((String) b);
            }
        });
        comparers.put(Character.class, new Comparer() {
            public int compare(Object b, Object a) {
                int i = (a == null ? (b == null ? 0 : -1) : (b == null ? 1 : 2));
                if (i < 2) {
                    return i;
                }
                char ca = ((Character) (a)).charValue();
                char cb = ((Character) (b)).charValue();
                return (ca < cb ? -1 : ca == cb ? 0 : 1);
            }
        });
        reverseComparers.put(Integer.class, new Comparer() {
            public int compare(Object b, Object a) {
                int i = (a == null ? (b == null ? 0 : -1) : (b == null ? 1 : 2));
                if (i < 2) {
                    return i;
                }
                int ia = ((Integer) (a)).intValue();
                int ib = ((Integer) (b)).intValue();
                return (ia < ib ? -1 : ia == ib ? 0 : 1);
            }
        });
        reverseComparers.put(Date.class, new Comparer() {
            public int compare(Object b, Object a) {
                int i = (a == null ? (b == null ? 0 : -1) : (b == null ? 1 : 2));
                if (i < 2) {
                    return i;
                }
                Date da = (Date) a;
                Date db = (Date) b;
                return (da.before(db) ? -1 : da.after(db) ? 1 : 0);
            }
        });
    }

    /**
     * sort v according to c
     */
    static public Vector getSortedArray(Vector v, Comparer c) {
        Object ov[] = new Object[v.size()];
        v.copyInto(ov);
        quickSortInPlace(ov, 0, ov.length - 1, c);
        Vector ret = new Vector();
        for (int i = 0; i < ov.length; i++) {
            ret.addElement(ov[i]);
        }
        return ret;
    }

    static public Object[] getSortedArray(Object v[], Comparer c, Class arrayOfObjectsOfThisType) {
        if (v == null) {
            return null;
        }
        Object ret[] = (Object[]) java.lang.reflect.Array.newInstance(arrayOfObjectsOfThisType, v.length);
        System.arraycopy(v, 0, ret, 0, v.length);
        quickSortInPlace(ret, 0, ret.length - 1, c);
        return ret;
    }

    /**
     * sort v according to c
     */
    static public void quickSortInPlace(Object v[], Comparer c) {
        quickSortInPlace(v, 0, v.length - 1, c);
    }

    /**
     * find key in a according to c.  a negative number if not found (negative number indicates where
     * search failed in the sorted list)
     */
    static public int binarySearch(Vector a, Object key, Comparer c) {
        Object ov[] = new Object[a.size()];
        a.copyInto(ov);
        return binarySearch(ov, key, c);
    }

    public static boolean isObjectInUnsortedList(Object unsortedArray[], Object key, Comparer c) {
        for (int i = 0; unsortedArray != null && i < unsortedArray.length; i++) {
            if (0 == c.compare(key, unsortedArray[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * find key in a according to c.  a negative number if not found (negative number indicates where
     * search failed in the sorted list)
     */
    static public int binarySearch(Object a[], Object key, Comparer c) {
        int low = 0;
        int high = a.length - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            Object midVal = a[mid];
            int cmp = c.compare(midVal, key);

            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return mid; // key found
            }
        }
        return -(low + 1);  // key not found.
    }

    /*
    public static void slowBubbleSort(Object v[], Comparer c) {
        for(int iFirst=0;iFirst<v.length-1;iFirst++) {
            for(int iSecond=iFirst+1;iSecond<v.length;iSecond++) {
                int comp = c.compare(v[iFirst], v[iSecond]);
                if(logger.isInfoEnabled()) logger.info( "" + comp + " = " + v[iFirst] + " vs " + v[iSecond]);
                if(comp>0) {
                    swap(v,iFirst,iSecond);
                }
            }
        }
    }
    */
    private static void quickSortInPlace(Object[] a, int lo0, int hi0, Comparer c) {
        int lo = lo0;
        int hi = hi0;

        if (lo >= hi) {
            return;
        }

        Object mid = a[(lo + hi) / 2];
        while (lo < hi) {
            while (lo < hi && c.compare(a[lo], mid) < 0) {
                lo++;
            }
            while (lo < hi && c.compare(a[hi], mid) > 0) {
                hi--;
            }
            if (lo < hi) {
                Object T = a[lo];
                a[lo] = a[hi];
                a[hi] = T;
                lo++;
                hi--;
            }
        }
        if (hi < lo) {
            int T = hi;
            hi = lo;
            lo = T;
        }
        quickSortInPlace(a, lo0, lo, c);
        quickSortInPlace(a, lo == lo0 ? lo + 1 : lo, hi0, c);
    }

    /**
     * sorts strings by whether they are empty (null or 0 length).
     * (use it where you want equivalency for null and "")
     */
    public static class EmptyStringComparer extends Comparer {
        public int compare(Object _a, Object _b) {
            String a = (String) _a;
            String b = (String) _b;
            boolean isAEmpty = IPStringUtil.isEmpty(a);
            boolean isBEmpty = IPStringUtil.isEmpty(b);
            if (isAEmpty && isBEmpty) {
                return 0;
            }
            if (isAEmpty) {
                return -1;
            }
            return 1;
        }
    }

    public static final Comparer stringComparer = Sorting.getInstance().getComparer(String.class);

    /**
     * A String sorter that also knows how to match a String
     * to StringPrefix.  e.g. StringPrefix 'foo' will match String 'foobar'
     */
    public static class StringPrefixComparer extends Sorting.Comparer {
        public static interface StringPrefix {
            String getPrefix();
        }

        public static interface FullString {
            String getFullString();
        }

        public static StringPrefixComparer instance = new StringPrefixComparer();

        public int compare(Object a, Object b) {
            String sa = a instanceof FullString ? ((FullString) a).getFullString() : (a instanceof StringPrefix ? ((StringPrefix) a).getPrefix() : (String) a);
            String sb = b instanceof FullString ? ((FullString) b).getFullString() : (b instanceof StringPrefix ? ((StringPrefix) b).getPrefix() : (String) b);
            // both prefixes...
            if (a instanceof StringPrefix && b instanceof StringPrefix) {
                return Sorting.stringComparer.compare(((StringPrefix) a).getPrefix(), ((StringPrefix) b).getPrefix());
            }
            // both fullstrings/strings...
            if ((a instanceof FullString || a instanceof String) && (b instanceof FullString || b instanceof String)) {
                return Sorting.stringComparer.compare(sa, sb);
            }
            // mix of fullstring/string and prefix
            {
                String fullString = null;
                String prefix = null;
                if (a instanceof FullString || a instanceof String) {
                    fullString = sa;
                    prefix = sb;
                } else {
                    fullString = sb;
                    prefix = sa;
                }
                // this is the crux of the class, here.  Basically this bit will match, e.g., a
                // a specifc address of 127.0.0.1 to an address prefix of 127.0.
                if (fullString.startsWith(prefix)) {
                    return 0;
                }
            }
            return Sorting.stringComparer.compare(sa, sb);
        }

        ;
    }
}
