package org.narrative.network.core.search;

import org.narrative.common.persistence.OID;

import java.io.Serializable;

/**
 * Date: Sep 23, 2009
 * Time: 3:28:34 PM
 *
 * @author brian
 */
public class IndexOperationId implements Serializable {

    private final OID docId;
    private final OID extraDataOid;

    public IndexOperationId(OID docId) {
        this(docId, null);
    }

    public IndexOperationId(OID docId, OID extraDataOid) {
        this.docId = docId;
        this.extraDataOid = extraDataOid;
    }

    public OID getDocId() {
        return docId;
    }

    public OID getExtraDataOid() {
        return extraDataOid;
    }
}
