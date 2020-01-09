package org.narrative.common.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Apr 26, 2006
 * Time: 1:13:10 PM
 */
public class ObjectTriplet<T1, T2, T3> extends ObjectPair<T1, T2> {
    private static final long serialVersionUID = 8493000655890133878L;

    protected final T3 three;

    public ObjectTriplet(T1 one, T2 two, T3 three) {
        super(one, two);
        this.three = three;
    }

    public T3 getThree() {
        return three;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ObjectTriplet that = (ObjectTriplet) o;

        if (three != null ? !three.equals(that.three) : that.three != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (three != null ? three.hashCode() : 0);
        return result;
    }

    protected String getValues() {
        return super.getValues() + ", " + three;
    }

    public static <T1, T2, T3, OT extends ObjectTriplet<T1, T2, T3>> List<T3> getAllThrees(Collection<OT> triplets) {
        List<T3> ret = new ArrayList<T3>(triplets.size());
        for (OT triplet : triplets) {
            ret.add(triplet.getThree());
        }
        return ret;
    }

    public static <T1, T2, T3, OT extends ObjectTriplet<T1, T2, T3>> Set<T3> getUniqueThrees(Collection<OT> triplets) {
        Set<T3> ret = new LinkedHashSet<T3>();
        for (OT triplet : triplets) {
            ret.add(triplet.getThree());
        }
        return ret;
    }

    public static <T1, T2, T3, OT extends ObjectTriplet<T1, T2, T3>> Map<T1, Map<T2, T3>> getOnesToTwosToThrees(Collection<OT> triplets) {
        Map<T1, Map<T2, T3>> ret = new HashMap<T1, Map<T2, T3>>();
        for (OT triplet : triplets) {
            Map<T2, T3> map = ret.get(triplet.getOne());
            if (map == null) {
                ret.put(triplet.getOne(), map = new HashMap<T2, T3>());
            }
            map.put(triplet.getTwo(), triplet.getThree());
        }
        return ret;
    }

    public static <T1, T2, T3, OT extends ObjectTriplet<T1, T2, T3>> Map<T1, Map<T2, List<T3>>> getOnesToTwosToThreesList(Collection<OT> triplets) {
        Map<T1, Map<T2, List<T3>>> ret = newHashMap();
        for (OT triplet : triplets) {
            Map<T2, List<T3>> map = ret.get(triplet.getOne());
            if (map == null) {
                ret.put(triplet.getOne(), map = newHashMap());
            }
            addMapListLookupValue(map, triplet.getTwo(), triplet.getThree());
        }
        return ret;
    }

    public static <T1, T2, T3> Map<T1, ObjectPair<T2, T3>> getOnesToObjectPairOfTwoAndThree(List<ObjectTriplet<T1, T2, T3>> triplets) {
        // jw: using a linked hashmap here so that we will maintain order!
        Map<T1, ObjectPair<T2, T3>> ret = newLinkedHashMap();
        for (ObjectTriplet<T1, T2, T3> triplet : triplets) {
            ret.put(triplet.getOne(), newObjectPair(triplet.getTwo(), triplet.getThree()));
        }
        return ret;
    }

    public static <T1, T2, T3, OP extends ObjectPair<T2, T3>, OT extends ObjectTriplet<T1, T2, T3>> Map<T1, List<OP>> getOnesToListOfObjectPairsOfTwoAndThree(List<OT> triplets) {
        Map<T1, List<OP>> ret = newHashMap();
        for (OT triplet : triplets) {
            List<OP> objectPairs = ret.get(triplet.getOne());
            if (objectPairs == null) {
                ret.put(triplet.getOne(), objectPairs = newLinkedList());
            }
            objectPairs.add((OP) newObjectPair(triplet.getTwo(), triplet.getThree()));
        }
        return ret;
    }

    public static <T1, T2, T3, OT extends ObjectTriplet<T1, T2, T3>> Map<T2, T3> getMapOfTwosToThrees(List<OT> triplets) {
        Map<T2, T3> ret = newHashMap();
        for (OT triplet : triplets) {
            ret.put(triplet.getTwo(), triplet.getThree());
        }
        return ret;
    }

    public static <T1, T2, T3, OT extends ObjectTriplet<T1, T2, T3>> Map<T3, Set<T1>> getAsMapOfThreesToOnesSet(Collection<OT> triplets) {
        Map<T3, Set<T1>> ret = new HashMap<>();
        for (OT triplet : triplets) {
            addMapSetLookupValue(ret, triplet.getThree(), triplet.getOne());
        }
        return ret;
    }

    public static <T1, T2, T3, OT extends ObjectTriplet<T1, T2, T3>> Map<T1, ObjectPair<T2, T3>> getAsMapOfOnesToTwoThreePairs(List<OT> triplets) {
        Map<T1, ObjectPair<T2, T3>> ret = newHashMap();
        for (OT triplet : triplets) {
            ret.put(triplet.getOne(), new ObjectPair<>(triplet.getTwo(), triplet.getThree()));
        }
        return ret;
    }
}
