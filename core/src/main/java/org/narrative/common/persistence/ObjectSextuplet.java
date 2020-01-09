package org.narrative.common.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
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
public class ObjectSextuplet<T1, T2, T3, T4, T5, T6> extends ObjectQuintuplet<T1, T2, T3, T4, T5> {
    protected final T6 six;

    public ObjectSextuplet(T1 one, T2 two, T3 three, T4 four, T5 five, T6 six) {
        super(one, two, three, four, five);
        this.six = six;
    }

    public T6 getSix() {
        return six;
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

        ObjectSextuplet that = (ObjectSextuplet) o;

        if (six != null ? !six.equals(that.six) : that.six != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (six != null ? six.hashCode() : 0);
        return result;
    }

    protected String getValues() {
        return super.getValues() + ", " + six;
    }

    public static <T1, T2, T3, T4, T5, T6, OS extends ObjectSextuplet<T1, T2, T3, T4, T5, T6>> List<T6> getAllSixes(Collection<OS> sextuplets) {
        List<T6> ret = new ArrayList<T6>(sextuplets.size());
        for (OS sextuplet : sextuplets) {
            ret.add(sextuplet.getSix());
        }
        return ret;
    }

    public static <T1, T2, T3, T4, T5, T6, OQ extends ObjectSextuplet<T1, T2, T3, T4, T5, T6>> Map<T6, Set<T1>> getAsMapOfSixesToOneSet(Collection<OQ> sextuplets) {
        Map<T6, Set<T1>> ret = new LinkedHashMap<>();
        for (OQ sextuplet : sextuplets) {
            addMapSetLookupValue(ret, sextuplet.getSix(), sextuplet.getOne());
        }
        return ret;
    }
}
