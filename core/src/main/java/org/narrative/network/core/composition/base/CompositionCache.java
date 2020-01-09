package org.narrative.network.core.composition.base;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.GSession;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Nov 15, 2006
 * Time: 9:31:17 AM
 * This class is designed to house a composition and composition items beyond the lifetime of the composition task that
 * retrieved them, as long as their composition session is still open.  Items can either be prepopulated or
 * retrieved just in time
 */
public class CompositionCache {
    protected final GSession compSession;
    protected final OID compositionOid;
    private Composition composition;

    public CompositionCache(OID compositionOid, GSession compSession) {
        this.compositionOid = compositionOid;
        this.compSession = compSession;
    }

    public Composition getComposition() {
        if (composition == null) {
            composition = compSession.getObject(Composition.class, compositionOid);
        }
        return composition;

    }

    public void setComposition(Composition composition) {
        this.composition = composition;
    }
}
