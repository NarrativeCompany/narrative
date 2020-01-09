package org.narrative.network.core.search;

import org.apache.solr.common.SolrInputDocument;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.rating.AgeRating;
import org.narrative.network.core.user.AuthZone;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Date: 1/19/11
 * Time: 10:05 AM
 *
 * @author brian
 */
public abstract class AreaDataIndexHandlerBase extends IndexHandler {

    public static final String FIELD__COMMON__AREA_OID = "areaOid";
    public static final String FIELD__COMMON__USER_OID = "userOid";
    public static final String FIELD__COMMON__LAST_UPDATE_DATETIME = "lastUpdateDatetime";

    protected AreaDataIndexHandlerBase(IndexType indexType) {
        super(indexType);
    }

    protected SolrInputDocument createAreaDataDocument(OID oid, OID userOid, String name, String fullText, Timestamp itemDate, Timestamp lastUpdateDate, AuthZone authZone, OID areaOid, AgeRating ageRating, long indexVersion) {
        SolrInputDocument document = createDefaultDocument(oid, name, fullText, itemDate, authZone, ageRating, indexVersion);
        document.addField(FIELD__COMMON__AREA_OID, areaOid.getValue());
        lastUpdateDate = (lastUpdateDate == null || lastUpdateDate.before(itemDate)) ? itemDate : lastUpdateDate;
        document.addField(FIELD__COMMON__LAST_UPDATE_DATETIME, new Date(lastUpdateDate.getTime()));
        if (userOid != null) {
            document.addField(FIELD__COMMON__USER_OID, userOid.getValue());
        }

        return document;
    }

}
