package org.narrative.common.persistence;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Note that ObjectPair is only correctly serializable if types T1 and T2 are Serializable.
 * <p>
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Apr 26, 2006
 * Time: 1:12:01 PM
 */
public class ObjectPair<T1, T2> implements Serializable {
    private static final long serialVersionUID = -3449164035277494946L;
    protected  T1 one;
    protected  T2 two;

    public ObjectPair() {

    }

    @JsonCreator
    public ObjectPair(@JsonProperty("one") T1 one, @JsonProperty("two") T2 two) {
        this.one = one;
        this.two = two;
    }

    public T1 getOne() {
        return one;
    }

    public T2 getTwo() {
        return two;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ObjectPair that = (ObjectPair) o;

        if (one != null ? !one.equals(that.one) : that.one != null) {
            return false;
        }
        if (two != null ? !two.equals(that.two) : that.two != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = (one != null ? one.hashCode() : 0);
        result = 31 * result + (two != null ? two.hashCode() : 0);
        return result;
    }

    public String toString() {
        return "{" + getValues() + "}";
    }

    protected String getValues() {
        return one + ", " + two;
    }

    public static <T1, T2, OP extends ObjectPair<T1, T2>> List<T1> getAllOnes(Collection<OP> pairs) {
        List<T1> ret = newArrayList(pairs.size());
        for (OP pair : pairs) {
            ret.add(pair.getOne());
        }
        return ret;
    }

    public static <T1, T2, OP extends ObjectPair<T1, T2>> Set<T1> getAllUniqueOnes(Collection<OP> pairs) {
        Set<T1> ret = newHashSet();
        for (OP pair : pairs) {
            ret.add(pair.getOne());
        }
        return ret;
    }

    public static <T1, T2, OP extends ObjectPair<T1, T2>> List<T2> getAllTwos(Collection<OP> pairs) {
        List<T2> ret = newArrayList(pairs.size());
        for (OP pair : pairs) {
            ret.add(pair.getTwo());
        }
        return ret;
    }

    public static <T1, T2, OP extends ObjectPair<T1, T2>> Set<T2> getAllUniqueTwos(Collection<OP> pairs) {
        Set<T2> ret = newHashSet();
        for (OP pair : pairs) {
            ret.add(pair.getTwo());
        }
        return ret;
    }

    public static <T1, T2, OP extends ObjectPair<T1, T2>> Map<T1, T2> getAsMap(Collection<OP> pairs) {
        Map<T1, T2> ret = newLinkedHashMap();
        for (OP pair : pairs) {
            ret.put(pair.getOne(), pair.getTwo());
        }
        return ret;
    }

    public static <T1, T2, OP extends ObjectPair<T1, T2>> Map<T2, T1> getAsMapOfTwosToOnes(Collection<OP> pairs) {
        Map<T2, T1> ret = newLinkedHashMap();
        for (OP pair : pairs) {
            ret.put(pair.getTwo(), pair.getOne());
        }
        return ret;
    }

    public static <T1, T2, OP extends ObjectPair<T1, T2>> Map<T1, OP> getAsMapOfOnesToObjs(Collection<OP> pairs) {
        Map<T1, OP> ret = newLinkedHashMap();
        for (OP pair : pairs) {
            ret.put(pair.getOne(), pair);
        }
        return ret;
    }

    public static <T1, T2, OP extends ObjectPair<T1, T2>> Map<T1, List<OP>> getAsMapOfOnesToObjList(Collection<OP> pairs) {
        Map<T1, List<OP>> ret = newLinkedHashMap();
        for (OP pair : pairs) {
            List<OP> list = ret.get(pair.getOne());
            if (list == null) {
                ret.put(pair.getOne(), list = newLinkedList());
            }
            list.add(pair);
        }
        return ret;
    }

    public static <T1, T2, OP extends ObjectPair<T1, T2>> Map<T2, List<OP>> getAsMapOfTwosToObjList(Collection<OP> pairs) {
        Map<T2, List<OP>> ret = newLinkedHashMap();
        for (OP pair : pairs) {
            List<OP> list = ret.get(pair.getTwo());
            if (list == null) {
                ret.put(pair.getTwo(), list = newLinkedList());
            }
            list.add(pair);
        }
        return ret;
    }

    public static <T1, T2, OP extends ObjectPair<T1, T2>> Map<T1, Set<T2>> getAsMapOfOnesToTwosSet(Collection<OP> pairs) {
        Map<T1, Set<T2>> ret = newLinkedHashMap();
        for (OP pair : pairs) {
            Set<T2> set = ret.get(pair.getOne());
            if (set == null) {
                ret.put(pair.getOne(), set = newLinkedHashSet());
            }
            set.add(pair.getTwo());
        }
        return ret;
    }

    public static <T1, T2, OP extends ObjectPair<T1, T2>> Map<T1, List<T2>> getAsMapOfOnesToTwosList(Collection<OP> pairs) {
        Map<T1, List<T2>> ret = newLinkedHashMap();
        addOnesToTwosList(pairs, ret);

        return ret;
    }

    public static <T1, T2, OP extends ObjectPair<T1, T2>> void addOnesToTwosList(Collection<OP> pairs, Map<T1, List<T2>> lookup) {
        assert lookup != null : "Should always provide the lookup map to merge values into!";
        for (OP pair : pairs) {
            List<T2> list = lookup.get(pair.getOne());
            if (list == null) {
                lookup.put(pair.getOne(), list = newLinkedList());
            }
            list.add(pair.getTwo());
        }
    }

}
