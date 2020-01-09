package org.narrative.network.core.search.services;

import org.narrative.common.persistence.OID;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;

import java.util.Collection;

import static org.narrative.common.util.CoreUtils.*;

public class OidSearchUtil {
    public static TermQuery getTermQuery(OID oid , String fieldName) {
        return new TermQuery(new Term(fieldName, oid.toString()));
    }

    public static void addToBooleanQuery(OID oid, BooleanQuery query, String fieldName, BooleanClause.Occur occur) {
        query.add(getTermQuery(oid, fieldName), occur);
    }

    public static BooleanQuery addOidsToBooleanQuery(Collection<OID> oids, BooleanQuery query, String fieldName, BooleanClause.Occur occur) {
        if (isEmptyOrNull(oids)) {
            return query;
        }

        for (OID oid : oids) {
            addToBooleanQuery(oid, query, fieldName, occur);
        }

        return query;
    }
}
