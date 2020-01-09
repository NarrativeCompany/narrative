package org.narrative.network.customizations.narrative.service.impl.comment;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.posting.MessageTextMassager;
import org.narrative.network.core.composition.base.CompositionConsumer;
import org.narrative.network.core.composition.base.CompositionConsumerType;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.core.content.base.services.GetCompositionConsumerRepliesTask;
import org.narrative.network.core.mentions.MentionsUtil;
import org.narrative.network.core.rating.QualityRating;
import org.narrative.network.core.rating.RatingService;
import org.narrative.network.core.user.UserActivityRateLimit;
import org.narrative.network.core.user.services.activityrate.CheckUserActivityRateLimitTask;
import org.narrative.network.customizations.narrative.controller.CommentController;
import org.narrative.network.customizations.narrative.controller.result.ScalarResultDTO;
import org.narrative.network.customizations.narrative.permissions.NarrativePermissionType;
import org.narrative.network.customizations.narrative.service.api.AreaTaskExecutor;
import org.narrative.network.customizations.narrative.service.api.CommentService;
import org.narrative.network.customizations.narrative.service.api.model.CommentDTO;
import org.narrative.network.customizations.narrative.service.api.model.CommentsDTO;
import org.narrative.network.customizations.narrative.service.api.model.input.CommentInput;
import org.narrative.network.customizations.narrative.service.impl.common.PageUtil;
import org.narrative.network.customizations.narrative.service.mapper.ReplyMapper;
import org.narrative.network.shared.replies.services.ReplySortOrder;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.CompositionTaskImpl;
import org.narrative.shared.event.reputation.RatingType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-02-13
 * Time: 12:31
 *
 * @author jonmark
 */
@Service
@Transactional
public class CommentServiceImpl implements CommentService {
    private final AreaTaskExecutor areaTaskExecutor;
    private final ReplyMapper replyMapper;
    private final RatingService ratingService;

    public CommentServiceImpl(
            AreaTaskExecutor areaTaskExecutor,
            ReplyMapper replyMapper,
            RatingService ratingService
    ) {
        this.areaTaskExecutor = areaTaskExecutor;
        this.replyMapper = replyMapper;
        this.ratingService = ratingService;
    }

    @Override
    public CommentsDTO getComments(String consumerTypeId, OID consumerOid, Pageable pageRequest, boolean includeBuried, OID commentOid) {
        // jw: this method will get a value, and throw an InvalidParamError if one is not found.
        CompositionConsumerType consumerType = CompositionConsumerType.getForRestApi(consumerTypeId, CommentController.CONSUMER_TYPE_ID_PARAM);

        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<CommentsDTO>(false) {
            @Override
            protected CommentsDTO doMonitoredTask() {
                // jw: get and validate the consumer
                CompositionConsumer consumer = (CompositionConsumer) consumerType.getCompositionType().getDAO().get(consumerOid);
                assert consumer.isHasComposition() : "At this time, all supported consumers always have a composition!";

                // jw: let's setup the task to fetch the replies
                GetCompositionConsumerRepliesTask repliesTask = new GetCompositionConsumerRepliesTask(consumer, ReplySortOrder.NEWEST_TO_OLDEST, commentOid==null ? null : commentOid.toString(), pageRequest.getPageSize(), pageRequest.getPageNumber() + 1);
                repliesTask.setPrimeUserRatedReplies(true);
                if(consumer.isSupportsQualityRatingReplies()) {
                    repliesTask.setExcludeBuried(!includeBuried);
                }

                // jw: finally, let's fetch the replies
                getAreaContext().doCompositionTask(consumer.getCompositionPartition(), repliesTask);

                CommentsDTO.CommentsDTOBuilder builder = CommentsDTO.builder();

                Pageable pageable = pageRequest;

                if(consumer.isSupportsQualityRatingReplies()) {
                    builder
                            .buriedCommentCount(repliesTask.getAnyQualityReplyCount() - repliesTask.getAtLeastMediumQualityReplyCount())
                            .includeBuried(!repliesTask.isExcludeBuried());

                    // bl: if this was a comment permalink, the page number might have changed. in that case,
                    // we need to set up a new commentsPage. note that our internal values are 1-based indexes,
                    // whereas Pageable uses a 0-based index, so we have to translate.
                    if((repliesTask.getPage() - 1) != pageable.getPageNumber()) {
                        pageable = PageRequest.of(repliesTask.getPage() - 1, pageable.getPageSize());
                    }
                }

                Page<CommentDTO> commentsPage = PageUtil.buildPageImpl(
                        replyMapper.mapReplyListToCommentDTOList(repliesTask.getReplies()),
                        pageable,
                        repliesTask.getPostCountForPagination()
                );

                return builder
                        .page(commentsPage)
                        .build();
            }
        });
    }

    @Override
    public CommentDTO postComment(String consumerTypeId, OID consumerOid, CommentInput input) {
        return doCommentTask(consumerTypeId, consumerOid, null, new CommentTask<CommentDTO>() {
            @Override
            protected CommentDTO doMonitoredTask() {
                Reply reply = getAreaContext().doAreaTask(new CreateEditCommentTask(getConsumer(), input.getBody(), null));
                return getCommentDTO(reply);
            }
        });
    }

    @Override
    public CommentDTO editComment(String consumerTypeId, OID consumerOid, OID commentOid, CommentInput input) {
        return doCommentTask(consumerTypeId, consumerOid, commentOid, new CommentTask<CommentDTO>() {
            @Override
            protected CommentDTO doMonitoredTask() {
                Reply reply = getAreaContext().doAreaTask(new CreateEditCommentTask(getConsumer(), input.getBody(), getReply()));
                return getCommentDTO(reply);
            }
        });
    }

    @Override
    public ScalarResultDTO<String> getCommentForEdit(String consumerTypeId, OID consumerOid, OID commentOid) {
        return doCommentTask(consumerTypeId, consumerOid, commentOid, new CommentTask<ScalarResultDTO<String>>(false) {
            @Override
            protected ScalarResultDTO<String> doMonitoredTask() {
                Reply reply  = Reply.dao().getForApiParam(commentOid, CommentController.COMMENT_OID_PARAM);
                reply.checkEditableByCurrentUser();
                getAreaContext().getAreaRole().checkNarrativeRight(NarrativePermissionType.POST_COMMENTS);
                String body = MentionsUtil.getPlainTextMentions(reply.getBody());
                body = MessageTextMassager.getUnmassagedTextForBasicTextareaEdit(body, true);
                return ScalarResultDTO.<String>builder().value(body).build();
            }
        });
    }

    @Override
    public void deleteComment(String consumerTypeId, OID consumerOid, OID commentOid) {
        doCommentTask(consumerTypeId, consumerOid, commentOid, new CommentTask<Object>() {
            @Override
            protected Object doMonitoredTask() {
                if(!getReply().isDeletableByCurrentUser()) {
                    throw UnexpectedError.getRuntimeException("Should never attempt to delete a reply by another user!");
                }

                getAreaContext().doAreaTask(new DeleteCommentTask(getConsumer(), getReply()));

                return null;
            }
        });
    }

    @Override
    public CommentDTO qualityRateComment(String consumerTypeId, OID consumerOid, OID commentOid, QualityRating rating, String reason) {
        return doCommentTask(consumerTypeId, consumerOid, commentOid, new CommentTask<CommentDTO>() {
            @Override
            protected CommentDTO doMonitoredTask() {
                // jw: ensure that this user has the right to rate content
                getAreaContext().getAreaRole().checkNarrativeRight(NarrativePermissionType.RATE_CONTENT);

                // jw: ensure that the user is not trying to rate their own reply
                if(exists(getReply().getAuthor()) && getReply().getAuthor().isCurrentUserThisUser()) {
                    throw UnexpectedError.getRuntimeException("Can't rate your own comments!");
                }
                // jw: ensure that the consumer supporty quality rated replies.
                if(!getConsumer().isSupportsQualityRatingReplies()) {
                    throw UnexpectedError.getRuntimeException("Don't support quality rating comments on this CompositionConsumer!");
                }

                // jw: now, let's esure that they have not hit their rating limit for comments.
                getAreaContext().doAreaTask(new CheckUserActivityRateLimitTask(getAreaContext().getUser(), UserActivityRateLimit.RATE_COMMENT));

                // jw: finally, record the quality rating.
                ratingService.setRating(getReply(), getNetworkContext().getUser(), RatingType.QUALITY, rating, reason);

                // jw: be sure to cache this rating on the reply, there is no need to go to the DB for it.
                getReply().setQualityRatingByCurrentUser(rating);

                return replyMapper.mapReplyToCommentDTO(getReply());
            }
        });
    }

    private <T> T doCommentTask(String consumerTypeId, OID consumerOid, OID commentOid, CommentTask<T> task) {
        // jw: this method will get a value, and throw an InvalidParamError if one is not found.
        CompositionConsumerType consumerType = CompositionConsumerType.getForRestApi(consumerTypeId, CommentController.CONSUMER_TYPE_ID_PARAM);

        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<T>(task.isForceWritable()) {
            @Override
            protected T doMonitoredTask() {
                CompositionConsumer consumer = (CompositionConsumer) consumerType.getCompositionType().getDAO().get(consumerOid);
                assert consumer.isHasComposition() : "At this time, all supported consumers always have a composition!";

                return getAreaContext().doCompositionTask(consumer.getCompositionPartition(), new CompositionTaskImpl<T>(false) {
                    @Override
                    protected T doMonitoredTask() {
                        task.setConsumer(consumer);
                        if(commentOid!=null) {
                            Reply reply  = Reply.dao().getForApiParam(commentOid, CommentController.COMMENT_OID_PARAM);
                            task.setReply(reply);
                        }
                        return getAreaContext().doAreaTask(task);
                    }
                });
            }
        });
    }

    private static abstract class CommentTask<T> extends AreaTaskImpl<T> {
        private CompositionConsumer consumer;
        private Reply reply;

        CommentTask() {}

        CommentTask(boolean forceWritable) {
            super(forceWritable);
        }

        public CompositionConsumer getConsumer() {
            return consumer;
        }

        public void setConsumer(CompositionConsumer consumer) {
            this.consumer = consumer;
        }

        public Reply getReply() {
            return reply;
        }

        public void setReply(Reply reply) {
            this.reply = reply;
        }
    }

    private CommentDTO getCommentDTO(Reply reply) {
        return replyMapper.mapReplyToCommentDTO(reply);
    }
}
