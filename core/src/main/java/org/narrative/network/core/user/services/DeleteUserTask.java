package org.narrative.network.core.user.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.base.AreaStats;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.area.user.WatchedUser;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.fileondisk.base.services.DeleteFileOnDisk;
import org.narrative.network.core.fileondisk.image.ImageOnDisk;
import org.narrative.network.core.security.area.community.advanced.AreaCircle;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.UserKyc;
import org.narrative.network.core.user.UserStatus;
import org.narrative.network.customizations.narrative.service.api.model.kyc.UserKycStatus;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Dec 18, 2006
 * Time: 1:26:59 PM
 */
public class DeleteUserTask extends AreaTaskImpl<Object> {
    private User user;
    private final OID userOid;

    public DeleteUserTask(OID userOid) {
        this.userOid = userOid;
    }

    protected Object doMonitoredTask() {

        user = User.dao().get(userOid);

        final AuthZone authZone = user.getAuthZone();

        // jw: first things first, let's make sure we remove this user from any Niches they may own, and put them up for sale.
        getAreaContext().doAreaTask(new CleanupNichesForDeletedOrDeactivatedUserTask(user));

        // bl: also need to abandon all of the user's owned Publications
        getAreaContext().doAreaTask(new CleanupPublicationsForDeletedOrDeactivatedUserTask(user));

        String newDisplayName = "u" + OIDGenerator.getNextOID();
        //rename the displayName and e-mails to prevent unique issues
        user.updateDisplayName(newDisplayName);
        // bl: free up the username to be used by other users
        user.setUsername(null);

        Set<AreaUser> areaUsers = user.getAreaUsers();

        //mk: do not run existing deletion code for already deleted members.
        if (!user.getUserStatus().isThis(UserStatus.DELETED)) {
            //set the user deleted status
            user.getUserStatus().turnOn(UserStatus.DELETED);

            //mk: set date when the user was deleted
            user.getPreferences().setLastDeactivationDatetime(now());

            user.getUserFields().getEmailAddress().setEmailAddress(OIDGenerator.getNextOID() + "@deleted.narrative.org");

            if (user.isHasInternalCredentials()) {
                user.getInternalCredentials().setEmailAddress(user.getEmailAddress());
            }

            WatchedUser.dao().deleteAllForUser(user);

            // Delete KYC info
            UserKyc userKyc = user.getUserKyc();
            if (exists(userKyc)) {
                userKyc.setKycStatus(UserKycStatus.NONE);
                userKyc.setLastUpdated(Instant.now());
                userKyc.setUserDetailHash(null);
                userKyc.setBirthMonth(null);
                userKyc.setBirthYear(null);
                userKyc.setCountry(null);
            }

            disableAllAreaUsers(areaUsers);

            ImageOnDisk avatar = user.getAvatar();
            if (exists(avatar)) {
                user.updateAvatar(null);
                getNetworkContext().doGlobalTask(new DeleteFileOnDisk(avatar));
            }
        }

        // jw: finally, let's transfer whatever may be left in their wallet to the current reward pool.
        getAreaContext().doAreaTask(new ReclaimNrveFromDeletedUserTask(user));

        return null;
    }

    private void disableAllAreaUsers(Set<AreaUser> areaUsers) {
        final Set<OID> areaOids = new HashSet<>();
        for (AreaUser areaUser : areaUsers) {
            Area area = areaUser.getArea();

            // bl: delete all of the AreaCircleUsers for this user, too
            Set<AreaCircle> areaCircles = new HashSet<>(areaUser.getAreaCircleUsersInited().keySet());
            for (AreaCircle areaCircle : areaCircles) {
                areaUser.removeFromAreaCircle(areaCircle);
            }

            //need the oid to reset stats later.
            areaOids.add(area.getOid());
        }

        if (!areaOids.isEmpty() && !NetworkRegistry.getInstance().isImporting()) {
            //add a runnable to update the area stats at the end of this partition group, in its own transaction
            // bk: this seems over the top, all we really need to do is update the user count right?
            // bl: yeah, recalculating all area stats was over the top. we used to have to recalculate content counts, too,
            // but now that we only disable/delete content on the personal site, it is literally just a member count now.
            PartitionGroup.addEndOfPartitionGroupRunnableForUtilityThread(new Runnable() {
                public void run() {
                    for (final OID areaOid : areaOids) {
                        TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>() {
                            @Override
                            protected Object doMonitoredTask() {
                                AreaStats areaStats = AreaStats.dao().getLocked(areaOid);
                                areaStats.updateMemberCount();
                                return null;
                            }
                        });
                    }
                }
            });
        }
    }
}
