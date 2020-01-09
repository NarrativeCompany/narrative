package org.narrative.network.core.search.actions;

import com.google.common.collect.Sets;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.enums.StringEnum;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.portfolio.Portfolio;
import org.narrative.network.core.composition.base.CompositionType;
import org.narrative.network.core.content.base.ContentType;
import org.narrative.network.core.search.IndexType;
import org.narrative.network.core.search.services.ContentSearchCriteria;
import org.narrative.network.core.search.services.SearchCriteria;
import org.narrative.network.core.search.services.UserSearchCriteria;
import org.narrative.network.customizations.narrative.niches.niche.services.NicheSearchCriteria;
import org.narrative.network.customizations.narrative.publications.services.PublicationSearchCriteria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2/23/11
 * Time: 8:19 AM
 *
 * @author brian
 */
public enum SearchType implements StringEnum {
    EVERYTHING("everything"),
    MEMBERS("members", IndexType.USER),
    NICHES("niches", IndexType.NICHE),
    PUBLICATIONS("publications", IndexType.PUBLICATION),
    NARRATIVE_POSTS("posts", Collections.singleton(ContentType.NARRATIVE_POST)),
    ;

    private final String idStr;
    private final IndexType indexType;
    private final Set<ContentType> contentTypes;

    // jw: For Narrative let's restrict the available search types down a bit.
    public static final Set<SearchType> SUPPORTED_TYPES = Sets.immutableEnumSet(EVERYTHING, MEMBERS, NICHES, PUBLICATIONS, NARRATIVE_POSTS);

    private static final Map<ContentType, SearchType> CONTENT_TYPE_TO_SEARCH_TYPE;

    static {
        Map<ContentType, SearchType> contentTypeMap = newHashMap();
        for (SearchType searchType : values()) {
            if (searchType.contentTypes != null) {
                for (ContentType contentType : searchType.contentTypes) {
                    assert !contentTypeMap.containsKey(contentType) : "Shouldn't have the same ContentType mapped to multiple SearchTypes!";
                    contentTypeMap.put(contentType, searchType);
                }
            }
        }
        CONTENT_TYPE_TO_SEARCH_TYPE = Collections.unmodifiableMap(contentTypeMap);
    }

    SearchType(String idStr) {
        this(idStr, null, null);
    }

    SearchType(String idStr, Set<ContentType> contentTypes) {
        this(idStr, IndexType.CONTENT, contentTypes);
    }

    SearchType(String idStr, IndexType indexType) {
        this(idStr, indexType, null);
    }

    SearchType(String idStr, IndexType indexType, Set<ContentType> contentTypes) {
        this.idStr = idStr;
        this.indexType = indexType;
        this.contentTypes = contentTypes;
    }

    public SearchCriteria getSearcherCriteria(Area area, Portfolio portfolio, boolean includeReplies) {
        assert exists(area) : "Should always have an area!";

        // bl: default to the default portfolio for the area if no portfolio is specified.
        final Portfolio portfolioForCriteria = exists(portfolio) ? portfolio : Area.getAreaRlm(area).getDefaultPortfolio();

        if (isEverything()) {
            Set<IndexType> indexTypes = newLinkedHashSet();
            Set<ContentType> contentTypes = newLinkedHashSet();

            List<SearchType> searchTypes = new ArrayList<>(SUPPORTED_TYPES);

            for (SearchType searchType : searchTypes) {
                if (searchType.getIndexType() != null) {
                    indexTypes.add(searchType.getIndexType());
                }
                if (searchType.getContentTypes() != null) {
                    contentTypes.addAll(searchType.getContentTypes());
                }
            }

            Set<CompositionType> replyCompositionTypes = newHashSet();
            if (indexTypes.contains(IndexType.CONTENT) && includeReplies) {
                indexTypes.add(IndexType.REPLY);
                replyCompositionTypes.add(CompositionType.CONTENT);
            }
            return new SearchCriteria(portfolioForCriteria, indexTypes, replyCompositionTypes);
        }

        if (contentTypes != null) {
            return new ContentSearchCriteria(portfolioForCriteria, includeReplies, Collections.singleton(getSingleContentType()));
        }
        if (indexType.isUser()) {
            return new UserSearchCriteria(portfolioForCriteria);
        }
        if (indexType.isNiche()) {
            return new NicheSearchCriteria(portfolioForCriteria);
        }
        if (indexType.isPublication()) {
            return new PublicationSearchCriteria(portfolioForCriteria);
        }
        throw UnexpectedError.getRuntimeException("Unsupported SearchType found! st/" + this);
    }

    @Override
    public String getIdStr() {
        return idStr;
    }

    public IndexType getIndexType() {
        return indexType;
    }

    public ContentType getSingleContentType() {
        assert contentTypes != null && contentTypes.size() == 1 : "Should only get single ContentType for SearchTypes that have a single type!";
        return contentTypes.iterator().next();
    }

    public Set<ContentType> getContentTypes() {
        return contentTypes;
    }

    public boolean isEverything() {
        return this == EVERYTHING;
    }

    public boolean isNiches() {
        return this == NICHES;
    }

    public boolean isPublications() {
        return this == PUBLICATIONS;
    }

}
