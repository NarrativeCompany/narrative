import * as React from 'react';
import { branch, compose, withHandlers } from 'recompose';
import {
  withQualityRatingController, WithQualityRatingControllerHandlers,
  WithQualityRatingControllerParentHandlers,
  WithQualityRatingControllerProps
} from '../../../shared/containers/withQualityRatingController';
import { RatingMessages } from '../../../shared/i18n/RatingMessages';
import { QualityRating, withQualityRatePost, WithQualityRatePostProps } from '@narrative/shared';
import { PermissionErrorModal } from '../../../shared/components/PermissionErrorModal';
import {
  DownVoteQualityRatingSelectorModal
} from '../../../shared/components/rating/DownVoteQualityRatingSelectorModal';
import { AuthorRateContentWarning } from '../../../shared/components/rating/AuthorRateContentWarning';
import { QualityRateObject } from '../../../shared/components/rating/QualityRateObject';
import { PostActionProps } from './PostActions';

type Props =
  PostActionProps &
  WithQualityRatingControllerProps;

const PostQualityRatingComponent: React.SFC<Props> = (props) => {
  const { post, postDetail, handleQualityRating, handleOpenDownVoteSelector,
    permissionErrorModalProps, downVoteQualityRatingSelectorProps, authorRateWarningProps } = props;

  const score = post.qualityRatingFields.score;

  return (
    <React.Fragment>
      <QualityRateObject
        objectOid={post.oid}
        authorOid={post.author.oid}
        fields={post.qualityRatingFields}
        current={postDetail.qualityRatingByCurrentUser}
        handleOpenDownVoteSelector={handleOpenDownVoteSelector}
        handleQualityRating={handleQualityRating}
      />
      {permissionErrorModalProps && <PermissionErrorModal {...permissionErrorModalProps}/>}
      {authorRateWarningProps && <AuthorRateContentWarning {...authorRateWarningProps}/>}
      {downVoteQualityRatingSelectorProps &&
        <DownVoteQualityRatingSelectorModal {...downVoteQualityRatingSelectorProps} />
      }
      <meta itemProp="worstRating" content="0" />
      {score ? <meta itemProp="ratingValue" content={String(score)}/> : undefined}
      <meta itemProp="bestRating" content="100" />
    </React.Fragment>
  );
};

export const PostQualityRating = compose(
  // bl: if ratings are disabled, then we don't need the mutation/handler/controller
  branch((props: PostActionProps) => !!props.showRatingsDisabledModal,
    // bl: if ratings are disabled, then just include a handler with a popup error
    withHandlers<PostActionProps, WithQualityRatingControllerHandlers>({
      handleOpenDownVoteSelector: (props: PostActionProps) =>
        (_ratedObjectOid: string, _authorOid: string, _currentRating?: QualityRating) =>
      {
        (props.showRatingsDisabledModal as () => void)();
      },
      handleQualityRating: (props: PostActionProps) =>
        (_ratedObjectOid: string, _authorOid: string, _rating?: QualityRating, _reason?: string) =>
      {
        (props.showRatingsDisabledModal as () => void)();
      }
    }),
    // bl: if ratings are enabled, then compose our normal rating HOC stack
    compose(
      withQualityRatePost,
      withHandlers<WithQualityRatePostProps, WithQualityRatingControllerParentHandlers>({
        handleSubmitQualityRating: (props: WithQualityRatePostProps) =>
          async (ratedObjectOid: string, rating?: QualityRating, reason?: string) =>
        {
          const { qualityRatePost } = props;

          await qualityRatePost({rating, reason}, ratedObjectOid);
        }
      }),
      withQualityRatingController(RatingMessages.RatePostsAction)
    )
  )
)(PostQualityRatingComponent) as React.ComponentClass<PostActionProps>;
