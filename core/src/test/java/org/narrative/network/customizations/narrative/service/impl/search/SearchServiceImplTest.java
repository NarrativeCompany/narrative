package org.narrative.network.customizations.narrative.service.impl.search;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.search.actions.SearchType;
import org.narrative.network.customizations.narrative.niches.niche.NicheStatus;
import org.narrative.network.customizations.narrative.service.api.AreaTaskExecutor;
import org.narrative.network.customizations.narrative.service.api.model.NicheDTO;
import org.narrative.network.customizations.narrative.service.api.model.SearchResultDTO;
import org.narrative.network.customizations.narrative.service.api.model.SearchResultsDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserDetailDTO;
import org.narrative.network.customizations.narrative.service.mapper.NicheDerivativeMapper;
import org.narrative.network.customizations.narrative.service.mapper.NicheMapper;
import org.narrative.network.customizations.narrative.service.mapper.PostMapper;
import org.narrative.network.customizations.narrative.service.mapper.PublicationMapper;
import org.narrative.network.customizations.narrative.service.mapper.UserMapper;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.util.NetworkLogger;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class SearchServiceImplTest {

    @Tested
    private SearchServiceImpl searchService;
    @Injectable
    private NetworkLogger logger;
    @Injectable
    private AreaTaskExecutor areaTaskExecutor;
    @Injectable
    private UserMapper userMapper;
    @Injectable
    private NicheMapper nicheMapper;
    @Injectable
    private NicheDerivativeMapper nicheDerivativeMapper;
    @Injectable
    private PostMapper postMapper;
    @Injectable
    private PublicationMapper publicationMapper;

    @Test
    void search_NullKeyword() {
        assertThrows(IllegalArgumentException.class, () -> searchService.search("", null, null, 1, 1), "Search string must be provided.");
    }

    @Test
    void search() {
        List<SearchResultDTO> nicheAndUsers = new ArrayList<>();
        nicheAndUsers.add(buildNicheSearchResult());
        nicheAndUsers.add(buildUserSearchResult());
        SearchResultsDTO taskResult = SearchResultsDTO.builder()
                .lastResultIndex(2)
                .items(nicheAndUsers)
                .hasMoreItems(true)
                .build();

        new Expectations() {{
            areaTaskExecutor.executeAreaTask((AreaTaskImpl) any);
            result = taskResult;
        }};
        SearchResultsDTO result = searchService.search("test", SearchType.NICHES, null, null, 2);
        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getItems());
        Assert.assertEquals(result.getItems().size(), 2);
    }

    private SearchResultDTO buildUserSearchResult() {
        OID oid = OID.valueOf(RandomUtils.nextLong());
        return SearchResultDTO.builder()
                .oid(oid)
                .userDetail(UserDetailDTO.builder()
                        .lastVisit(now())
                        .joined(now())
                        .user(UserDTO.builder()
                                .oid(oid)
                                .displayName("JaneDoe")
                                .username("jane-doe")
                                .build()
                        )
                        .build()
                )
                .build();
    }

    private SearchResultDTO buildNicheSearchResult() {
        OID oid = OID.valueOf(RandomUtils.nextLong());
        return SearchResultDTO.builder()
                .oid(oid)
                .niche(NicheDTO.builder()
                        .name("My Niche")
                        .description("Testing.")
                        .status(NicheStatus.ACTIVE)
                        .prettyUrlString("my-niche")
                        .build()
                )
                .build();
    }
}