package org.narrative.network.customizations.narrative.service.impl.user;

import org.narrative.common.persistence.OID;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.fakes.FakePartitionGroup;
import org.narrative.fakes.FakeTask;
import org.narrative.fakes.FakeTaskRunner;
import org.narrative.network.core.area.user.AreaUserStats;
import org.narrative.network.core.area.user.dao.AreaUserStatsDAO;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.UserStats;
import org.narrative.network.core.user.dao.UserStatsDAO;
import org.narrative.network.customizations.narrative.service.api.AreaTaskExecutor;
import org.narrative.network.customizations.narrative.service.api.ContentStreamService;
import org.narrative.network.customizations.narrative.service.api.RewardsService;
import org.narrative.network.customizations.narrative.service.impl.StaticMethodWrapper;
import org.narrative.network.customizations.narrative.service.mapper.EmailAddressMapper;
import org.narrative.network.customizations.narrative.service.mapper.NicheDerivativeMapper;
import org.narrative.network.customizations.narrative.service.mapper.NicheMapper;
import org.narrative.network.customizations.narrative.service.mapper.NicheUserAssociationMapper;
import org.narrative.network.customizations.narrative.service.mapper.PostMapper;
import org.narrative.network.customizations.narrative.service.mapper.PublicationMapper;
import org.narrative.network.customizations.narrative.service.mapper.RewardPeriodMapper;
import org.narrative.network.customizations.narrative.service.mapper.UserMapper;
import org.narrative.network.customizations.narrative.service.mapper.WalletTransactionMapper;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.sql.Date;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceImplTest {

    @Tested
    private UserServiceImpl userService;

    @Injectable
    private AreaTaskExecutor areaTaskExecutor;

    @Injectable
    private UserMapper userMapper;

    @Injectable
    private NicheUserAssociationMapper nicheUserAssociationMapper;

    @Injectable
    private NicheDerivativeMapper nicheDerivativeMapper;

    @Injectable
    private PostMapper postMapper;

    @Injectable
    private NicheMapper nicheMapper;

    @Injectable
    private RewardPeriodMapper rewardPeriodMapper;

    @Injectable
    private WalletTransactionMapper walletTransactionMapper;

    @Injectable
    private EmailAddressMapper emailAddressMapper;

    @Injectable
    private PublicationMapper publicationMapper;

    @Injectable
    private RewardsService rewardsService;

    @Injectable
    private StaticMethodWrapper staticMethodWrapper;

    @Injectable
    private NarrativeProperties narrativeProperties;

    @Injectable
    private ContentStreamService contentStreamService;

    @Test
    void findUser_NullOID() {
        assertThrows(UserPrincipalNotFoundException.class, () -> userService.getUser(null), "Oid is NULL");
    }

    @Test
    void findUser_NoUserFound() {
        OID oid = new OID();
        assertThrows(UserPrincipalNotFoundException.class, () -> {
            new Expectations() {{
                areaTaskExecutor.executeAreaTask((AreaTaskImpl) any);
                result = null;
            }};
            userService.getUser(oid);
        }, "User " + oid.getValue() + " was not found.");
    }

    @Test
    void findUser() throws UserPrincipalNotFoundException {
        OID oid = new OID();
        User user = new User();
        new Expectations() {{
            areaTaskExecutor.executeAreaTask((AreaTaskImpl) any);
            result = user;
        }};
        User result = userService.getUser(oid);
        Assertions.assertNotNull(result);
    }

    @Test
    // todo: should fix this test at some point
    @Disabled
    void updateUserLastLoginTime_givenUser_setsLastLoginDatetime(@Mocked User user, @Mocked UserStatsDAO userStatsDAO, @Mocked UserStats userStats, @Mocked AreaUserStatsDAO areaUserStatsDAO, @Mocked AreaUserStats areaUserStats)  {

        long lastLoginDateTime = DateUtils.addDays(new Date(System.currentTimeMillis()), -2).getTime();

        // Fakes for running a task
        new FakePartitionGroup();
        new FakeTaskRunner();
        new FakeTask();

        new Expectations(UserStats.class, AreaUserStats.class) {{
            UserStats.dao();
            result = userStatsDAO;

            userStatsDAO.getLocked((OID) any);
            result = userStats;

            AreaUserStats.dao();
            result = areaUserStatsDAO;

            areaUserStatsDAO.getLocked((OID) any);
            result = areaUserStats;
        }};

        userService.updateUserLastLoginTime(user);

        new Verifications() {{
            Timestamp newLogin;
            userStats.setLastLoginDatetime(newLogin = withCapture());
            areaUserStats.setLastLoginDatetime(newLogin);

            assertTrue(newLogin.after(new Timestamp(lastLoginDateTime)));
        }};

    }
}
