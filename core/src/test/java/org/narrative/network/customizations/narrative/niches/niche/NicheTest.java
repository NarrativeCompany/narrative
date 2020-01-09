package org.narrative.network.customizations.narrative.niches.niche;

import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssueType;
import org.narrative.network.shared.context.AreaContext;
import org.narrative.network.shared.security.PrimaryRole;
import org.narrative.network.shared.util.NetworkCoreUtils;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.Assert.*;

class NicheTest {

    @Tested
    Niche niche;

    @Mocked
    AreaContext areaContext;
    @Mocked
    PrimaryRole primaryRole;

    @BeforeEach
    void setup() {
        new Expectations(NetworkCoreUtils.class) {{
            NetworkCoreUtils.areaContext();
            minTimes=0;
            result = areaContext;

            areaContext.getPrimaryRole();
            minTimes=0;
            result = primaryRole;
        }};
    }

    @Test
    void getAvailableTribunalIssueTypes_nullPossibleTribunalIssueType_returnsEmptyList() {
        new Expectations(niche) {{
            niche.getPossibleTribunalIssueType();
            result = null;
        }};

        assertTrue(niche.getAvailableTribunalIssueTypes().isEmpty());
    }

    @Test
    void getAvailableTribunalIssueTypes_ratifyNicheTribunalIssueType_returnsOnlyRatifyNiche() {
        new Expectations(niche) {{
            niche.getPossibleTribunalIssueType();
            result = TribunalIssueType.RATIFY_NICHE;
        }};

        List<TribunalIssueType> tribunalIssueTypes = niche.getAvailableTribunalIssueTypes();
        assertFalse(tribunalIssueTypes.isEmpty());
        assertEquals(1, tribunalIssueTypes.size());
        assertEquals(TribunalIssueType.RATIFY_NICHE, tribunalIssueTypes.get(0));

    }

    @Test
    void getAvailableTribunalIssueTypes_ownerAndRatifyNicheTribunalIssueType_returnsApproveNicheDetailChange() {
        new Expectations(niche) {{
            niche.isCurrentRoleOwner();
            result = true;
            niche.isCanCurrentRoleEditDetails();
            result = true;
        }};

        List<TribunalIssueType> tribunalIssueTypes = niche.getAvailableTribunalIssueTypes();
        assertFalse(tribunalIssueTypes.isEmpty());
        assertEquals(1, tribunalIssueTypes.size());
        assertTrue(tribunalIssueTypes.contains(TribunalIssueType.APPROVE_NICHE_DETAIL_CHANGE));

    }
}