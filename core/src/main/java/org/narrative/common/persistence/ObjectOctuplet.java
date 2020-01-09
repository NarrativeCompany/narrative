package org.narrative.common.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Apr 26, 2006
 * Time: 1:52:46 PM
 */
public class ObjectOctuplet<T1, T2, T3, T4, T5, T6, T7, T8> extends ObjectSeptuplet<T1, T2, T3, T4, T5, T6, T7> {
    protected final T8 eight;

    public ObjectOctuplet(T1 one, T2 two, T3 three, T4 four, T5 five, T6 six, T7 seven, T8 eight) {
        super(one, two, three, four, five, six, seven);
        this.eight = eight;
    }

    public T8 getEight() {
        return eight;
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

        ObjectOctuplet that = (ObjectOctuplet) o;

        if (eight != null ? !eight.equals(that.eight) : that.eight != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (eight != null ? eight.hashCode() : 0);
        return result;
    }

    protected String getValues() {
        return super.getValues() + ", " + eight;
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, OO extends ObjectOctuplet<T1, T2, T3, T4, T5, T6, T7, T8>> List<T8> getAllEights(Collection<OO> octuplets) {
        List<T8> ret = new ArrayList<T8>(octuplets.size());
        for (OO octuplet : octuplets) {
            ret.add(octuplet.getEight());
        }
        return ret;
    }
}
