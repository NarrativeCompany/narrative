package org.narrative.network.customizations.narrative.niches.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.SubListIterator;
import org.narrative.common.util.Timer;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;

import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/21/18
 * Time: 8:49 AM
 */
public abstract class SendBulkNarrativeEmailTaskBase extends SendNarrativeEmailTaskBase {

    // jw: the assumption is that while the result might be empty, it will never be null.
    protected abstract List<OID> getUserOidsToNotify();

    // jw: called when preparing to send a chunk of emails, implement to saturate your necessary business objects!
    protected abstract void setupForChunk(List<User> users);

    protected boolean isRunSynchronously() {
        // bl: by default, most emails are sent at the end of partition group, not synchronously
        return false;
    }

    // jw: let's provide a way for the instance to log for each chunk of users.
    protected NetworkLogger getLogger() {
        return null;
    }

    @Override
    protected Object doMonitoredTask() {
        // jw: it's important to notice that we get the userOid list immediately within the same transaction we are ran in.
        //     this means anything that impacts this list should be done before this task is called.
        List<OID> userOids = getUserOidsToNotify();

        // jw: if we do not have any emails, then bail!
        if (isEmptyOrNull(userOids)) {
            return null;
        }

        // jw: before we move on to processing let's setup a timer if we have a logger.
        Timer timer = getLogger()==null ? null : new Timer(getLogger(), getProcess());

        OID areaOid = getAreaContext().getArea().getOid();
        SendBulkNarrativeEmailTaskBase task = this;
        Runnable runnable = () -> {
            SubListIterator<OID> userOidChunks = new SubListIterator<>(userOids, SubListIterator.CHUNK_SMALL);
            while (userOidChunks.hasNext()) {
                List<OID> userOidChunk = userOidChunks.next();

                if (timer!=null) {
                    // jw: because we already fetched a chunk our of the SubListIterator the currentIndex is offset by
                    //     our current results. As a result, to get ther starting index we need to decrement the currentIndex
                    //     by the number of users in this chunk.
                    int startingIndex = userOidChunks.getCurrentIndex()-userOidChunk.size();

                    // jw: now that we have the starting index, creating the logger message.
                    timer.start("Processing emails "+startingIndex+"-"+userOidChunks.getCurrentIndex()+ " out of "+userOids.size()+" total users.");
                }
                try {
                    // jw: To this point there are no email tasks that are updating objects as part of the email processing,
                    //     which if we are hbonest with ourselves should never happen. So, making this read-only.
                    TaskRunner.doRootAreaTask(areaOid, new AreaTaskImpl<Object>(false) {
                        @Override
                        protected Object doMonitoredTask() {
                            // jw: since we are using the same task that was initially called, we need to make sure we set it
                            //     up with the new network and area contexts.
                            task.doSetNetworkContext(getNetworkContext());
                            task.doSetAreaContext(getAreaContext());

                            List<User> users = User.dao().getObjectsFromIDsWithCache(userOidChunk);

                            // jw: for each cycle, we need to do any setup necessary
                            setupForChunk(users);

                            for (User user : users) {
                                sendEmailForUser(user);
                            }

                            return null;
                        }
                    });

                } finally {
                    if (timer!=null) {
                        timer.finish();
                    }
                }
            }
        };

        if(isRunSynchronously()) {
            runnable.run();
        } else {
            // jw: there are two reasons to use a endOfPartitionGroupRunnable
            //     1) We will only send the emails when the transactions is committed successfully.
            //     2) We can chunk through large email sets without slowing down the current request.
            PartitionGroup.addEndOfPartitionGroupRunnable(runnable);
        }

        return null;
    }
}
