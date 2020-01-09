package org.narrative.common.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Date: 11/7/14
 * Time: 4:16 PM
 *
 * @author brian
 */
public class ObjectNonuplet<T1, T2, T3, T4, T5, T6, T7, T8, T9> extends ObjectOctuplet<T1, T2, T3, T4, T5, T6, T7, T8> {
    private final T9 nine;

    public ObjectNonuplet(T1 one, T2 two, T3 three, T4 four, T5 five, T6 six, T7 seven, T8 eight, T9 nine) {
        super(one, two, three, four, five, six, seven, eight);
        this.nine = nine;
    }

    public T9 getNine() {
        return nine;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ObjectNonuplet)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ObjectNonuplet that = (ObjectNonuplet) o;

        if (nine != null ? !nine.equals(that.nine) : that.nine != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (nine != null ? nine.hashCode() : 0);
        return result;
    }

    protected String getValues() {
        return super.getValues() + ", " + nine;
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, OO extends ObjectNonuplet<T1, T2, T3, T4, T5, T6, T7, T8, T9>> List<T9> getAllNines(Collection<OO> nonuplets) {
        List<T9> ret = new ArrayList<T9>(nonuplets.size());
        for (OO nonuplet : nonuplets) {
            ret.add(nonuplet.getNine());
        }
        return ret;
    }
}
