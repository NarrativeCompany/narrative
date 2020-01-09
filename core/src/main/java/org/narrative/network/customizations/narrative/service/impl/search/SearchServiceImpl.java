package org.narrative.network.customizations.narrative.service.impl.search;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.InvalidParamError;
import org.narrative.network.core.area.portfolio.Portfolio;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.search.ContentSearchResult;
import org.narrative.network.core.search.SearchResult;
import org.narrative.network.core.search.SearchResultImpl;
import org.narrative.network.core.search.UserSearchResult;
import org.narrative.network.core.search.actions.SearchType;
import org.narrative.network.core.search.services.AreaSearcherTask;
import org.narrative.network.core.search.services.IndexSearcherTask;
import org.narrative.network.core.search.services.SearchCriteria;
import org.narrative.network.core.search.services.SearchPaginationParams;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.channels.FollowedChannel;
import org.narrative.network.customizations.narrative.controller.SearchController;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.niche.NicheStatus;
import org.narrative.network.customizations.narrative.niches.niche.services.NicheSearchResult;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.services.PublicationSearchResult;
import org.narrative.network.customizations.narrative.service.api.AreaTaskExecutor;
import org.narrative.network.customizations.narrative.service.api.SearchService;
import org.narrative.network.customizations.narrative.service.api.model.NicheDTO;
import org.narrative.network.customizations.narrative.service.api.model.SearchResultDTO;
import org.narrative.network.customizations.narrative.service.api.model.SearchResultsDTO;
import org.narrative.network.customizations.narrative.service.impl.niche.FindSimilarNichesTask;
import org.narrative.network.customizations.narrative.service.mapper.NicheDerivativeMapper;
import org.narrative.network.customizations.narrative.service.mapper.NicheMapper;
import org.narrative.network.customizations.narrative.service.mapper.PostMapper;
import org.narrative.network.customizations.narrative.service.mapper.PublicationMapper;
import org.narrative.network.customizations.narrative.service.mapper.UserMapper;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

@Service
@Transactional
public class SearchServiceImpl implements SearchService {
    private final AreaTaskExecutor areaTaskExecutor;
    private final UserMapper userMapper;
    private final NicheMapper nicheMapper;
    private final PublicationMapper publicationMapper;
    private final NicheDerivativeMapper nicheDerivativeMapper;
    private final PostMapper postMapper;

    public SearchServiceImpl(AreaTaskExecutor areaTaskExecutor, UserMapper userMapper, NicheMapper nicheMapper, PublicationMapper publicationMapper, NicheDerivativeMapper nicheDerivativeMapper, PostMapper postMapper) {
        this.areaTaskExecutor = areaTaskExecutor;
        this.userMapper = userMapper;
        this.nicheMapper = nicheMapper;
        this.publicationMapper = publicationMapper;
        this.nicheDerivativeMapper = nicheDerivativeMapper;
        this.postMapper = postMapper;
    }

    /**
     * Searches database records based on index types, starting index, and keyword query string.
     *
     * @param keyword Query string.
     * @param searchType {@link SearchType} to search.
     * @param channelOid {@link OID} of the channel to filter results to
     * @param startIndex Starting index for search.
     * @param count Number of items to include in results
     * @return {@link SearchResultsDTO} based on index and keyword results.
     * @throws IllegalArgumentException If keyword or pageable is empty or null.
     */
    @Override
    public SearchResultsDTO search(String keyword, SearchType searchType, OID channelOid, Integer startIndex, int count) throws IllegalArgumentException {
        assert count > 0 : "The SearchController is annotated as @Positive for this field, so how did we get a zero or negative number?";

        if (StringUtils.isEmpty(keyword)) {
            throw new IllegalArgumentException("Search string must be provided.");
        }

        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<SearchResultsDTO>(false) {
            @Override
            protected SearchResultsDTO doMonitoredTask() {
                // jw: let's default to a EVERYTHING search
                SearchType searchTypeResolved = searchType==null ? SearchType.EVERYTHING : searchType;
                if (!SearchType.SUPPORTED_TYPES.contains(searchTypeResolved)) {
                    throw new InvalidParamError(SearchController.FILTER_PARAM, searchType.getIdStr());
                }

                Portfolio portfolio = getAreaContext().getPortfolio();
                SearchCriteria searchCriteria = searchTypeResolved.getSearcherCriteria(
                        areaTaskExecutor.getArea(),
                        portfolio,
                        false
                );

                searchCriteria.setQueryString(keyword);

                Channel channel = channelOid==null ? null : Channel.dao().getForApiParam(channelOid, SearchController.CHANNEL_PARAM);

                // jw: if we have a channel, and its a publication we need to ensure it is not expired. Searching from the
                //     publication should not be available while the publication is expired.
                if (exists(channel) && channel.getType().isPublication()) {
                    Publication publication = channel.getPublication();

                    // jw: there is no reason for the admin to need access to search when the publication is expired.
                    publication.assertNotExpired(false);
                }

                searchCriteria.setChannel(channel);

                SearchPaginationParams pagination = new SearchPaginationParams();
                pagination.setSort(IndexSearcherTask.IndexSort.SCORE);
                // jw: since we will be using index based load more, we will always be specifying the first page, and rely on the the maxIndex below to
                //     get us to the first result we expect.
                pagination.setPage(1);
                pagination.setRowsPerPage(count);
                // jw: only set the maxIndex if we have one specified
                if (startIndex !=null) {
                    pagination.setMaxIndex(Collections.singletonList(startIndex));
                }

                AreaSearcherTask task = new AreaSearcherTask(searchCriteria, pagination, portfolio);

                List<SearchResultImpl> searchResults = getAreaContext().doAreaTask(task);
                if (isEmptyOrNull(searchResults)) {
                    return SearchResultsDTO.builder()
                            .items(null)
                            .hasMoreItems(false)
                            .lastResultIndex(null)
                            .build();
                }

                List<Channel> channels = new LinkedList<>();
                for (SearchResult item : searchResults) {
                    if (item.getIndexType().isNiche()) {
                        channels.add(((NicheSearchResult)item).getNiche().getChannel());

                    } else if (item.getIndexType().isPublication()) {
                        channels.add(((PublicationSearchResult)item).getPublication().getChannel());

                    } else if (item.getIndexType().isContent()) {
                        ContentSearchResult contentSearchResult = (ContentSearchResult) item;
                        Content content = (Content)contentSearchResult.getCompositionConsumer();

                        channels.addAll(content.getPublishedToChannels());
                    }
                }

                FollowedChannel.dao().populateChannelsFollowedByCurrentUserField(getNetworkContext().getUser(), channels);

                List<SearchResultDTO> items = new ArrayList<>(searchResults.size());
                for (SearchResultImpl item : searchResults) {
                    // jw: let's setup the SearchResultDTOBuilder.
                    SearchResultDTO.SearchResultDTOBuilder resultBuilder = SearchResultDTO.builder()
                            .oid(item.getOid());

                    if (item.getIndexType().isUser()) {
                        UserSearchResult result = (UserSearchResult) item;
                        resultBuilder.userDetail(userMapper.mapUserEntityToUserDetail(result.getUser()));

                    } else if (item.getIndexType().isNiche()) {
                        NicheSearchResult result = (NicheSearchResult) item;
                        resultBuilder.niche(nicheMapper.mapNicheEntityToNiche(result.getNiche()));

                    } else if (item.getIndexType().isPublication()) {
                        PublicationSearchResult result = (PublicationSearchResult) item;
                        resultBuilder.publication(publicationMapper.mapPublicationToDto(result.getPublication()));

                    } else {
                        assert item.getIndexType().isContent() : "Expected content, but got/"+item.getIndexType();

                        ContentSearchResult result = (ContentSearchResult) item;
                        assert result.getCompositionConsumer().getCompositionConsumerType().isNarrativePost() : "Expected the search result to be a Narrative Post, not/"+result.getCompositionConsumer().getCompositionConsumerType();
                        resultBuilder.post(postMapper.mapContentToPostDTO((Content) result.getCompositionConsumer()));
                    }

                    items.add(resultBuilder.build());
                }

                return SearchResultsDTO.builder()
                        .items(items)
                        .hasMoreItems(searchResults.size() == count)
                        .lastResultIndex(searchResults.get(searchResults.size() - 1).getResultIndex())
                        .build();
            }
        });
    }

    @Override
    public List<NicheDTO> findActiveNichesByName(String name, int count) throws IllegalArgumentException {
        // jw: first, let's try and get our results from a db prefix match.
        List<Niche> results = Niche.dao().getActiveNichesByName(name, count);

        // jw: if we do not have enough niches from just that query, let's go to lucene and try to find more.
        if (results.size() < count) {
            FindSimilarNichesTask task = new FindSimilarNichesTask(name, null, null);
            task.setMaxResults(count);
            task.setLimitToStatus(NicheStatus.ACTIVE);
            task.setSearchUnstemmedNameOnly(true);

            List<Niche> searchResults = areaTaskExecutor.executeAreaTask(task);

            // jw: first, let's remove all of the results we got from the db to ensure there are no dups
            searchResults.removeAll(results);

            // jw: next, lets add as many results as we need, or are available, whatever is lower.
            results.addAll(searchResults.subList(0, Math.min(searchResults.size(), count - results.size())));
        }

        // jw: finally, let's populate the currentUserFollowedItem records and call it a day.
        FollowedChannel.dao().populateChannelConsumersFollowedByCurrentUserField(networkContext().getUser(), results);

        return nicheDerivativeMapper.mapNicheEntityListToNicheList(results);
    }
}
