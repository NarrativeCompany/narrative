package org.narrative.common.persistence;

import org.narrative.common.util.IPStringUtil;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;

/**
 * ResultSetInfo holds information about results that save us a little work on
 * subsequent calls.  The idea is that you create one of these the first time
 * each different query is run.
 */
public class ResultSetInfo {
    /**
     * whether each column needs to be 'escaped' for xml
     * (basically any text type field, except _oid fields)
     */
    public boolean fieldNeedsXMLEscaping[];
    /**
     * there are a few fields (mostly the longer ones) that
     * it'd be more efficient to CDATA them than XML escape
     * them
     */
    public boolean fieldNeedsCDATAWrapping[];

    /**
     * true if the field holds xml (so does not need cdata wrapping nor
     * escaping
     */
    public boolean fieldHoldsXML[];
    /**
     * the field names at each position
     */
    public String fieldNames[];
    public int fieldTypes[];
    public int fieldSizes[];
    private static HashMap s_needsCDATAWrapping = new HashMap();
    private static HashMap s_insertWithNoEscaping = new HashMap();

    static {
        s_needsCDATAWrapping.put("MESSAGE_BODY", "MESSAGE_BODY");
        s_needsCDATAWrapping.put("TOPIC_LEAD", "TOPIC_LEAD");
        s_needsCDATAWrapping.put("FORUM_DESCRIPTION", "FORUM_DESCRIPTION");
        s_needsCDATAWrapping.put("FORUM_INTRO", "FORUM_INTRO");
        s_needsCDATAWrapping.put("AD_HTML", "AD_HTML");
        s_needsCDATAWrapping.put("FMT_HTML_POST_HTML", "FMT_HTML_POST_HTML");
        s_needsCDATAWrapping.put("FMT_HTML_PRE_HTML", "FMT_HTML_PRE_HTML");
        s_needsCDATAWrapping.put("FMT_HTML_HEAD_TAG", "FMT_HTML_HEAD_TAG");

        s_insertWithNoEscaping.put("FMT_CUSTOM_XML", "FMT_CUSTOM_XML");
        s_insertWithNoEscaping.put("FMT_IMAGES_XML", "FMT_IMAGES_XML");
        s_insertWithNoEscaping.put("EVENT_XML", "EVENT_XML");
    }

    public ResultSetInfo(ResultSet rs) {
        if (rs == null) {
            fieldNeedsXMLEscaping = fieldNeedsCDATAWrapping = fieldHoldsXML = new boolean[0];
            fieldSizes = fieldTypes = new int[0];
            fieldNames = IPStringUtil.EMPTY_STRING_ARRAY;
            return;
        }
        try {
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            fieldNeedsXMLEscaping = new boolean[cols];
            fieldNeedsCDATAWrapping = new boolean[cols];
            fieldNames = new String[cols];
            fieldHoldsXML = new boolean[cols];
            fieldTypes = new int[cols];
            fieldSizes = new int[cols];

            for (int i = 0; i < cols; i++) {
                int type = meta.getColumnType(i + 1);
                fieldTypes[i] = type;
                fieldSizes[i] = meta.getColumnDisplaySize(i + 1);
                fieldNeedsXMLEscaping[i] = type == Types.CHAR || type == Types.VARCHAR || type == Types.LONGVARBINARY || type == Types.LONGVARCHAR || type == Types.VARBINARY;
                String name = meta.getColumnName(i + 1);
                if (fieldNeedsXMLEscaping[i] && s_needsCDATAWrapping.get(name) != null) {
                    fieldNeedsCDATAWrapping[i] = true;
                } else if (null != s_insertWithNoEscaping.get(name)) {
                    fieldHoldsXML[i] = true;
                } else {
                    fieldNeedsXMLEscaping[i] = doesFieldNeedXMLEscaping(name);
                }
                fieldNames[i] = name;
            }
        } catch (SQLException se) {
            //ignore: happens with oracle when doing updates
            // but not mysql
            //throw UnexpectedError.getRuntimeException("failed creating resultsetinfo", se);
        }
    }

    /**
     * Does the field require XML escaping ?
     * <p>
     * Note: OIDs and fields ending with _CUSTOM_XML don't require escaping.
     */
    public static boolean doesFieldNeedXMLEscaping(String name) {
        if (name == null) {
            return false;
        }
        if (name.endsWith("_OID") || name.endsWith("_XML")) {
            return false;
        }
        return true;
    }
}