package org.narrative.network.customizations.narrative.niches.tribunal.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.channels.ChannelConsumer;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.customizations.narrative.niches.referendum.metadata.NicheDetailChangeReferendumMetadata;
import org.narrative.network.customizations.narrative.niches.referendum.services.CreateReferendumTask;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssue;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssueType;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/15/18
 * Time: 10:29 AM
 */
public class CreateTribunalIssueTask extends AreaTaskImpl<TribunalIssue> {
    private final ChannelConsumer channelConsumer;
    private final TribunalIssueType type;
    private final String newName;
    private final String newDescription;
    private boolean createdIssue;

    public CreateTribunalIssueTask(ChannelConsumer channelConsumer, TribunalIssueType type) {
        assert type != null && isEqual(channelConsumer.getPossibleTribunalIssueType(), type) : "TribunalIssueType must be valid for niche /" + type;

        this.channelConsumer = channelConsumer;
        this.type = type;
        this.newName = null;
        this.newDescription = null;
    }

    public CreateTribunalIssueTask(Niche niche, String newName, String newDescription) {
        assert !isEmpty(newName) || !isEmpty(newDescription) : "TribunalIssueType must be valid for niche/" + niche.getOid();

        this.channelConsumer = niche;
        this.type = TribunalIssueType.APPROVE_NICHE_DETAIL_CHANGE;
        this.newName = newName;
        this.newDescription = newDescription;
    }

    @Override
    protected TribunalIssue doMonitoredTask() {
        // jw: first things first, let's see if there is already a open issue with this type
        TribunalIssue issue = TribunalIssue.dao().getOpenForChannelAndType(channelConsumer.getChannel(), type);
        if (exists(issue)) {
            // jw: lets just use this bad boy!
            return issue;
        }

        // jw: since we do not have a issue, let's try and see if we have to create one, but let's do that in a small
        //     root area task so we minimize the lock time.
        OID issueOid = TaskRunner.doRootAreaTask(Area.dao().getNarrativePlatformArea().getOid(), new AreaTaskImpl<OID>(true) {
            @Override
            protected OID doMonitoredTask() {
                // jw: now, let's lock on the Channel.
                Channel channel = Channel.dao().getLocked(CreateTribunalIssueTask.this.channelConsumer.getOid());

                // jw: check one more time now that we have the lock to see if the issue has been created.
                TribunalIssue issue = TribunalIssue.dao().getOpenForChannelAndType(channel, type);

                if (!exists(issue)) {
                    issue = new TribunalIssue(channel, type);
                    // jw: kinda big, we need to create the referendum for the tribunal as well.
                    Referendum referendum = getAreaContext().doAreaTask(new CreateReferendumTask(channel.getConsumer(), type.getReferendumTypeForTribunal()));
                    issue.setReferendum(referendum);

                    if (type.isApproveNicheDetailChange()) {
                        NicheDetailChangeReferendumMetadata referendumMetadata = referendum.getMetadata();
                        referendumMetadata.setup(channel.getConsumer(), newName, newDescription);
                    }

                    TribunalIssue.dao().save(issue);

                    referendum.setTribunalIssue(issue);

                    createdIssue = true;
                }

                return issue.getOid();
            }
        });

        // jw: since the issue was created in an isolated transaction above, we need to fetch it in our current transaction.
        assert issueOid != null : "Should always have a issue OID by this point!";
        return TribunalIssue.dao().get(issueOid);
    }

    public boolean isCreatedIssue() {
        return createdIssue;
    }
}
