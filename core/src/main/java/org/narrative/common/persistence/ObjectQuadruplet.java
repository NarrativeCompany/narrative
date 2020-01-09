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
 * Date: May 8, 2006
 * Time: 10:42:26 AM
 */
public class ObjectQuadruplet<T1, T2, T3, T4> extends ObjectTriplet<T1, T2, T3> {
    protected final T4 four;

    public ObjectQuadruplet(T1 one, T2 two, T3 three, T4 four) {
        super(one, two, three);
        this.four = four;
    }

    public T4 getFour() {
        return four;
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

        ObjectQuadruplet that = (ObjectQuadruplet) o;

        if (four != null ? !four.equals(that.four) : that.four != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (four != null ? four.hashCode() : 0);
        return result;
    }

    protected String getValues() {
        return super.getValues() + ", " + four;
    }

    public static <T1, T2, T3, T4, OQ extends ObjectQuadruplet<T1, T2, T3, T4>> List<T4> getAllFours(Collection<OQ> quadruplets) {
        List<T4> ret = new ArrayList<T4>(quadruplets.size());
        for (OQ quadruplet : quadruplets) {
            ret.add(quadruplet.getFour());
        }
        return ret;
    }

    public static <T1, T2, T3, T4, OQ extends ObjectQuadruplet<T1, T2, T3, T4>> Map<T3, Set<T4>> getAsMapOfThreesToFoursSet(Collection<OQ> quads) {
        Map<T3, Set<T4>> ret = newLinkedHashMap();
        for (OQ quad : quads) {
            Set<T4> set = ret.get(quad.getThree());
            if (set == null) {
                ret.put(quad.getThree(), set = newLinkedHashSet());
            }
            set.add(quad.getFour());
        }
        return ret;
    }

    public static <T1, T2, T3, T4, OT extends ObjectTriplet<T2, T3, T4>, OQ extends ObjectQuadruplet<T1, T2, T3, T4>> Map<T1, List<OT>> getOnesToListOfObjectTripletsOfTwoThreeAndFour(List<OQ> quads) {
        Map<T1, List<OT>> ret = newHashMap();
        for (OQ quad : quads) {
            List<OT> objectTriplets = ret.get(quad.getOne());
            if (objectTriplets == null) {
                ret.put(quad.getOne(), objectTriplets = newLinkedList());
            }
            objectTriplets.add((OT) newObjectTriplet(quad.getTwo(), quad.getThree(), quad.getFour()));
        }
        return ret;
    }

    public static <T1, T2, T3, T4, OQ extends ObjectQuadruplet<T1, T2, T3, T4>> Map<T4, Set<T3>> getAsMapOfFoursToThreesSet(List<OQ> quadruplets) {
        Map<T4, Set<T3>> ret = new HashMap<>();
        for (OQ quadruplet : quadruplets) {
            addMapSetLookupValue(ret, quadruplet.getFour(), quadruplet.getThree());
        }
        return ret;
    }

    public static <T1, T2, T3, T4, OQ extends ObjectQuadruplet<T1, T2, T3, T4>> Map<T4, Set<T1>> getAsMapOfFoursToOnesSet(List<OQ> quadruplets) {
        Map<T4, Set<T1>> ret = new HashMap<>();
        for (OQ quadruplet : quadruplets) {
            addMapSetLookupValue(ret, quadruplet.getFour(), quadruplet.getOne());
        }
        return ret;
    }

    public static <T1, T2, T3, T4, OQ extends ObjectQuadruplet<T1, T2, T3, T4>> Set<T4> getUniqueFours(Collection<OQ> triplets) {
        Set<T4> ret = new LinkedHashSet<T4>();
        for (OQ quadruplet : triplets) {
            ret.add(quadruplet.getFour());
        }
        return ret;
    }
}
