package org.narrative.common.persistence;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 6/30/14
 * Time: 3:29 PM
 * <p>
 * Name from: http://en.wikipedia.org/wiki/Multiple_birth
 * which ultimately seems to derive from:
 * http://blogs.transparent.com/latin/latin-numbers-1-100/
 */
public class ObjectDuodecaplet<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> extends ObjectNonuplet<T1, T2, T3, T4, T5, T6, T7, T8, T9> {
    protected final T10 ten;
    protected final T11 eleven;
    protected final T12 twelve;

    public ObjectDuodecaplet(T1 one, T2 two, T3 three, T4 four, T5 five, T6 six, T7 seven, T8 eight, T9 nine, T10 ten, T11 eleven, T12 twelve) {
        super(one, two, three, four, five, six, seven, eight, nine);
        this.ten = ten;
        this.eleven = eleven;
        this.twelve = twelve;
    }

    public T10 getTen() {
        return ten;
    }

    public T11 getEleven() {
        return eleven;
    }

    public T12 getTwelve() {
        return twelve;
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

        ObjectDuodecaplet that = (ObjectDuodecaplet) o;

        if (ten != null ? !ten.equals(that.ten) : that.ten != null) {
            return false;
        }
        if (eleven != null ? !eleven.equals(that.eleven) : that.eleven != null) {
            return false;
        }
        if (twelve != null ? !twelve.equals(that.twelve) : that.twelve != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (ten != null ? ten.hashCode() : 0);
        result = 31 * result + (eleven != null ? eleven.hashCode() : 0);
        result = 31 * result + (twelve != null ? twelve.hashCode() : 0);
        return result;
    }

    protected String getValues() {
        return super.getValues() + ", " + ten + ", " + eleven + ", " + twelve;
    }
}
