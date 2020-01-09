package org.narrative.network.customizations.narrative.service.impl.publication;

import org.narrative.common.util.ValidationHandler;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.channels.ChannelUser;
import org.narrative.network.customizations.narrative.channels.channel.services.UpdateFollowedChannelTask;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.SaveLedgerEntryTask;
import org.narrative.network.customizations.narrative.permissions.NarrativePermissionType;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.PublicationPlanType;
import org.narrative.network.customizations.narrative.publications.PublicationRole;
import org.narrative.network.customizations.narrative.publications.PublicationWaitListEntry;
import org.narrative.network.customizations.narrative.publications.services.ProcessPublicationExpiringJob;
import org.narrative.network.customizations.narrative.publications.services.PublicationLogoProcessor;
import org.narrative.network.customizations.narrative.service.api.model.input.CreatePublicationInput;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import java.util.EnumSet;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-07-31
 * Time: 13:10
 *
 * @author jonmark
 */
public class CreatePublicationTask extends AreaTaskImpl<Publication> {
    private final CreatePublicationInput input;
    private final PublicationLogoProcessor logoProcessor;


    public CreatePublicationTask(CreatePublicationInput input) {
        assert input != null : "The input object should always be provided to this task!";

        this.input = input;
        logoProcessor = new PublicationLogoProcessor(input.getLogo(), CreatePublicationInput.Fields.logo, "createPublicationTask.publicationLogo", null);
    }

    @Override
    protected void validate(ValidationHandler validationHandler) {
        /*
         * From SubmitNicheDetailsActionBase#checkRightAfterParams
         */
        NarrativePermissionType.CREATE_PUBLICATIONS.checkRight(getAreaContext().getAreaRole());

        if (!input.isAgreedToAup()) {
            validationHandler.addFieldError(CreatePublicationInput.Fields.agreedToAup, "createPublicationTask.youMustAgreeToAup");
        }

        validateCorePublicationFields(validationHandler, input.getName(), input.getDescription());

        // jw: we only need to validate the logo if one is provided.
        logoProcessor.validate(validationHandler);
    }

    public static void validateCorePublicationFields(ValidationHandler handler, String name, String description) {
        // jw: technically the Fields referenced here is only 100% accurate for creation, not update, but the update fields shares the same names so leaving for simplicity of contract.
        handler.validateString(name, Publication.MIN_NAME_LENGTH, Publication.MAX_NAME_LENGTH, CreatePublicationInput.Fields.name, "createPublicationTask.publicationName");
        handler.validateString(description, Publication.MIN_DESCRIPTION_LENGTH, Publication.MAX_DESCRIPTION_LENGTH, CreatePublicationInput.Fields.description, "createPublicationTask.publicationDescription");
    }

    @Override
    protected Publication doMonitoredTask() {
        User user = getNetworkContext().getUser();
        // bl: only allow wait list discounts up until the end date.
        PublicationWaitListEntry waitListEntry = PublicationWaitListEntry.isAreWaitListDiscountsAllowedForNewPublications() ?
                // bl: get the entry locked to avoid concurrency issues. each entry may only be redeemed once.
                PublicationWaitListEntry.dao().getWaitListEntryLocked(user) :
                null;

        // jw: first, we need to create the publication.
        Publication publication = new Publication(
                input.getName(),
                input.getDescription(),
                Publication.dao().getAvailablePrettyUrlString(input.getName()),
                // jw: all new publications are created on the BASIC plan.
                PublicationPlanType.BASIC,
                user
        );

        Publication.dao().save(publication);

        // bl: if there is an unused waitListEntry, then we'll associate it to this Publication so that the discount is applied
        if(exists(waitListEntry) && !waitListEntry.isUsed()) {
            // track the original user (owner/creator) who claimed the discount
            waitListEntry.setClaimer(user);
            // bl: setting the publication here is what will activate the discount when the publication is paid for
            waitListEntry.setPublication(publication);
            // bl: also mark it as used so that it can't be used again
            waitListEntry.setUsed(true);
        }

        // jw: before we move on let's process the logo if we were given one.
        // jw: since this is a new publication we need to explicitly provide it as part of the processing trigger.
        logoProcessor.setConsumer(publication);
        logoProcessor.process();

        // jw: now that the Publication has been saved, let's store the Channel. We need to do this after since the Publication
        //     needs to have its OID generated first.
        Channel channel = new Channel(publication);
        Channel.dao().save(channel);
        publication.setChannel(channel);

        // jw: now that the Channel exists let's setup the ChannelUser for the owner.
        ChannelUser channelUser = new ChannelUser(channel, publication.getOwner());
        // jw: for now, let's go ahead and make the owner fulfill all of the roles.
        channelUser.addRoles(EnumSet.allOf(PublicationRole.class));
        ChannelUser.dao().save(channelUser);

        // jw: finally, we need to create the invoice for the publication.
        LedgerEntry entry = new LedgerEntry(getAreaContext().getAreaUserRlm(), LedgerEntryType.PUBLICATION_CREATED);
        entry.setChannel(channel);
        // jw: unlike Niche suggestion, I'm not sure it makes sense to track the original name/description for this Publication.
        //     Could be useful if we report name/description changes.
        getNetworkContext().doGlobalTask(new SaveLedgerEntryTask(entry));

        // jw: before we schedule the expiring emails let's have the owner follow the publication.
        getNetworkContext().doGlobalTask(new UpdateFollowedChannelTask(channel, true));

        // jw: let's schedule any reminder emails that might fall within the trial period:
        ProcessPublicationExpiringJob.schedule(publication);

        return publication;
    }
}
