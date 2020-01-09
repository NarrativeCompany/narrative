package org.narrative.network.core.search.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.ValidationHandler;
import org.narrative.network.core.area.portfolio.Portfolio;
import org.narrative.network.core.search.IndexHandler;
import org.narrative.network.core.search.IndexType;
import org.narrative.network.core.search.SearchResult;
import org.narrative.network.core.search.UserSearchResult;
import org.narrative.network.core.user.User;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2/23/11
 * Time: 12:15 PM
 *
 * @author brian
 */
public class UserSearchCriteria extends SearchCriteria {

    private String displayNameLike;
    private String profileKeywordsLike;

    public UserSearchCriteria(Portfolio portfolio) {
        super(portfolio, Collections.singleton(IndexType.USER), null);
    }

    @Override
    public void validate(ValidationHandler handler, String paramName) {
        super.validate(handler, paramName);
    }

    @Override
    public void appendQueryParams(BooleanQuery booleanQuery) {
        super.appendQueryParams(booleanQuery);

        if (!isEmpty(displayNameLike)) {
            BooleanQuery displayNameQuery = new BooleanQuery();

            // jw: give exact matches a boost
            addExactNameFieldMatchBoost(displayNameQuery, getSearchTermsFromString(displayNameLike));

            // jw: Include any members with the value within it.  So if you search for ST, you will get ST first, and then anyone with ST in their name.
            addWildcardTermQueryString(displayNameQuery, IndexHandler.FIELD__COMMON__NAME, displayNameLike, BooleanClause.Occur.MUST);
            booleanQuery.add(displayNameQuery, BooleanClause.Occur.MUST);
        }
        if (!isEmpty(profileKeywordsLike)) {
            addWildcardTermQueryString(booleanQuery, IndexHandler.FIELD__COMMON__FULL_TEXT, profileKeywordsLike, BooleanClause.Occur.MUST);
        }
    }

    public static void prefetchUserResultChunk(List<UserSearchResult> searchResults) {
        List<OID> userOids = IndexSearcherTask.getOidsFromSearchResults(searchResults);
        Map<OID, User> oidToUser = User.dao().getIDToObjectsFromObjects(User.dao().getObjectsFromIDsWithCache(userOids));
        for (SearchResult searchResult : searchResults) {
            UserSearchResult userSearchResult = (UserSearchResult) searchResult;
            User user = oidToUser.get(searchResult.getOid());
            // bl: only include visible users
            if (exists(user) && user.isVisible()) {
                userSearchResult.setUser(user);
            }
        }
    }

    // jw: The isCriteriaSpecified overrides here ensure that we will only consider the criteria setup for searching
    //     if applicable fields are set.  This is necessary because of how we persist search criteria on the search.jsp
    //     page between SearchType requests.  If the user was previously filtering by a member on a content search, and
    //     they switch to a member search we will not honor the users criteria, and thanks to these changes the user
    //     will get the "You must specify criteria" message.
    @Override
    public boolean isCriteriaSpecified() {
        return !isEmpty(getQueryString()) || !isEmpty(displayNameLike) || !isEmpty(profileKeywordsLike);
    }

    public String getDisplayNameLike() {
        return displayNameLike;
    }

    public void setDisplayNameLike(String displayNameLike) {
        this.displayNameLike = displayNameLike;
    }

    public String getProfileKeywordsLike() {
        return profileKeywordsLike;
    }

    public void setProfileKeywordsLike(String profileKeywordsLike) {
        this.profileKeywordsLike = profileKeywordsLike;
    }
}
