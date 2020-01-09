package org.narrative.network.core.content.base;

import org.narrative.common.persistence.Bitmask;
import org.narrative.common.persistence.ByteBitmaskType;
import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.OID;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.enums.EnumRegistry;
import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.common.util.enums.StringEnum;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.composition.base.CompositionConsumerType;
import org.narrative.network.core.fileondisk.base.FileUsageType;
import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.customizations.narrative.posts.NarrativePostContent;
import org.narrative.network.customizations.narrative.posts.services.NarrativePostFields;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import org.narrative.network.shared.security.AccessViolation;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: Dec 1, 2005
 * Time: 10:28:47 AM
 *
 * @author Brian
 */
public enum ContentType implements Bitmask<ByteBitmaskType<ContentType>>, IntegerEnum, StringEnum {
    NARRATIVE_POST(12, "NARRATIVE_POST", NarrativePostContent.class, NarrativePostFields.class)
    ;

    public static final Set<ContentType> DRAFT_TYPES;

    static {
        Set<ContentType> draft = newLinkedHashSet();
        for (ContentType contentType : values()) {
            if (contentType.isSupportsDrafts()) {
                draft.add(contentType);
            }
        }
        DRAFT_TYPES = Collections.unmodifiableSet(draft);
    }

    private final int id;
    private final String idStr;
    private final Class<? extends ContentConsumer> contentConsumerClass;
    private final Class<? extends ContentFields> contentFieldsClass;
    private final boolean allowAttachments;

    ContentType(int id, String idStr, Class<? extends ContentConsumer> daoObjectClass, Class<? extends ContentFields> contentFieldsClass) {
        this.id = id;
        this.idStr = idStr;
        this.contentConsumerClass = daoObjectClass;
        assert ClassUtils.hasConstructor(contentConsumerClass, Composition.class) : "All contentConsumerClasses must have a constructor that takes a Composition object!";
        this.contentFieldsClass = contentFieldsClass;
        // bl: changed to figure this out based on whether or not the ContentConsumer class is a ContentWithAttachmentsConsumer
        this.allowAttachments = ContentWithAttachmentsConsumer.class.isAssignableFrom(contentConsumerClass);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getIdStr() {
        return idStr;
    }

    public Class<? extends ContentConsumer> getContentConsumerClass() {
        return contentConsumerClass;
    }

    public Class<? extends ContentFields> getContentFieldsClass() {
        return contentFieldsClass;
    }

    public NetworkDAOImpl getDAO() {
        // bl: this is a little bit funky, but that's life.  since ContentConsumer doesn't/can't
        // implement DAOObject (since it doesn't have an associated DAO), we need to use an unparameterized
        // call to getDAO to get the generic DAO 
        return (NetworkDAOImpl) DAOImpl.getDAO((Class) contentConsumerClass);
    }

    public ContentConsumer getInstance(Composition composition) {
        return getInstance(composition.getOid());
    }

    public ContentConsumer getInstance(OID compositionOid) {
        // bl: a little bit ugly with a cast since enums can't be parameterized
        return (ContentConsumer) getDAO().get(compositionOid);
    }

    public <T extends ContentConsumer> T getNewInstance(Composition composition) {
        try {
            Constructor<T> constructor = (Constructor<T>) contentConsumerClass.getConstructor(Composition.class);
            return constructor.newInstance(composition);
        } catch (Throwable t) {
            throw UnexpectedError.getRuntimeException("All ContentConsumer impl must provide a constructor that takes a Composition! class/" + contentConsumerClass.getName(), t, true);
        }
    }

    public CompositionConsumerType getCompositionConsumerType() {
        return CompositionConsumerType.getCompositionConsumerTypeForContentType(this);
    }

    public boolean isAllowAttachments() {
        return allowAttachments;
    }

    public String getNameForDisplay() {
        return wordlet(getNameForDisplayWordletKey());
    }

    public String getNameForDisplayWordletKey() {
        return newString("contentType.", this);
    }

    public String getNameForDisplayLowercase() {
        return wordlet("contentType." + this + ".lowercase");
    }

    public String getBodyFieldWordletKey() {
        // everything else just uses "Body"
        return "createContent.body";
    }

    public boolean isAllowsReplies() {
        // bl: if the content supports an author, then it allows replies. it's "standard" content. all other types
        // (chats, FAQ posts, documents) are not
        return isSupportsAuthor();
    }

    public boolean isNarrativePost() {
        return this == NARRATIVE_POST;
    }

    public boolean isSupportsTitleImage() {
        return isNarrativePost();
    }

    @Override
    public long getBitmask() {
        return EnumRegistry.getBitForIntegerEnum(this);
    }

    @Override
    public ByteBitmaskType<ContentType> getBitmaskType() {
        return new ByteBitmaskType<>((byte) getBitmask());
    }

    public void checkModerateRight(AreaRole areaRole) {
        // jw: there are currently no moderator rights for Narrative Posts, so throwing Access Violation here.
        throw new AccessViolation(wordlet("contentType.cannotManageNarrativePosts"));
    }

    public boolean isSupportsDrafts() {
        return isNarrativePost();
    }

    public boolean isSupportsTrendingStats() {
        return isNarrativePost();
    }

    public FileUsageType getAttachmentFileUsageType() {
        return FileUsageType.ATTACHMENT;
    }

    public String getSubjectFieldNameForDisplayWordletKey() {
        if (isNarrativePost()) {
            return "narrativePostFields.title";
        }

        // finally lets just assume its called "Subject" since that is what is used by Forum, QShark Topics, QShark KB Entries, and User Wall Topics
        return "contentType.subjectFieldName.subject";
    }

    public String getSubjectFieldNameForDisplay() {
        return wordlet(getSubjectFieldNameForDisplayWordletKey());
    }

    public boolean isSupportsAuthor() {
        return isNarrativePost();
    }

    public boolean isSupportsQualityRatingReplies() {
        return isNarrativePost();
    }
}
