package org.narrative.network.core.content.base.services;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.util.IPHTMLUtil;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.ValidationHandler;
import org.narrative.common.util.html.HTMLParser;
import org.narrative.common.util.posting.HtmlTextMassager;
import org.narrative.network.core.area.base.AreaRlm;
import org.narrative.network.core.area.portfolio.Portfolio;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.composition.base.CompositionStats;
import org.narrative.network.core.composition.base.CompositionType;
import org.narrative.network.core.composition.files.FilePointer;
import org.narrative.network.core.composition.files.FilePointerSet;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.content.base.ContentConsumer;
import org.narrative.network.core.content.base.ContentFields;
import org.narrative.network.core.content.base.ContentType;
import org.narrative.network.core.content.base.ContentWithAttachmentsFields;
import org.narrative.network.core.content.base.ContentWrapper;
import org.narrative.network.core.content.base.FileData;
import org.narrative.network.core.content.base.SEOObject;
import org.narrative.network.core.content.base.SEOObjectDAO;
import org.narrative.network.core.fileondisk.base.services.CreateUpdateFiles;
import org.narrative.network.core.fileondisk.base.services.FileUploadUtils;
import org.narrative.network.core.mentions.MentionsHtmlParser;
import org.narrative.network.core.mentions.SendNewMentionsNotificationEmailJob;
import org.narrative.network.core.search.services.ContentIndexRunnable;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.core.user.User;
import org.narrative.network.core.watchlist.services.InstantWatchedContentEmailJob;
import org.narrative.network.customizations.narrative.posts.services.NarrativePostFields;
import org.narrative.network.shared.services.NetworkException;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.util.NetworkConstants;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Sep 18, 2007
 * Time: 4:18:31 PM
 */
public class CreateContentTask extends AreaTaskImpl<Object> {
    private final Portfolio portfolioToPostTo;
    private final User user;
    private final boolean isNew;
    private final Partition compositionPartition;
    private final OID fileUploadProcessOid;
    private final Collection<OID> areaOidsInWhichContentIsNewlyLive = new HashSet<>();
    private final Collection<OID> areaOidsInWhichContentIsEditedLive = new HashSet<>();
    //content fields
    private ContentFields contentFields;
    private ContentType contentType;
    private ContentConsumer contentConsumer;
    private Composition composition;

    //task fields
    private Content content;
    // the following fields are used to determine if this is a significant edit or not
    private int filesAdded = 0;
    private boolean charsChanged = false;

    public CreateContentTask(ContentType contentType, User user, Portfolio portfolioToPostTo, Partition compositionPartition, OID fileUploadProcessOid) {
        this.user = user;
        this.portfolioToPostTo = portfolioToPostTo;
        this.isNew = true;
        this.contentType = contentType;
        this.compositionPartition = compositionPartition;
        this.fileUploadProcessOid = fileUploadProcessOid;
        prepare();
    }

    public CreateContentTask(Content content, Composition composition, OID fileUploadProcessOid) {
        this.content = content;
        this.contentType = content.getContentType();
        this.portfolioToPostTo = content.getPortfolio();
        this.compositionPartition = content.getCompositionPartition();
        this.composition = composition;
        this.user = content.getRealAuthor();
        this.isNew = false;
        this.fileUploadProcessOid = fileUploadProcessOid;
        prepare();
    }

    public boolean isForceNoIsolation() {
        //since we get hibernate objects in the contructor, we want to make sure they are from the same seesion
        return true;
    }

    private void prepare() {
        if (isNew) {
            prepareForNew();
        } else {
            prepareForEdit();
        }
    }

    protected Object doMonitoredTask() {
        assert isEqual(getAreaContext().getArea(), portfolioToPostTo.getArea()) : "Area mismatch between portfolioToPostTo and current area!";

        // now we need to create the composition for this piece of content
        if (isNew()) {
            // composition gets the initial OID
            getComposition().setOid(OIDGenerator.getNextOID());
        }
        getComposition().setBody(getContentFields().getBodyForDatabase());

        // bl: must call save on the ContentConsumer prior to scrubbing the body so that the FilePointers
        // will have been given OIDs, which are required in order for proper URL translation to work.
        if (isNew()) {
            // bl: be sure to pre-populate the last update datetime on CompositionStats.
            getComposition().getCompositionStats().setLastUpdateDatetime(getContentFields().getEventDatetime());
            Composition.dao().save(getComposition());
            getContentType().getDAO().save((DAOObject) getContentConsumer());
        }

        // bl: create the extract once, rather than once for each piece of content.
        // now that we've massaged the body (and censored it), let's auto-generate an extract from the body, if necessary.
        if (IPStringUtil.isEmpty(getContentFields().getBody())) {
            getContentFields().setExtract("");
        } else {
            // now truncate the string to the maximum extract length.
            // bl: be sure to strip html from the body before generating the extract.
            getContentFields().setExtract(getComposition().getBodyAsExtract());
        }

        // bl: we handle files separately during import
        List<FileData> fodsToCreateAndStore = new ArrayList<FileData>();
        if (!NetworkRegistry.getInstance().isImporting()) {
            Collection<? extends FileData> fileData = getContentFields().getFileData();
            if (fileData != null && !fileData.isEmpty()) {
                fodsToCreateAndStore.addAll(fileData);
            }

            // bl: do all of the scrubbing up front (just like we used to).  this way, we will already have translated
            // UBBCode prior to creating the extract, which will prevent the extract from containing UBB Code characters.
            // note that we are passing in nulls here for the temp file url replacement arguments.  this is because
            // at this point, we don't yet know what the replacement urls are.  we need to wait until after processing
            // the message in order to do the temp file url translation.  that will happen in execute above.
            // bl: don't scrub if importing

            getComposition().scrub();

            // bl: now that we have scrubbed the body, set the body back onto ContentFields so that when we get
            // the amount of characters changed, the body that we compare to the originalBody will have had all of
            // the necessary translations done.  previously, we would use the original body (unscrubbed) that was submitted
            // by the user.  thanks to Barry for figuring this out during debugging.  it will certainly help prevent
            // insignificant edits from being marked as significant.
            getContentFields().setBody(getComposition().getBody());

            getContentFields().scrub();

            // see if any characters changed in the various content fields.
            if (getContentFields().isCharsChanged()) {
                charsChanged = true;
            }
        }

        // bl: not allowing deletes from the posting page, so require a "store" if not "new"
        storeContentForArea();

        // bl: we handle files separately during import
        if (!NetworkRegistry.getInstance().isImporting()) {
            // bl: no longer doing this in a new session.  do it all in the same session so that content
            // creation is an all or nothing operation.
            // jw: if the content is authored by a guest, then lets add the attachments by the current user.
            CreateUpdateFiles createFilesTask = new CreateUpdateFiles(fodsToCreateAndStore, exists(user) ? user : getNetworkContext().getUser());

            getNetworkContext().doGlobalTask(createFilesTask);
            // check how many files were added
            filesAdded = createFilesTask.getFilesAdded();
            // check how many chars in titles and descriptions were changed
            if (createFilesTask.isCharsChanged()) {
                charsChanged = true;
            }
        }

        if (contentType.isAllowAttachments()) {
            // bl: have to apply content fields first so that the FileData objects get FilePointers associated with them.
            FilePointerSet<FilePointer> fps = getComposition().getFilePointerSet();
            ContentWithAttachmentsFields attachmentFields = (ContentWithAttachmentsFields) getContentFields();
            UpdateFilePointerSet updateFpsTask = new UpdateFilePointerSet(getComposition(), fps, attachmentFields.getAttachmentList());
            fps = networkContext().doGlobalTask(updateFpsTask);
            getComposition().setFilePointerSet(exists(fps) ? fps : null);
        }

        // massage the content fields only once per submission.
        // scrub the contentFields, which will include content type specific fields.  disable html where necessary
        // and to trim the strings, etc.
        if (!NetworkRegistry.getInstance().isImporting()) {
            // editing a draft going live?  then update the timestamp on the content to be the current time
            if (!getContentFields().isDraft() && getContentFields().isEditOfDraft()) {
                CompositionStats stats = CompositionStats.dao().getLocked(getComposition().getOid());
                stats.setLastUpdateDatetime(getContentFields().getEventDatetime());

            }

            // if this is an edit, mark the composition as such.
            // bl: except when editing drafts and future publications, in which case we don't need
            // to mark it as edited.
            if (!isNew() && !getContentFields().isEditOfDraft()) {
                // bl: only update the edit datetime for significant edits
                if (isSignificantEdit()) {
                    getComposition().setEditDatetime(getContentFields().getEditDatetime());
                    getComposition().setEditorUserOid(getNetworkContext().getUser().getOid());
                }
            }
        }

        // use the event datetime for the save datetime - event datetime is just
        // the time that the content is being created/edited.

        //Always index, let the indexer figure out what to do with it
        // bl: except when importing.  we can just rebuild the full index later.
        if (!NetworkRegistry.getInstance().isImporting()) {
            ContentIndexRunnable.registerContentIndexRunnable(content);
        }

        // now that we've stored the content for the areas, the sessions are still open so that
        // the entire content post is treated as one atomic transaction (as best we can).
        // thus, attempt a flush for all active sessions to see if everything worked out aok.
        PartitionType.flushAllOpenSessionsForCurrentPartitionGroup();

        if (!NetworkRegistry.getInstance().isImporting()) {
            // bl: once we have finished message processing, then translate the temp file urls (since we don't have the
            // replacement urls until after the content has been posted to each area).  note that we are doing the rest
            // of the normal scrubbing prior to posting to all of the areas.
            // note: since we are doing the URL translation after the fact, it is possible that an auto-generated extract may still contain
            // a temp file url.  that is something that i'm willing to live with for now.  i think it will be extremely rare that this would
            // actually happen.
            String body = getComposition().getBody();
            if (getContentFields().isSupportMentions()) {
                body = MentionsHtmlParser.escapeMentions(body);
            }

            // send notifications if content went live to any areas
            if (!areaOidsInWhichContentIsNewlyLive.isEmpty()) {
                InstantWatchedContentEmailJob.schedule(null, content, false, null);
            }

            // jw: if the content is live anywhere then lets process for any new mentions
            if (!areaOidsInWhichContentIsEditedLive.isEmpty() || !areaOidsInWhichContentIsNewlyLive.isEmpty()) {
                if (content.getCompositionConsumerType().isSupportsMentions()) {
                    SendNewMentionsNotificationEmailJob.schedule(null, content, null);
                }
            }
        }

        if (!exists(content)) {
            throw UnexpectedError.getRuntimeException("Failed creating any content!  Should check the log for any Debug.BAD messages.  This shouldn't happen.  Returning generic error to user.", true);
        }

        // clean up any uploaded files off of disk.
        // also will remove this form from the session map.
        if (fileUploadProcessOid != null) {
            FileUploadUtils.cleanUpFilesAfterFormCompletionAtEndOfPartitionGroup(fileUploadProcessOid);
        }

        return null;

    }

    /**
     * store the content for an area
     */
    private void storeContentForArea() {
        // re-use any current active sessions and also leave the sessions open so that other
        // areas in the same realm can re-use them.
        getNetworkContext().doAreaTask(portfolioToPostTo.getArea(), new AreaTaskImpl<OID>() {
            protected OID doMonitoredTask() {
                boolean wasContentLive;
                ContentWrapper contentWrapper;
                if (isNew) {
                    contentWrapper = new ContentWrapper(compositionPartition, user, getContentFields(), portfolioToPostTo, getContentConsumer());
                    wasContentLive = false;
                } else {
                    wasContentLive = content.isContentLive();

                    contentWrapper = new ContentWrapper(content, getContentFields());
                }
                content = contentWrapper.getContent();

                // todo: validation to make sure certain fields (extract, subject, etc.) are
                // within the limits of the field size in the database?

                final boolean isNewInArea = isNew;
                final boolean isEditOfDraft = getContentFields().isEditOfDraft();

                final boolean isSignificantEdit = !isNewInArea && isSignificantEdit();

                // bl: always do a saveOrUpdate of the content.  content has special
                // behavior for saving the primary picture file pointer set that we
                // need to be sure to invoke each time that content is updated by the user.
                // this used to be handled in an EventListener, but that didn't work since
                // the event listener triggers happen on _flush_, not when you call save.
                // once the flush process has started, i've found that it becomes problematic
                // to try to save an object via the event listener.  specifically, you would
                // get a Hibernate error to the effect of:
                // SEVERE: an assertion failure occured (this may indicate a bug in Hibernate, but is more likely due to unsafe use of the session)
                // org.hibernate.AssertionFailure: collection [null] was not processed by flush()
                Content.dao().saveOrUpdate(content);

                final boolean isContentGoingLiveInAreaForFirstTime = !wasContentLive && content.isContentLive() && (isNewInArea || isEditOfDraft);

                // bl: always use the current user for FutureContent so that it shows up in the current user's drafts/future pubs list.
                // otherwise, if you saved for a different author, it would show up in their future publication list.
                // note that this may need to change when we add support for publication tools.
                getAreaContext().doAreaTask(new UpdateFutureContent(content, getNetworkContext().getUser(), getContentFields().isDraft(), getContentFields().getEventDatetime()));

                // don't ever create WatchedContent for drafts.
                // nb. we WILL create WatchedContent for future publications and moderated content at
                // creation time, however.
                // only create the WatchedContent if this is new or if this is the actual
                // submission of the draft content.

                // editing a draft going live?  then update the timestamp on the content to be the current time
                if (content.isContentLive() && isEditOfDraft) {
                    content.updateLiveDatetime(getContentFields().getEventDatetime());
                }

                if (content.getContentType().isNarrativePost()) {
                    // jw: let's allow the NarrativePostFields to handle the heavy lifting. This will better centralize
                    //     this type specific behavior.
                    NarrativePostFields fields = (NarrativePostFields) getContentFields();
                    fields.execute(content);
                }

                // save/update this Content (will only happen if necessary).
                // bl: always create the WatchedContent for the author.  it won't hurt to do this replication
                // even on edits.
                getNetworkContext().doGlobalTask(new UpdateCachedContentStatsForUpdatedContent(content, !isContentGoingLiveInAreaForFirstTime, wasContentLive));

                if (content.isContentLive() && (!wasContentLive || isSignificantEdit)) {
                    if (isContentGoingLiveInAreaForFirstTime) {
                        areaOidsInWhichContentIsNewlyLive.add(getAreaContext().getArea().getOid());
                    } else {
                        areaOidsInWhichContentIsEditedLive.add(getAreaContext().getArea().getOid());
                    }
                }

                // try explicitly flushing the realm session so that we can realize
                // any errors immediately.
                // once we're done with all of the areas, we'll attempt a flush of the global
                // and composition sessions.
                // bl: don't need to do this here anymore, as it will be handled automatically
                // by PartitionType.doTask now anytime that we are leaving the session open.
                //PartitionType.REALM.currentSession().flushSession();

                return content.getOid();
            }

        });
    }

    public User getUser() {
        return user;
    }

    public boolean isSignificantEdit() {
        // all edits are significant if they included adding files
        if (filesAdded > 0) {
            return true;
        }
        // bl: consider it a significant edit if any characters in the post were changed.
        return charsChanged;
    }

    public void setCharsChanged(boolean charsChanged) {
        this.charsChanged = charsChanged;
    }

    public Content getContent() {
        return content;
    }

    private void prepareForNew() {
        // create a new ContentFields from which all of the various
        // content fields can be set.  this class is used purely for
        // demarshalling from an Http request
        // bl: create the ContentFields object for every ContentType.  this way, the ContentFields
        // will be properly maintained through form submissions.  otherwise, i may upload a video
        // under the video content type, then switch to blog post, then submit form (with errors),
        // which will result in the post page displaying and switching to the video content type
        // again would improperly show no video when in fact there is already a video.
        try {
            Class<? extends ContentFields> cls = contentType.getContentFieldsClass();
            Constructor<? extends ContentFields> constructor = cls.getConstructor(User.class, OID.class);
            contentFields = constructor.newInstance(user, fileUploadProcessOid);
        } catch (Throwable t) {
            throw UnexpectedError.getRuntimeException("Unable to create ContentFields for content type: " + contentType, t);
        }
    }

    private void prepareForEdit() {
        // bl: optimization to stay in the current session if possible and to leave the session open
        networkContext().doAreaTask(content.getArea(), new PrepareForEdit());
    }

    public ContentFields getContentFields() {
        return contentFields;
    }

    public Composition getComposition() {
        if (isNew && composition == null) {
            composition = exists(user) ? new Composition(CompositionType.CONTENT, user, portfolioToPostTo.getArea()) : new Composition(CompositionType.CONTENT, portfolioToPostTo.getArea());

            if (!exists(user)) {
                assert getContentFields().isSupportsGuestAuthors() : "Expect that if the author is not set then the content supports guests authors!";
                String guestName = getContentFields().getGuestName();
                assert !isEmpty(guestName) : "We expect that guestName will be set by this point by either the customize author fields or the contentFields!";
                composition.setGuestName(guestName);
            }
        }
        return composition;
    }

    public ContentConsumer getContentConsumer() {
        if (isNew && contentConsumer == null) {
            contentConsumer = contentType.getNewInstance(getComposition());
        }
        return contentConsumer;
    }

    public boolean isNew() {
        return isNew;
    }

    public OID getFileUploadProcessOid() {
        return fileUploadProcessOid;
    }

    public ContentType getContentType() {
        return contentType;
    }

    protected void validate(final ValidationHandler contentValidationHandler) {
        // jw: this is used a lot in here, so lets create a reference so that it will be shorter and easier to identify.
        ContentFields fields = getContentFields();

        // bl: have to disable html in the subject and extract before validating them to ensure that they
        // will fit in the specified column sizes since < and > will be converted to &lt; and &gt;
        // nb. this is happening now automatically for string fields in our parameters interceptor.
        fields.validateSubject(this);

        // todo: should we process the message body prior to doing this test?  is it possible someone could
        // enter UBBCode that translated to nothing in order to have an empty body?
        // bl: changed to enforce that the body contains visible html
        boolean isBodyRequired = fields.isBodyRequired();
        int minChars = isBodyRequired ? 1 : 0;
        // bl: always need to make sure that the body is less than the max allowed characters
        if(contentValidationHandler.validateString(fields.getBody(), minChars, Composition.MAX_BODY_LENGTH, fields.getBodyFieldName(), getContentType().getBodyFieldWordletKey())) {
            if (isBodyRequired && !HTMLParser.doesHtmlFragmentContainVisibleContent(fields.getBody())) {
                contentValidationHandler.addWordletizedRequiredFieldError(fields.getBodyFieldName(), getContentType().getBodyFieldWordletKey());
            }
        }

        fields.validate(this);

        // for master content, make sure that the pretty-url string isn't in use already
        networkContext().doAreaTask(portfolioToPostTo.getArea(), new AreaTaskImpl<Object>() {
            protected Object doMonitoredTask() {
                assert contentType.isNarrativePost() : "Only support Narrative posts!";
                // bl: we'll allow auto-generation of prettyUrlString up until the post is actually live for the first time.
                // future publications don't count. once a post is live, the prettyUrlString should never change again.
                if(isNew || fields.isEditOfDraft()) {
                    derivePrettyUrlString();
                }

                return null;
            }
        });
    }

    public void derivePrettyUrlString() {
        contentFields.setPrettyUrlString(getPrettyUrlStringValue(Content.dao(), content, portfolioToPostTo.getAreaRlm(), portfolioToPostTo, contentType, contentFields.getSubject()));
    }

    private static String getPrettyUrlStringPrefix(String prettyUrlText) {
        return IPHTMLUtil.getSafePrettyUrlStringFromString(prettyUrlText, true);
    }

    private static String getPrettyUrlString(String basePrettyUrlPrefix, int index) {
        if (index <= 1) {
            return basePrettyUrlPrefix.substring(0, Math.min(basePrettyUrlPrefix.length(), NetworkConstants.MAX_PRETTY_URL_STRING_LENGTH));
        }

        String suffix = newString("-", Integer.toString(index));

        return newString(basePrettyUrlPrefix.substring(0, Math.min(basePrettyUrlPrefix.length(), NetworkConstants.MAX_PRETTY_URL_STRING_LENGTH - suffix.length())), suffix);
    }

    public static String getPrettyUrlStringValue(SEOObjectDAO dao, AreaRlm areaRlm, Portfolio portfolio, ContentType contentType, String title) {
        return getPrettyUrlStringValue(dao, null, areaRlm, portfolio, contentType, title);
    }

    public static String getPrettyUrlStringValue(SEOObjectDAO dao, SEOObject object, AreaRlm areaRlm, Portfolio portfolio, ContentType contentType, String title) {
        if (portfolio == null) {
            assert contentType == null : "Should never supply ContentType when portfolio is not supplied! contentType/" + contentType;
        } else {
            assert isEqual(areaRlm, portfolio.getAreaRlm()) : "AreaRlm and Portfolio mismatch! areaRlm/" + areaRlm.getOid() + " portfolio/" + portfolio.getOid() + " portfolioAreaRlm/" + portfolio.getAreaRlm().getOid();
        }
        // bl: make sure that we enable any disabled HTML in the subject so that we don't end up with "andamp" in
        // subjects where we had an &amp;
        String prettyUrlStringPrefix = getPrettyUrlStringPrefix(HtmlTextMassager.enableDisabledHtml(title));

        // jw: abstracted this logic into a new utility method so that we can do this same logic for existing elements
        //     where we want to base the new ID off of their old ID.
        return getPrettyUrlStringValueForPrefix(dao, object, areaRlm, portfolio, contentType, prettyUrlStringPrefix);
    }

    private static String getPrettyUrlStringValueForPrefix(SEOObjectDAO dao, SEOObject object, AreaRlm areaRlm, Portfolio portfolio, ContentType contentType, String prettyUrlStringPrefix) {
        // bl: if there is no prefix, there is no point to deriving a prettyUrlString. otherwise, it will just have
        // the format "-XXX" where "XXX" is some number that will increment over time. it's pretty silly and has
        // no value. so, let's just never use empty prettyUrlStrings.
        if (isEmpty(prettyUrlStringPrefix)) {
            return null;
        }

        String prettyUrlString;
        int index = 1;
        // jw: lets support editing (for draft purposes) So we will need to store the existing object to allow multiple test.
        SEOObject existingObject;
        do {
            // the initial zero will cause the index to not be included in the prettyUrlString the first time
            prettyUrlString = getPrettyUrlString(prettyUrlStringPrefix, index);
            index++;
        }
        while (exists(existingObject = dao.getForPrettyURLString(areaRlm, portfolio, contentType, prettyUrlString)) && !isEqual(existingObject, object));

        return prettyUrlString;
    }

    public ValidationHandler getValidationHandler() {
        return validationHandler;
    }

    public void setValidationHandler(ValidationHandler validationHandler) {
        this.validationHandler = validationHandler;
    }

    private class PrepareForEdit extends AreaTaskImpl<Object> {

        public boolean isForceWritable() {
            // bl: inherit the writable flag from the global session's read only flag.
            // if the global session is read only, this must be for "input" for the rendering
            // of the create content page.  in that case, forceWritable should be false.
            // in the event that the global session is not read only, this is for the posting
            // of the content page.  in that case, forceWritable should be true.  we'll leave
            // this session open so that it can be re-used for posting the content, too.

            // bl: actually, now that we are inheriting the read-only flag automatically from the PartitionGroup
            // the isForceWritable only needs to return whether or not it explicitly, internally requires
            // itself to be run in a writable mode.  in the case of this task, it doesn't do any writes,
            // and therefore it can be run read-only, when available.
            return false;
        }

        protected Object doMonitoredTask() {
            if (content.isDeleted()) {
                throw new NetworkException(CompositionType.CONTENT.getDeletedErrorMessage());
            }
            if (content.isDisabled()) {
                throw new NetworkException(wordlet("error.contentDisabled"));
            }

            contentType = content.getContentType();
            // load the ContentConsumer for this composition as well
            contentConsumer = contentType.getInstance(composition);

            if (!exists(contentConsumer)) {
                throw UnexpectedError.getRuntimeException("Failed lookup of ContentConsumer for Content c/" + content.getOid(), true);
            }

            if(!content.isEditableByCurrentUser()) {
                throw UnexpectedError.getIgnorableRuntimeException("You can't edit this post!");
            }

            // set the contentFields for this edit.
            // composition partition should already be in scope, so just do the read directly
            {
                Class<? extends ContentFields> cls = contentType.getContentFieldsClass();
                try {
                    contentFields = cls.getConstructor(Content.class, contentType.getContentConsumerClass(), OID.class).newInstance(content, contentConsumer, fileUploadProcessOid);
                } catch (Throwable t) {
                    throw UnexpectedError.getRuntimeException("Failed instantiating ContentFields!", t, true);
                }
            }

            assert isEqual(content.getPortfolio(), portfolioToPostTo) : "Portfolio mismatch";

            return null;
        }
    }
}
