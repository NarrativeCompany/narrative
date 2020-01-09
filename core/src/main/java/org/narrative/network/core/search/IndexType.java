package org.narrative.network.core.search;

import org.narrative.common.util.enums.StringEnum;
import org.narrative.network.customizations.narrative.niches.niche.services.NicheIndexHandler;
import org.narrative.network.customizations.narrative.publications.services.PublicationIndexHandler;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Sep 11, 2008
 * Time: 11:55:43 AM
 *
 * @author brian
 */
public enum IndexType implements StringEnum {
    USER("u", UserIndexHandler.class)
    ,CONTENT("c", ContentIndexHandler.class)
    ,REPLY("r", ReplyIndexHandler.class)
    ,NICHE("n", NicheIndexHandler.class)
    ,PUBLICATION("p", PublicationIndexHandler.class)
    ;

    private final String idStr;
    private final Class<? extends IndexHandler> indexHandlerClass;

    IndexType(String idStr, Class<? extends IndexHandler> indexHandlerClass) {
        this.idStr = idStr;
        this.indexHandlerClass = indexHandlerClass;
    }

    private static final List<IndexType> ALL_INDEX_TYPES = Collections.unmodifiableList(Arrays.asList(values()));
    private static final Map<String, IndexType> CODE_TO_INDEX_TYPE;

    static {
        Map<String, IndexType> map = newHashMap();
        for (IndexType indexType : values()) {
            map.put(indexType.idStr, indexType);
        }
        CODE_TO_INDEX_TYPE = Collections.unmodifiableMap(map);
    }

    @Override
    public String getIdStr() {
        return idStr;
    }

    public TermQuery getTermQuery() {
        return new TermQuery(new Term(IndexHandler.FIELD__COMMON__INDEX_TYPE, getIdStr()));
    }

    public Class<? extends IndexHandler> getIndexHandlerClass() {
        return indexHandlerClass;
    }

    public IndexHandler getIndexHandler() {
        return IndexHandlerManager.getIndexHandler(this);
    }

    public boolean isUser() {
        return this == USER;
    }

    public boolean isContent() {
        return this == CONTENT;
    }

    public boolean isReply() {
        return this == REPLY;
    }

    public boolean isNiche() {
        return this == NICHE;
    }

    public boolean isPublication() {
        return this == PUBLICATION;
    }

    public static List<IndexType> getAllIndexTypes() {
        return ALL_INDEX_TYPES;
    }

    public static IndexType getFromCode(String code) {
        return CODE_TO_INDEX_TYPE.get(code);
    }

}