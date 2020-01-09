package org.narrative.common.persistence.hibernate.criteria;

import org.hibernate.transform.BasicTransformerAdapter;

import java.io.Serializable;

/**
 * Date: 11/30/16
 * Time: 2:22 PM
 *
 * @author brian
 */
public class FirstResultTransformer extends BasicTransformerAdapter implements Serializable {

    public static final FirstResultTransformer INSTANCE = new FirstResultTransformer();

    /**
     * Disallow instantiation of FirstResultTransformer.
     */
    private FirstResultTransformer() {
    }

    /**
     * {@inheritDoc}
     */
    public Object transformTuple(Object[] tuple, String[] aliases) {
        // bl: the whole point of this adapter is to just return the first item in the result
        return tuple[0];
    }

    /**
     * Serialization hook for ensuring singleton uniqueing.
     *
     * @return The singleton instance : {@link #INSTANCE}
     */
    private Object readResolve() {
        return INSTANCE;
    }
}
