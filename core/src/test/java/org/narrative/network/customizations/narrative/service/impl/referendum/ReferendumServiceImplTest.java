package org.narrative.network.customizations.narrative.service.impl.referendum;

import org.narrative.network.customizations.narrative.niches.referendum.dao.ReferendumDAO;
import org.narrative.network.customizations.narrative.service.api.AreaTaskExecutor;
import org.narrative.network.customizations.narrative.service.api.model.PageDataDTO;
import org.narrative.network.customizations.narrative.service.api.model.ReferendumDTO;
import org.narrative.network.customizations.narrative.service.mapper.ReferendumMapper;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;

public class ReferendumServiceImplTest {

    @Tested
    private ReferendumServiceImpl referendumService;

    @Injectable
    private ReferendumMapper referendumMapper;

    @Injectable
    private AreaTaskExecutor areaTaskExecutor;

    @Injectable
    private ReferendumDAO referendumDAO;

    // @Test
    public void findBallotBox_emptyList() {
        List<ReferendumDTO> ref = new ArrayList<>();
        new Expectations() {{
            areaTaskExecutor.executeAreaTask((AreaTaskImpl) any);
            result = ref;
        }};
        PageRequest pageRequest = PageRequest.of(0, 1);
        PageDataDTO<ReferendumDTO> result = referendumService.findReferendums(pageRequest);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getInfo().getTotalElements(), 0L);
        Assert.assertEquals(result.getInfo().getTotalPages(), 0L);
    }

    // @Test
    public void findBallotBox() {
        List<ReferendumDTO> ref = new ArrayList<>();
        ref.add(ReferendumDTO.builder().build());

        new Expectations() {{
            areaTaskExecutor.executeAreaTask((AreaTaskImpl) any);
            result = ref;
        }};
        PageRequest pageRequest = PageRequest.of(0, 1);
        PageDataDTO<ReferendumDTO> result = referendumService.findReferendums(pageRequest);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getInfo().getTotalElements(), 1L);
        Assert.assertEquals(result.getInfo().getTotalPages(), 1L);
    }
}