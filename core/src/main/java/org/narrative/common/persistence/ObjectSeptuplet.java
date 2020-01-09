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
public class ObjectSeptuplet<T1, T2, T3, T4, T5, T6, T7> extends ObjectSextuplet<T1, T2, T3, T4, T5, T6> {
    protected final T7 seven;

    public ObjectSeptuplet(T1 one, T2 two, T3 three, T4 four, T5 five, T6 six, T7 seven) {
        super(one, two, three, four, five, six);
        this.seven = seven;
    }

    public T7 getSeven() {
        return seven;
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

        ObjectSeptuplet that = (ObjectSeptuplet) o;

        if (seven != null ? !seven.equals(that.seven) : that.seven != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (seven != null ? seven.hashCode() : 0);
        return result;
    }

    protected String getValues() {
        return super.getValues() + ", " + seven;
    }

    public static <T1, T2, T3, T4, T5, T6, T7, OS extends ObjectSeptuplet<T1, T2, T3, T4, T5, T6, T7>> List<T7> getAllSevens(Collection<OS> septuplets) {
        List<T7> ret = new ArrayList<T7>(septuplets.size());
        for (OS septuplet : septuplets) {
            ret.add(septuplet.getSeven());
        }
        return ret;
    }
}
