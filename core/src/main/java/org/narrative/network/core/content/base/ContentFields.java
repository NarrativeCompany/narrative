package org.narrative.network.core.content.base;

import org.narrative.common.core.services.interceptors.BypassHtmlDisable;
import org.narrative.common.persistence.OID;
import org.narrative.common.util.IPUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.posting.HtmlTextMassager;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.content.base.services.CreateContentTask;
import org.narrative.network.core.mentions.MentionsHtmlParser;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.posting.Scrubbable;
import org.narrative.network.shared.util.NetworkConstants;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.Collection;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * ContentFields represents each of the fields that can be set on content
 * in a create content or edit content request.  ContentFields can then
 * be subsequently applied to a ContentWrapper to create or update
 * the Content object as necessary.
 * <p>
 * Conceptually, you should view this class as a set of all of the "common"
 * fields that apply to a single piece of broadcasted content across multiple
 * sites.  As soon as a field in this class can change when content is broadcasted
 * across multiple sites (e.g. if isDraft could be set individually for each site),
 * then that field should be removed from this class.
 * <p>
 * All subclasses of ContentFields should have two constructors:
 * - A constructor that takes (User,OID,boolean)
 * - A constructor that takes (Content,T,OID)
 * <p>
 * The OID is the OID of the form.
 * <p>
 * Date: Jan 25, 2006
 * Time: 10:26:07 AM
 *
 * @author Brian
 */
public abstract class ContentFields<T extends ContentConsumer> implements org.narrative.common.util.posting.Formattable, Scrubbable {

    protected final boolean isNew;
    private final boolean isEditOfDraft;
    /**
     * eventDatetime is just the time that the content is being created/edited.
     */
    private Timestamp eventDatetime = new Timestamp(System.currentTimeMillis());
    private final Timestamp editDatetime;
    private Timestamp liveDatetime;

    private String subject;
    private String extract;
    private boolean isDraft;
    private String body;

    // the following fields are used to track the significance of edits
    private final String originalSubject;
    private final String originalBody;

    private String prettyUrlString;

    private String guestName;

    private Content currentContent;

    @NotNull
    public abstract ContentType getContentType();

    /**
     * use this constructor when creating new content
     */
    public ContentFields(User user, OID fileUploadProcessOid) {
        isNew = true;
        isEditOfDraft = false;
        editDatetime = null;
        liveDatetime = eventDatetime;

        this.originalSubject = null;
        this.originalBody = null;
    }

    /**
     * use this constructor when displaying content
     *
     * @param content the content to be displayed
     */
    public ContentFields(@NotNull Content content, OID fileUploadProcessOid) {
        assert IPUtil.isEqual(getContentType(), content.getContentType()) : "Content types must be equal when creating ContentFields!";
        subject = content.getSubject();
        extract = content.getExtractResolved();
        isDraft = content.isDraft();
        liveDatetime = content.getLiveDatetime();

        Composition composition = content.getComposition();
        if (!exists(composition)) {
            throw UnexpectedError.getRuntimeException("Failed lookup of Composition for valid content! c/" + content.getOid() + " part/" + content.getCompositionPartitionOid(), true);
        }
        body = composition.getBodyResolved();
        isNew = false;
        if (isDraft) {
            // don't record the edit of a draft as a true edit
            editDatetime = null;
            isEditOfDraft = true;
        } else {
            editDatetime = eventDatetime;
            isEditOfDraft = false;
        }

        this.originalSubject = content.getSubject();
        this.originalBody = body;
        this.prettyUrlString = content.getPrettyUrlString();

        this.currentContent = content;
    }

    public boolean isSupportMentions() {
        return getContentType().getCompositionConsumerType().isSupportsMentions() && networkContext().getPrimaryRole().isRegisteredUser();
    }

    public void scrub() {
        // be sure to keep the scrubbing of subject in sync with the getSubjectForEdit() method.
        subject = HtmlTextMassager.sanitizePlainTextString(subject, false);
        // be sure to keep the scrubbing of subject in sync with the getExtractForEdit() method.
        extract = HtmlTextMassager.sanitizePlainTextString(extract, false);

        // bl: no longer need to scrub the file pointer set, as that will be handled automatically
        // in CreateClips.
        /*ContentFilePointerSet primaryPictureFilePointerSet = getPrimaryPictureFilePointerSet();
        if(primaryPictureFilePointerSet!=null) {
            primaryPictureFilePointerSet.scrub();
        }*/
    }

    public boolean isCharsChanged() {
        return !isEqual(originalSubject, subject) || !isEqual(originalBody, body);
    }

    public abstract Collection<? extends FileData> getFileData();

    public void validate(CreateContentTask task) {
        Collection<? extends FileData> fileDatas = getFileData();
        if (fileDatas != null) {
            for (FileData fileData : fileDatas) {
                FileData.validateAttachment(task.getValidationHandler(), fileData);
            }
        }

        if (!networkContext().getPrimaryRole().isRegisteredUser() && isSupportsGuestAuthors()) {
            task.getValidationHandler().validateString(getGuestName(), Content.MIN_GUEST_NAME_LENGTH, Content.MAX_GUEST_NAME_LENGTH, "tags.site.page.createContentWrapper.author", "tags.site.page.createContentWrapper.yourName");
        }
    }

    public Timestamp getEventDatetime() {
        return eventDatetime;
    }

    public void doSetEventDatetime(Timestamp eventDatetime) {
        this.eventDatetime = eventDatetime;
    }

    public Timestamp getEditDatetime() {
        return editDatetime;
    }

    public Timestamp getLiveDatetime() {
        return liveDatetime;
    }

    public void doSetLiveDatetime(Timestamp liveDatetime) {
        this.liveDatetime = liveDatetime;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getPrettyUrlString() {
        return prettyUrlString;
    }

    @BypassHtmlDisable
    public void setPrettyUrlString(String prettyUrlString) {
        this.prettyUrlString = prettyUrlString;
    }

    protected Content getCurrentContent() {
        return currentContent;
    }

    public String getExtract() {
        return extract;
    }

    public void setExtract(String extract) {
        this.extract = extract;
    }

    public boolean isDraft() {
        return getContentType().isSupportsDrafts() && isDraft;
    }

    public void setDraft(boolean draft) {
        // only new content and draft content can be marked as a draft
        if (!isNew && !isEditOfDraft) {
            // can't change an existing live piece of content into a draft.
            return;
        }
        isDraft = draft;
    }

    public boolean isEditOfDraft() {
        return isEditOfDraft;
    }

    public String getBody() {
        return body;
    }

    @BypassHtmlDisable
    public void setBody(String body) {
        this.body = body;
    }

    public String getBodyForDatabase() {
        if (isSupportMentions()) {
            return MentionsHtmlParser.escapeMentions(getBody());
        }

        return getBody();
    }

    public abstract boolean isBodyRequired();

    public String getSubjectFieldName() {
        return "contentFields.subject";
    }

    public String getBodyFieldName() {
        return "contentFields.body";
    }

    public int getMinSubjectLength() {
        return NetworkConstants.MIN_SUBJECT_LENGTH;
    }

    public int getMaxSubjectLength() {
        return NetworkConstants.MAX_SUBJECT_LENGTH;
    }

    public void validateSubject(CreateContentTask task) {
        task.getValidationHandler().validateString(getSubject(), getMinSubjectLength(), getMaxSubjectLength(), getSubjectFieldName(), getContentType().getSubjectFieldNameForDisplayWordletKey());
    }

    public boolean isSupportsGuestAuthors() {
        return false;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

}
