package org.narrative.network.core.search.services;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Version;
import org.narrative.common.core.services.interceptors.BypassHtmlDisable;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.util.IPDateUtil;
import org.narrative.common.util.IPHTMLUtil;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.Stemmer;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.ValidationHandler;
import org.narrative.network.core.area.portfolio.Portfolio;
import org.narrative.network.core.composition.base.CompositionType;
import org.narrative.network.core.search.AreaDataIndexHandlerBase;
import org.narrative.network.core.search.CompositionIndexFields;
import org.narrative.network.core.search.IndexHandler;
import org.narrative.network.core.search.IndexType;
import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.shared.util.NetworkLogger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 1/23/13
 * Time: 4:12 PM
 * User: jonmark
 */
public class SearchCriteria {
    private static final NetworkLogger logger = new NetworkLogger(SearchCriteria.class);

    protected static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    static {
        DATE_FORMAT.setTimeZone(IPDateUtil.UTC_TIMEZONE);
    }

    private static final List<ObjectPair<Pattern, String>> LUCENE_SPECIAL_STRING_ESCAPE_PATTERNS;
    private static final List<ObjectPair<Pattern, String>> LUCENE_SPECIAL_STRING_SOLR_ESCAPE_PATTERNS;

    static {
        // bl: a single quote " is also a special character that would need escaping, but we are handling
        // quotes properly in cleanLuceneQueryString by making sure that there are matching quotes in the query.
        // bl: make sure that we do backslashes first or else the originally escaped strings will have their backslashes escaped
        List<String> luceneStringsToEscape = Arrays.asList("&", "\\", "+", "-", "|", "!", "(", ")", "{", "}", "[", "]", "^", "~", "*", "?", ":");
        List<ObjectPair<Pattern, String>> escapePatterns = newArrayList(luceneStringsToEscape.size());
        List<ObjectPair<Pattern, String>> solrPatterns = newArrayList(luceneStringsToEscape.size());
        for (String s : luceneStringsToEscape) {
            String htmlEntity = IPHTMLUtil.getHtmlEntityForCharacter(s.charAt(0));
            escapePatterns.add(newObjectPair(Pattern.compile(s, Pattern.LITERAL), Matcher.quoteReplacement(htmlEntity)));
            solrPatterns.add(newObjectPair(Pattern.compile(htmlEntity, Pattern.LITERAL), Matcher.quoteReplacement(s)));
        }
        LUCENE_SPECIAL_STRING_ESCAPE_PATTERNS = Collections.unmodifiableList(escapePatterns);
        LUCENE_SPECIAL_STRING_SOLR_ESCAPE_PATTERNS = Collections.unmodifiableList(Lists.reverse(solrPatterns));
    }

    private final Portfolio portfolio;
    private final Collection<IndexType> indexTypes;
    private final Collection<CompositionType> replyCompositionTypes;

    private Collection<User> users;

    private String queryString;
    private String exactPhrase;
    private String requiredWords;
    private String optionalWords;
    private String notWords;

    private boolean hasAttachments;
    private Boolean allowReplies;

    private Channel channel;

    public SearchCriteria(Portfolio portfolio, Collection<IndexType> indexTypes, Collection<CompositionType> replyCompositionTypes) {
        assert !isEmptyOrNull(indexTypes) : "Should never attempt a search with no indexes!";

        this.portfolio = portfolio;
        this.indexTypes = indexTypes;
        this.replyCompositionTypes = replyCompositionTypes != null ? replyCompositionTypes : Collections.emptyList();
    }

    public void validate(ValidationHandler handler, String paramName) {

    }

    public void appendQueryParams(BooleanQuery booleanQuery) {
        if (getIndexTypes().contains(IndexType.REPLY) && !getReplyCompositionTypes().isEmpty()) {
            // jw: if we are filtering down to certain reply types, then the result must either not be a reply, or it
            //     must has a specific compositionType!
            Collection<CompositionType> excludedCompositionTypes = CollectionUtils.disjunction(getReplyCompositionTypes(), CompositionType.ALL_SEARCHABLE_REPLY_TYPES);
            if (excludedCompositionTypes != null) {
                for (CompositionType excludedCompositionType : excludedCompositionTypes) {
                    TermQuery excludedCompositionTypeQuery = excludedCompositionType.getTermQuery();
                    // bl: no need for boost for excluding CompositionTypes
                    excludedCompositionTypeQuery.setBoost(0);
                    booleanQuery.add(excludedCompositionTypeQuery, BooleanClause.Occur.MUST_NOT);
                }
            }
        }

        Map<String, Float> fields = getFieldsToSearch();
        if (!isEmpty(queryString)) {
            addQueryStringQuery(booleanQuery, fields);
        }

        boolean isMemberSearch = isForIndexTypeOnly(IndexType.USER);

        // bl: the "optional" words are described as "with at least one of these words" so make sure that one of
        // the words appears in one of the fields at least once
        if (!isMemberSearch) {
            if (!isEmpty(optionalWords)) {
                BooleanQuery optionalWordQuery = new BooleanQuery();
                List<String> optionalWordTerms = getSearchTermsFromString(optionalWords);
                for (Map.Entry<String, Float> entry : fields.entrySet()) {
                    String field = entry.getKey();
                    Float boost = entry.getValue();
                    for (String optionalWordTerm : optionalWordTerms) {
                        addWildcardTermQuery(optionalWordQuery, field, optionalWordTerm, BooleanClause.Occur.SHOULD, boost);
                    }
                }

                addExactNameFieldMatchBoost(optionalWordQuery, optionalWordTerms);
                booleanQuery.add(optionalWordQuery, BooleanClause.Occur.MUST);
            }

            if (!isEmpty(exactPhrase)) {
                BooleanQuery phraseFieldsQuery = new BooleanQuery();
                List<String> phraseTerms = getSearchTermsFromString(exactPhrase);
                for (Map.Entry<String, Float> entry : fields.entrySet()) {
                    String field = entry.getKey();
                    Float boost = entry.getValue();
                    PhraseQuery exact = new PhraseQuery();
                    exact.setSlop(0);
                    for (String phraseTerm : phraseTerms) {
                        exact.add(new Term(field, phraseTerm));
                    }
                    exact.setBoost(boost);
                    phraseFieldsQuery.add(exact, BooleanClause.Occur.SHOULD);
                }

                booleanQuery.add(phraseFieldsQuery, BooleanClause.Occur.MUST);
            }

            if (!isEmpty(requiredWords)) {
                List<String> requiredWordTerms = getSearchTermsFromString(requiredWords);
                for (String requiredWordTerm : requiredWordTerms) {
                    BooleanQuery requiredWordQuery = new BooleanQuery();
                    for (Map.Entry<String, Float> entry : fields.entrySet()) {
                        String field = entry.getKey();
                        Float boost = entry.getValue();
                        addWildcardTermQuery(requiredWordQuery, field, requiredWordTerm, BooleanClause.Occur.SHOULD, boost);
                    }
                    booleanQuery.add(requiredWordQuery, BooleanClause.Occur.MUST);
                }
            }

            if (!isEmpty(notWords)) {
                List<String> notWordTerms = getSearchTermsFromString(notWords);
                for (String field : fields.keySet()) {
                    for (String notWordTerm : notWordTerms) {
                        TermQuery notQuery = new TermQuery(new Term(field, notWordTerm));
                        // bl: no need for boost for the excluded terms
                        notQuery.setBoost(0);
                        booleanQuery.add(notQuery, BooleanClause.Occur.MUST_NOT);
                    }
                }
            }

            // this Field is only on Content and Reply, but we need it for Everything searching so adding it here!
            if (hasAttachments) {
                booleanQuery.add(createUnweightedTermQuery(CompositionIndexFields.FIELD_HAS_ATTACHMENTS, Boolean.TRUE.toString()), BooleanClause.Occur.MUST);
            }

            if (allowReplies != null) {
                booleanQuery.add(createUnweightedTermQuery(IndexHandler.FIELD__COMMON__ALLOW_REPLIES, allowReplies.toString()), BooleanClause.Occur.MUST);
            }

            User.dao().removeNonExistentObjects(users);
            if (!isEmptyOrNull(users)) {
                BooleanQuery usersQuery = new BooleanQuery();
                for (User user : users) {
                    usersQuery.add(new TermQuery(new Term(AreaDataIndexHandlerBase.FIELD__COMMON__USER_OID, user.getOid().toString())), BooleanClause.Occur.SHOULD);
                }
                // bl: no need for boost by user
                usersQuery.setBoost(0);
                booleanQuery.add(usersQuery, BooleanClause.Occur.MUST);
            }

            if(exists(channel)) {
                booleanQuery.add(createUnweightedTermQuery(CompositionIndexFields.FIELD_CHANNEL_OID, channel.getOid().toString()), BooleanClause.Occur.MUST);
            }
        }
    }

    protected void addQueryStringQuery(BooleanQuery booleanQuery, Map<String, Float> fieldsToSearch) {
        BooleanQuery stringBooleanQuery = new BooleanQuery();

        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);//indexTypes.iterator().next().getIndexHandler().getAnalyzer();
        //bw: Deprecated array, but the constructor wasn't fixed until 3.0.1
        //new SnowballAnalyzer(Version.LUCENE_35, "English", StopAnalyzer.ENGLISH_STOP_WORDS)
        MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParser(Version.LUCENE_35, fieldsToSearch.keySet().toArray(new String[fieldsToSearch.size()]), analyzer, fieldsToSearch);
        List<String> queryTerms = getSearchTermsFromString(queryString);
        for (String queryTerm : queryTerms) {
            try {
                boolean isWildcard = !queryTerm.startsWith("\"") && !queryTerm.endsWith("\"");
                Query query = multiFieldQueryParser.parse(isWildcard ? queryTerm + "*" : queryTerm);
                // bl: for wildcard queries, the boosts aren't applied (supplied in the fields param to MultiFieldQueryParser constructor.
                // so, hack in and apply them manually here.
                if (isWildcard) {
                    // bl: we know this should always be a BooleanQuery
                    List<BooleanClause> clauses = ((BooleanQuery) query).clauses();
                    for (BooleanClause clause : clauses) {
                        // bl: we know they are PrefixQuery objects since we appended a trailing wildcard (*)
                        // jw: though the multiFieldQueryParser.parse() may split the string into multiple queries
                        //     and in that case it means that we failed to split the queryTerms properly.  So lets
                        //     go ahead and log out a error to the logs and Error page so we can identify a new delimiter
                        if (clause.getQuery() instanceof PrefixQuery) {
                            PrefixQuery prefixQuery = (PrefixQuery) clause.getQuery();
                            Term term = prefixQuery.getPrefix();
                            prefixQuery.setBoost(fieldsToSearch.get(term.field()));
                        } else {
                            String error = "queryTerm parsed into multiple Queries unexpectedly.  queryTerm: " + queryTerm;
                            if (logger.isErrorEnabled()) {
                                logger.error(error);
                            }
                            StatisticManager.recordException(UnexpectedError.getRuntimeException(error), false, null);
                        }
                    }
                }
                stringBooleanQuery.add(query, BooleanClause.Occur.SHOULD);
            } catch (org.apache.lucene.queryParser.ParseException pe) {
                throw UnexpectedError.getRuntimeException("Failed parsing query string qs/" + queryString, pe);
            }
        }

        addExactNameFieldMatchBoost(stringBooleanQuery, queryTerms);

        booleanQuery.add(stringBooleanQuery, BooleanClause.Occur.MUST);
    }

    protected Map<String, Float> getFieldsToSearch() {
        Map<String, Float> ret = newLinkedHashMap();
        // bl: give subjects 2x the weight of the full text field
        ret.put(IndexHandler.FIELD__COMMON__NAME, 2.0f);
        ret.put(IndexHandler.FIELD__COMMON__FULL_TEXT, 1.0f);
        return ret;
    }

    public static void addWildcardTermQueryString(BooleanQuery booleanQuery, String fieldName, String queryString, BooleanClause.Occur occur) {
        addWildcardTermQuery(booleanQuery, fieldName, getSearchTermsFromString(queryString), occur, null);
    }

    protected static void addWildcardTermQuery(BooleanQuery booleanQuery, String fieldName, String searchTerm, BooleanClause.Occur occur, Float boost) {
        addWildcardTermQuery(booleanQuery, fieldName, Collections.singleton(searchTerm), occur, boost);
    }

    protected static void addWildcardTermQuery(BooleanQuery booleanQuery, String fieldName, Collection<String> searchTerms, BooleanClause.Occur occur, Float boost) {
        addTermQuery(booleanQuery, fieldName, searchTerms, true, occur, boost);
    }

    protected static void addTermQuery(BooleanQuery booleanQuery, String fieldName, Collection<String> searchTerms, boolean wildcard, BooleanClause.Occur occur, Float boost) {
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_35, Collections.emptySet());
        QueryParser qp = new QueryParser(Version.LUCENE_35, fieldName, analyzer);
        BooleanQuery termsQuery = new BooleanQuery();
        for (String searchTerm : searchTerms) {
            // bl: only use wildcard for terms that are not in quotes
            boolean isWildcard = wildcard && (!searchTerm.startsWith("\"") && !searchTerm.endsWith("\""));
            try {
                Query query = qp.parse(isWildcard ? searchTerm + "*" : searchTerm);

                // jw: if the parser was unable to parse a meaningful query from the string, we need to skip it. This came
                //     up now because we now support single character non-wildcard searches, which for comma's ends up being
                //     a bad thing.
                if (query instanceof BooleanQuery) {
                    BooleanQuery termQuery = (BooleanQuery) query;
                    if (termQuery.clauses().isEmpty()) {
                        continue;
                    }
                }
                termsQuery.add(query, BooleanClause.Occur.SHOULD);
            } catch (org.apache.lucene.queryParser.ParseException pe) {
                logger.warn("Found parse exception while parsing query string!  Continuing on.  term: '" + searchTerm, pe);
            }
        }
        if (boost != null) {
            termsQuery.setBoost(boost);
        }
        // jw: if we ended up skipping all of the terms for this group, we need to not include it.
        if (!termsQuery.clauses().isEmpty()) {
            booleanQuery.add(termsQuery, occur);
        }
    }

    private static final Pattern QUOTED_SEARCH_PATTERN = Pattern.compile("\\\"[^\\\"]*\\\"");

    private static final BitSet TERM_DELIMITERS = new BitSet();

    static {
        TERM_DELIMITERS.set(' ');
        TERM_DELIMITERS.set('\r');
        TERM_DELIMITERS.set('\n');
        TERM_DELIMITERS.set('\t');
    }

    protected static List<String> getSearchTermsFromString(String queryString) {
        return getSearchTermsFromString(queryString, true);
    }

    protected static List<String> getSearchTermsFromString(String queryString, boolean stemUnquotedTerms) {
        queryString = cleanQueryStringPriorToSolr(queryString);
        List<String> ret = newLinkedList();
        StringBuffer sb = new StringBuffer();
        // bl: identify any quoted text first
        Matcher matcher = QUOTED_SEARCH_PATTERN.matcher(queryString);
        while (matcher.find()) {
            String quotedText = matcher.group();
            // bl: skip appending ""
            if (quotedText.length() > 2) {
                // bl: can't start with a wildcard, so strip it out
                if (quotedText.startsWith("\"\\*")) {
                    quotedText = quotedText.replaceFirst("\"\\\\\\*", "\"");
                }
                ret.add(quotedText);
            }
            matcher.appendReplacement(sb, " ");
        }
        matcher.appendTail(sb);
        // bl: whatever wasn't quoted text should now be space separated, so parse those out and add to the list
        List<String> unquotedTerms = IPStringUtil.getListFromDelimitedString(sb.toString(), TERM_DELIMITERS);
        for (String unquotedTerm : unquotedTerms) {
            // bl: don't allow any sequences to start with a *
            if (unquotedTerm.startsWith("\\*")) {
                unquotedTerm = unquotedTerm.replaceFirst("\\\\\\*", "");
            }
            // bl: handle stripping off a single '*' search term
            if (isEmpty(unquotedTerm)) {
                continue;
            }

            // Stem the unquoted term
            if (stemUnquotedTerms) {
                ret.add(Stemmer.getStemmedWord(unquotedTerm));
            } else {
                ret.add(unquotedTerm);
            }
        }

        return ret;
    }

    // jw: this is a utility method to add a exact match boost for any exact matches on the "name" field.
    protected void addExactNameFieldMatchBoost(BooleanQuery addTo, Collection<String> terms) {
        addExactNameFieldMatchBoost(addTo, terms, IndexType.USER);
    }

    // jw: this is a utility method to add a exact match boost for any exact matches on the "name" field.
    protected void addExactNameFieldMatchBoost(BooleanQuery addTo, Collection<String> terms, IndexType forIndexType) {
        addExactFieldMatchBoost(addTo, IndexHandler.FIELD__COMMON__NAME, terms, forIndexType, 1000);
    }

    protected void addExactFieldMatchBoost(BooleanQuery addTo, String fieldName, Collection<String> terms, IndexType forIndexType, float boost) {
        // jw: if we are not searching for members then dont bother doing anything.
        if (!indexTypes.contains(forIndexType)) {
            return;
        }

        // jw: first, lets setup the exact field match!
        PhraseQuery exact = new PhraseQuery();
        exact.setSlop(0);
        for (String term : terms) {
            exact.add(new Term(fieldName, term));
        }

        // jw: next, lets get a reference to the actually query we will be boosting and adding
        Query queryToAdd;

        // jw: if this is a user search we dont need to include the index type in the condition, which is much simpler.
        if (isForIndexTypeOnly(forIndexType)) {
            queryToAdd = exact;

            // jw: guess we need to do the more complicated version.  Yay for utility methods!
        } else {
            BooleanQuery query = new BooleanQuery();
            query.add(forIndexType.getTermQuery(), BooleanClause.Occur.MUST);
            query.add(exact, BooleanClause.Occur.MUST);

            queryToAdd = query;
        }

        // jw: finally, we cant boost and add this query!
        queryToAdd.setBoost(boost);
        addTo.add(queryToAdd, BooleanClause.Occur.SHOULD);
    }

    public static String cleanQueryStringPriorToSolr(String query) {
        if (isEmpty(query)) {
            return query;
        }
        // todo: should we be using this instead?  it escapes spaces, though, so we'd need to make sure we never
        // call this method with spaces
        //query = ClientUtils.escapeQueryChars(query);
        for (ObjectPair<Pattern, String> patternAndReplacement : LUCENE_SPECIAL_STRING_ESCAPE_PATTERNS) {
            query = patternAndReplacement.getOne().matcher(query).replaceAll(patternAndReplacement.getTwo());
        }
        // bl: have to make sure we have an even number of quotation marks.
        // stick a quote onto the end if we have an odd number of quotes.
        int quoteCount = 0;
        for (int i = 0; i < query.length(); i++) {
            quoteCount += query.charAt(i) == '\"' ? 1 : 0;
        }
        query = (quoteCount % 2 == 1) ? query + "\"" : query;

        // bl: now that we are properly escaping the special lucene characters, we don't need any of this behavior.
        /*StringTokenizer st = new StringTokenizer(query, " ");
        List<String> tokens = new ArrayList<String>(st.countTokens());
        // bl: need to strip out leading * and ? on tokens since that will result in Lucene errors.
        // refer: http://forum.murfman.de/en/viewtopic.php?p=116&sid=d58808aa00e1fe910e835d5c3ac35947
        while(st.hasMoreTokens()) {
            String token = st.nextToken();
            if(token!=null) {
                while(token.startsWith("*") || token.startsWith("?")) {
                    token = token.substring(1);
                }
                for (String wordChar : Arrays.asList("+", "-", "!")) {
                    if(IPUtil.isEqual(wordChar, token)) {
                        token = null;
                    }
                }
            }
            if(!IPStringUtil.isEmpty(token)) {
                tokens.add(token);
            }
        }

        if(tokens.isEmpty()) {
            return null;
        }

        query = IPStringUtil.getSeparatedList(tokens, " ").toString();*/

        // bl: use single char wildcards for all other punctuation in the search.
        //query = query.replaceAll("\\p{Punct}", "\\?");
        return query;
    }

    public static String escapeSolrQueryString(String query) {
        if (isEmpty(query)) {
            return query;
        }
        for (ObjectPair<Pattern, String> patternAndReplacement : LUCENE_SPECIAL_STRING_SOLR_ESCAPE_PATTERNS) {
            query = patternAndReplacement.getOne().matcher(query).replaceAll("\\\\" + patternAndReplacement.getTwo());
        }

        return query;
    }

    protected static TermQuery createUnweightedTermQuery(String name, String value) {
        TermQuery query = new TermQuery(new Term(name, value));
        query.setBoost(0);

        return query;
    }

    public Collection<IndexType> getIndexTypes() {
        return indexTypes;
    }

    public Collection<CompositionType> getReplyCompositionTypes() {
        return replyCompositionTypes;
    }

    public boolean isCriteriaSpecified() {
        return !isEmpty(queryString) || !isEmpty(exactPhrase) || !isEmpty(requiredWords) || !isEmpty(optionalWords) || !isEmptyOrNull(users);
    }

    public String getQueryString() {
        return queryString;
    }

    @BypassHtmlDisable
    public void setQueryString(String queryString) {
        this.queryString = queryString == null ? null : queryString.trim();
    }

    public String getExactPhrase() {
        return exactPhrase;
    }

    @BypassHtmlDisable
    public void setExactPhrase(String exactPhrase) {
        this.exactPhrase = exactPhrase;
    }

    public String getRequiredWords() {
        return requiredWords;
    }

    @BypassHtmlDisable
    public void setRequiredWords(String requiredWords) {
        this.requiredWords = requiredWords;
    }

    public String getOptionalWords() {
        return optionalWords;
    }

    @BypassHtmlDisable
    public void setOptionalWords(String optionalWords) {
        this.optionalWords = optionalWords;
    }

    public String getNotWords() {
        return notWords;
    }

    @BypassHtmlDisable
    public void setNotWords(String notWords) {
        this.notWords = notWords;
    }

    public Collection<User> getUsers() {
        return users;
    }

    public void setUsers(Collection<User> users) {
        this.users = users;
    }

    public boolean isHasAttachments() {
        return hasAttachments;
    }

    public void setHasAttachments(boolean hasAttachments) {
        this.hasAttachments = hasAttachments;
    }

    public Boolean getAllowReplies() {
        return allowReplies;
    }

    public void setAllowReplies(Boolean allowReplies) {
        this.allowReplies = allowReplies;
    }

    protected Portfolio getPortfolio() {
        return portfolio;
    }

    private boolean isForIndexTypeOnly(IndexType indexType) {
        return indexTypes.size() == 1 && indexTypes.contains(indexType);
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
