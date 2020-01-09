package org.narrative.network.customizations.narrative.service.impl.tribunal;

import org.narrative.common.util.ApplicationError;
import org.narrative.common.util.posting.MessageTextMassager;
import org.narrative.network.customizations.narrative.channels.ChannelConsumer;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.SaveLedgerEntryTask;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssue;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssueReport;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssueType;
import org.narrative.network.customizations.narrative.niches.tribunal.services.CreateTribunalIssueTask;
import org.narrative.network.customizations.narrative.niches.tribunal.services.SendNewTribunalIssueReportEmail;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 10/11/18
 * Time: 7:53 AM
 *
 * @author brian
 */
public abstract class CreateTribunalIssueBaseTask<C extends ChannelConsumer> extends AreaTaskImpl<TribunalIssue> {
    final C channelConsumer;
    final TribunalIssueType type;
    final String comment;

    public CreateTribunalIssueBaseTask(C channelConsumer, TribunalIssueType type, String comment) {
        this.channelConsumer = channelConsumer;
        this.type = type;
        this.comment = comment;
    }

    abstract LedgerEntryType getLedgerEntryType();
    abstract CreateTribunalIssueTask getCreateTribunalIssueTask();

    @Override
    protected void validate(ValidationContext validationContext) {
        getNetworkContext().getPrimaryRole().checkRegisteredUser();
    }

    @Override
    protected TribunalIssue doMonitoredTask() {
        // jw: let's get/create the tribunal issue through this task, to make sure that it is done as atomically as possible.
        CreateTribunalIssueTask createIssueTask = getCreateTribunalIssueTask();
        TribunalIssue issue = getAreaContext().doAreaTask(createIssueTask);

        // jw: only allow reports while the issue is open for tribunal voting!
        // bl: this is only possible for niche status reports. niche edits will never get into this case.
        if (exists(issue) && !issue.getReferendum().isOpen()) {
            throw new ApplicationError(wordlet("createTribunalIssueAction.tribunalVotingFinished."+channelConsumer.getChannel().getType()));
        }

        // jw: cache whether we created the issue or not.
        boolean isNewTribunalIssue = createIssueTask.isCreatedIssue();

        String comments = MessageTextMassager.getMassagedTextForBasicTextarea(comment, true);

        // jw: now, let's create the report for this issue.
        // note that for niche edits, we create the TribunalIssueReport with no comment. the purpose there is
        // to track who the editor was that submitted the request (the owner at the time of the edit).
        TribunalIssueReport report = new TribunalIssueReport(comments, getAreaContext().getAreaUserRlm(), issue);
        TribunalIssueReport.dao().save(report);
        issue.getTribunalIssueReports().add(report);
        issue.updateLastReportForNewReport(report);

        // jw: next, we can create the ledger entry for this report
        LedgerEntry entry = new LedgerEntry(getAreaContext().getAreaUserRlm(), getLedgerEntryType());
        entry.setIssue(issue);
        entry.setReferendum(issue.getReferendum());
        if(entry.getType().isIssueReport()) {
            entry.setIssueReport(report);
        }
        entry.setChannel(channelConsumer.getChannel());
        networkContext().doGlobalTask(new SaveLedgerEntryTask(entry));

        // jw: finally, notify the Tribunal about this report
        getAreaContext().doAreaTask(new SendNewTribunalIssueReportEmail(report, isNewTribunalIssue));

        return issue;
    }
}
