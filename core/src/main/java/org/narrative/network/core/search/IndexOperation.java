package org.narrative.network.core.search;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.enums.IntegerEnum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class IndexOperation implements Serializable {
    final Type operation;
    final IndexOperationId id;

    public Type getOperation() {
        return operation;
    }

    public IndexOperationId getId() {
        return id;
    }

    public static IndexOperation update(OID oid) {
        return new IndexOperation(Type.UPDATE, new IndexOperationId(oid));
    }

    public static IndexOperation update(OID oid, OID extraDataOid) {
        return new IndexOperation(Type.UPDATE, new IndexOperationId(oid, extraDataOid));
    }

    public static List<IndexOperation> update(List<OID> oids, OID extraDataOid) {
        return operations(Type.UPDATE, oids, extraDataOid);
    }

    public static IndexOperation delete(OID oid) {
        return new IndexOperation(Type.DELETE, new IndexOperationId(oid));
    }

    public static List<IndexOperation> delete(List<OID> oids, OID extraDataOid) {
        return operations(Type.DELETE, oids, extraDataOid);
    }

    private static List<IndexOperation> operations(Type type, List<OID> oids, OID extraDataOid) {
        List<IndexOperation> indexOperations = new ArrayList<IndexOperation>();
        for (OID oid : oids) {
            final IndexOperationId id;
            if (extraDataOid != null) {
                id = new IndexOperationId(oid, extraDataOid);
            } else {
                id = new IndexOperationId(oid);
            }

            indexOperations.add(new IndexOperation(type, id));
        }

        return indexOperations;
    }

    public IndexOperation(Type operation, IndexOperationId id) {
        this.operation = operation;
        this.id = id;
    }

    public IndexOperation(Type operation) {
        this(operation, null);
    }

    public static enum Type implements Serializable, IntegerEnum {
        UPDATE(0),
        DELETE(1),
        NOOP(3);

        private final int id;

        private Type(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
        }
    }
}