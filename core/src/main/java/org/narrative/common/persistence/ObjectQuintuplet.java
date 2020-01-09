package org.narrative.common.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Apr 26, 2006
 * Time: 1:52:46 PM
 */
public class ObjectQuintuplet<T1, T2, T3, T4, T5> extends ObjectQuadruplet<T1, T2, T3, T4> {
    protected final T5 five;

    public ObjectQuintuplet(T1 one, T2 two, T3 three, T4 four, T5 five) {
        super(one, two, three, four);
        this.five = five;
    }

    public T5 getFive() {
        return five;
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

        ObjectQuintuplet that = (ObjectQuintuplet) o;

        if (five != null ? !five.equals(that.five) : that.five != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (five != null ? five.hashCode() : 0);
        return result;
    }

    protected String getValues() {
        return super.getValues() + ", " + five;
    }

    public static <T1, T2, T3, T4, T5, OQ extends ObjectQuintuplet<T1, T2, T3, T4, T5>> List<T5> getAllFives(Collection<OQ> quintuplets) {
        List<T5> ret = new ArrayList<T5>(quintuplets.size());
        for (OQ quintuplet : quintuplets) {
            ret.add(quintuplet.getFive());
        }
        return ret;
    }

    public static <T1, T2, T3, T4, T5, OQ extends ObjectQuintuplet<T1, T2, T3, T4, T5>> Map<T5, OQ> getAsMapOfFivesToObjs(Collection<OQ> quintuplets) {
        Map<T5, OQ> ret = newLinkedHashMap();
        for (OQ quintuplet : quintuplets) {
            ret.put(quintuplet.getFive(), quintuplet);
        }
        return ret;
    }

    public static <T1, T2, T3, T4, T5, OQ extends ObjectQuintuplet<T1, T2, T3, T4, T5>> Map<T5, Set<T4>> getAsMapOfFivesToFoursSet(List<OQ> quintuplets) {
        Map<T5, Set<T4>> ret = newLinkedHashMap();
        for (OQ quintuplet : quintuplets) {
            addMapSetLookupValue(ret, quintuplet.getFive(), quintuplet.getFour());
        }
        return ret;

    }

    public static <T1, T2, T3, T4, T5, OQ extends ObjectQuintuplet<T1, T2, T3, T4, T5>> Map<T5, Set<T1>> getAsMapOfFivesToOnesSet(List<OQ> quintuplets) {
        Map<T5, Set<T1>> ret = newLinkedHashMap();
        for (OQ quintuplet : quintuplets) {
            addMapSetLookupValue(ret, quintuplet.getFive(), quintuplet.getOne());
        }
        return ret;

    }
}
