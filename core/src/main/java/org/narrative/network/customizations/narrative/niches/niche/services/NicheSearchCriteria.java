package org.narrative.network.customizations.narrative.niches.niche.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.Stemmer;
import org.narrative.network.core.area.portfolio.Portfolio;
import org.narrative.network.core.search.IndexHandler;
import org.narrative.network.core.search.IndexType;
import org.narrative.network.core.search.services.IndexSearcherTask;
import org.narrative.network.core.search.services.SearchCriteria;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.niche.NicheStatus;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/13/18
 * Time: 8:54 AM
 */
public class NicheSearchCriteria extends SearchCriteria {
    private Niche excludeNiche;
    private OID excludeNicheOID;
    private NicheStatus status;
    private List<User> owners;

    private String similarDescription;

    private boolean searchNameOnly;
    private boolean searchUnstemmedNameOnly;

    public NicheSearchCriteria(Portfolio portfolio) {
        super(portfolio, EnumSet.of(IndexType.NICHE), null);
    }

    @Override
    public void appendQueryParams(BooleanQuery booleanQuery) {
        super.appendQueryParams(booleanQuery);

        if (exists(excludeNiche)) {
            booleanQuery.add(new TermQuery(new Term(NicheIndexHandler.FIELD__COMMON__ID, excludeNiche.getOid().toString())), BooleanClause.Occur.MUST_NOT);
        } else  if (excludeNicheOID != null) {
            booleanQuery.add(new TermQuery(new Term(NicheIndexHandler.FIELD__COMMON__ID, excludeNicheOID.toString())), BooleanClause.Occur.MUST_NOT);
        }

        if (getStatus() != null) {
            booleanQuery.add(new TermQuery(new Term(NicheIndexHandler.FIELD_NICHE_STATUS, Integer.toString(getStatus().getId()))), BooleanClause.Occur.MUST);
        }

        if (!isEmptyOrNull(getOwners())) {
            BooleanQuery ownersQuery = new BooleanQuery();
            for (User owner : getOwners()) {
                ownersQuery.add(new TermQuery(new Term(NicheIndexHandler.FIELD_NICHE_OWNER, owner.getOid().toString())), BooleanClause.Occur.SHOULD);
            }
            ownersQuery.setBoost(0);
            booleanQuery.add(ownersQuery, BooleanClause.Occur.MUST);
        }

        // jw: four characters or more, strip out stop words
        boolean hasSimilarName = !isEmpty(getQueryString());
        boolean hasSimilarDescription = !isEmpty(similarDescription);
        if (hasSimilarName || hasSimilarDescription) {
            BooleanQuery wordQuery = new BooleanQuery();

            if (hasSimilarName) {
                if (searchUnstemmedNameOnly) {
                    Collection<String> terms = getSearchTerms(getQueryString(), false);

                    // jw: keeping this weight so that this will be weighted much more heavily than the description if provided.
                    addTermsToQuery(wordQuery, NicheIndexHandler.FIELD_NICHE_NAME_UNSTEMMED, terms, true, 1000F);

                } else {
                    Collection<String> terms = getSearchTerms(getQueryString(), false);
                    List<String> stemmedTerms = getStemmedSearchTerms(terms);

                    addTermsToQuery(wordQuery, NicheIndexHandler.FIELD_NICHE_NAME_UNSTEMMED, terms, true, 1000F);
                    addTermsToQuery(wordQuery, NicheIndexHandler.FIELD__COMMON__NAME, stemmedTerms, false, 250F);
                    if (!searchNameOnly) {
                        addTermsToQuery(wordQuery, IndexHandler.FIELD__COMMON__FULL_TEXT, stemmedTerms, false, 5F);
                    }
                }
            }
            if (hasSimilarDescription) {
                Collection<String> terms = getSearchTerms(similarDescription, true);

                addTermsToQuery(wordQuery, IndexHandler.FIELD__COMMON__FULL_TEXT, terms, false, null);
            }

            booleanQuery.add(wordQuery, BooleanClause.Occur.MUST);
        }
    }

    @Override
    protected void addQueryStringQuery(BooleanQuery booleanQuery, Map<String, Float> fieldsToSearch) {
        // do nothing! we'll handle the queryString ourselves above
    }

    public static Collection<String> getSearchTerms(String value, boolean stemUnquotedTerms) {
        List<String> rawTerms = getSearchTermsFromString(value, stemUnquotedTerms);
        List<String> returnedTerms = new ArrayList<>(rawTerms);

        returnedTerms.removeIf(STOP_WORDS::contains);

        // If the search query was ALL stop words, let it go through without removing stop words
        if (returnedTerms.size() == 0) {
            returnedTerms = rawTerms;
        }

        return returnedTerms;
    }

    public static List<String> getStemmedSearchTerms(Collection<String> terms) {
        List<String> ret = new ArrayList<>(terms.size());
        for (String term : terms) {
            ret.add(Stemmer.getStemmedWord(term));
        }
        return ret;
    }

    public static void addTermsToQuery(BooleanQuery booleanQuery, String name, Collection<String> terms, boolean wildcard, Float boost) {
        if (isEmptyOrNull(terms)) {
            return;
        }
        for (String term : terms) {
            addTermQuery(booleanQuery
                    , name
                    , Collections.singleton(term)
                    // bl: now that we are doing stemming, let's not do any wildcard matches. too generic.
                    // jw: in the case of unstemmed name matches we will want to support wildcard matches, so this is now
                    //     using a passed in parameter.
                    , wildcard
                    , BooleanClause.Occur.SHOULD, boost
            );
        }
    }

    public static void prefetchNicheResultChunk(List<NicheSearchResult> searchResults) {
        List<OID> nicheOids = IndexSearcherTask.getOidsFromSearchResults(searchResults);
        Map<OID, Niche> oidToNiche = Niche.dao().getIDToObjectsFromObjects(Niche.dao().getObjectsFromIDsWithCache(nicheOids));
        for (NicheSearchResult searchResult : searchResults) {
            Niche niche = oidToNiche.get(searchResult.getOid());
            if (exists(niche)) {
                searchResult.setNiche(niche);
            }
        }
    }

    @Override
    public boolean isCriteriaSpecified() {
        return status != null || !isEmptyOrNull(owners) || !isEmpty(similarDescription) || super.isCriteriaSpecified();
    }

    public void setExcludeNiche(Niche excludeNiche) {
        this.excludeNiche = excludeNiche;
    }

    public void setExcludeNicheOID(OID excludeNicheOID) {
        this.excludeNicheOID = excludeNicheOID;
    }

    public NicheStatus getStatus() {
        return status;
    }

    public void setStatus(NicheStatus status) {
        this.status = status;
    }

    public List<User> getOwners() {
        return owners;
    }

    public void setOwners(List<User> owners) {
        this.owners = owners;
    }

    public String getSimilarDescription() {
        return similarDescription;
    }

    public void setSimilarDescription(String similarDescription) {
        this.similarDescription = similarDescription;
    }

    public void setSearchNameOnly(boolean searchNameOnly) {
        this.searchNameOnly = searchNameOnly;
    }

    public void setSearchUnstemmedNameOnly(boolean searchUnstemmedNameOnly) {
        this.searchUnstemmedNameOnly = searchUnstemmedNameOnly;
    }

    private static final Collection<String> STOP_WORDS = Collections.unmodifiableList(Arrays.asList("i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", "yours", "yourself", "yourselves", "he", "him", "his", "himself", "she", "her", "hers", "herself", "it", "its", "itself", "they", "them", "their", "theirs", "themselves", "what", "which", "who", "whom", "this", "that", "these", "those", "am", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "having", "do", "does", "did", "doing", "a", "an", "the", "and", "but", "if", "or", "because", "as", "until", "while", "of", "at", "by", "for", "with", "about", "against", "between", "into", "through", "during", "before", "after", "above", "below", "to", "from", "up", "down", "in", "out", "on", "off", "over", "under", "again", "further", "then", "once", "here", "there", "when", "where", "why", "how", "all", "any", "both", "each", "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very", "s", "t", "can", "will", "just", "don", "should", "now"));
}
