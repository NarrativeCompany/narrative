package org.narrative.network.core.composition.base;

import org.narrative.common.util.InvalidParamError;
import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.common.util.enums.StringEnum;
import org.narrative.network.core.content.base.ContentType;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.customizations.narrative.niches.referendum.services.HandleDeletedReferendumCommentTask;
import org.narrative.network.customizations.narrative.niches.referendum.services.ReferendumCommentInstantEmailJob;
import org.narrative.network.customizations.narrative.service.impl.comment.CommentTaskBase;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 1/3/14
 * Time: 11:05 AM
 *
 * @author brian
 */
public enum CompositionConsumerType implements StringEnum, IntegerEnum {
    REFERENDUM(15, "REFERENDUM", "referendums", "referendum", CompositionType.REFERENDUM) {
        @Override
        public void scheduleNewReplyEmailJob(CompositionConsumer consumer, Reply reply) {
            assert exists(consumer) && consumer.getCompositionConsumerType()==this : "Should always have a consumer of appropriate type here!";

            ReferendumCommentInstantEmailJob.schedule((Referendum) consumer, reply);
        }
        @Override
        public CommentTaskBase<Object> getDeleteCommentHandlerTask(CompositionConsumer consumer, Reply reply) {
            assert exists(consumer) && consumer.getCompositionConsumerType()==this : "Should always have a consumer of appropriate type here!";

            return new HandleDeletedReferendumCommentTask((Referendum) consumer, reply);
        }
    },
    NARRATIVE_POST(16, "NARRATIVE_POST", "posts", "narrativepost", ContentType.NARRATIVE_POST)
    ;

    public static final String TYPE = "org.narrative.network.core.composition.base.CompositionConsumerType";

    private final int id;
    private final String idStr;
    private final String idForRestApi;
    private final String emailAddressPrefix;
    private final CompositionType compositionType;
    private final ContentType contentType;

    CompositionConsumerType(int id, String idStr, String idForRestApi, String emailAddressPrefix, ContentType contentType) {
        this.id = id;
        this.idStr = idStr;
        this.idForRestApi = idForRestApi;
        this.emailAddressPrefix = emailAddressPrefix;
        assert contentType != null : "Must always supply ContentType!";
        this.compositionType = CompositionType.CONTENT;
        this.contentType = contentType;
    }

    CompositionConsumerType(int id, String idStr, String idForRestApi, String emailAddressPrefix, CompositionType compositionType) {
        this.id = id;
        this.idStr = idStr;
        this.idForRestApi = idForRestApi;
        this.emailAddressPrefix = emailAddressPrefix;
        assert compositionType != null : "Must always supply CompositionType!";
        assert !compositionType.isContent() : "Should use the ContentType constructor for content!";
        this.compositionType = compositionType;
        this.contentType = null;
    }

    private static final Map<ContentType, CompositionConsumerType> CONTENT_TYPE_TO_CONSUMER_TYPE;
    private static final Map<String, CompositionConsumerType> BY_ID_FOR_REST_API;

    static {
        Map<ContentType, CompositionConsumerType> contentTypeToCompositionConsumerType = new LinkedHashMap<>();
        Map<String, CompositionConsumerType> byIdForRestApi = new LinkedHashMap<>();
        for (CompositionConsumerType compositionConsumerType : values()) {
            if (compositionConsumerType.compositionType.isContent()) {
                contentTypeToCompositionConsumerType.put(compositionConsumerType.contentType, compositionConsumerType);
            }
            if (!isEmpty(compositionConsumerType.getIdForRestApi())) {
                assert !byIdForRestApi.containsKey(compositionConsumerType.getIdForRestApi()) : "Should never use the same id for the rest api twice!";
                byIdForRestApi.put(compositionConsumerType.getIdForRestApi(), compositionConsumerType);
            }
        }
        CONTENT_TYPE_TO_CONSUMER_TYPE = Collections.unmodifiableMap(contentTypeToCompositionConsumerType);
        BY_ID_FOR_REST_API = Collections.unmodifiableMap(byIdForRestApi);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getIdStr() {
        return idStr;
    }

    public String getIdForRestApi() {
        return idForRestApi;
    }

    public CompositionType getCompositionType() {
        return compositionType;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public String getNameForDisplay() {
        if (getCompositionType().isContent()) {
            return getContentType().getNameForDisplay();
        }

        return getCompositionType().getNameForDisplay();
    }

    public boolean isReferendum() {
        return this == REFERENDUM;
    }

    public boolean isNarrativePost() {
        return this == NARRATIVE_POST;
    }

    public boolean isSupportsAuthor() {
        return getContentType() != null && getContentType().isSupportsAuthor();
    }

    public static CompositionConsumerType getCompositionConsumerTypeForContentType(ContentType contentType) {
        assert contentType != null : "Should always supply ContentType!";
        return CONTENT_TYPE_TO_CONSUMER_TYPE.get(contentType);
    }

    public boolean isSupportsMentions() {
        return true;
    }

    public void scheduleNewReplyEmailJob(CompositionConsumer consumer, Reply reply) {
        // jw: not all types have new reply emails. Override this to schedule email job for types that do.
    }

    public CommentTaskBase<Object> getDeleteCommentHandlerTask(CompositionConsumer consumer, Reply reply) {
        // jw: not all types have type specific data to cleanup when deleting a reply, so let's default to nothing.
        return null;
    }

    public static CompositionConsumerType getForRestApi(String idForRestApi, String paramName) {
        assert !isEmpty(idForRestApi) : "Should always have a id by this point. Never call this without one!";

        CompositionConsumerType consumerType = BY_ID_FOR_REST_API.get(idForRestApi);
        if (consumerType==null) {
            throw new InvalidParamError(paramName, idForRestApi);
        }

        return consumerType;
    }
}
