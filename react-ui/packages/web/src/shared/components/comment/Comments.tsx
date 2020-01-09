import * as React from 'react';
import { List, Pagination } from 'antd';
import { PaginationConfig } from 'antd/lib/pagination';
import {
  Comment,
  PageInfo,
  QualityRating,
  withQualityRateComment,
  WithQualityRateCommentProps
} from '@narrative/shared';
import { FormattedMessage } from 'react-intl';
import { LocalizedNumber } from '../LocalizedNumber';
import { Heading } from '../Heading';
import { DisplayComment } from './DisplayComment';
import { Paragraph } from '../Paragraph';
import { FlexContainer } from '../../styled/shared/containers';
import { CommentMessages } from '../../i18n/CommentMessages';
import { WithPaginationCurrentPageProps } from '../../containers/withPaginationController';
import styled from '../../styled';
import { CommentsSectionProps, WithDisableCommentRatingProps } from './CommentsSection';
import * as H from 'history';
import { branch, compose, withHandlers } from 'recompose';
import {
  withQualityRatingController,
  WithQualityRatingControllerParentHandlers, WithQualityRatingControllerProps
} from '../../containers/withQualityRatingController';
import { RatingMessages } from '../../i18n/RatingMessages';
import { AuthorRateContentWarning } from '../rating/AuthorRateContentWarning';
import { PermissionErrorModal } from '../PermissionErrorModal';
import { DownVoteQualityRatingSelectorModal } from '../rating/DownVoteQualityRatingSelectorModal';
import {
  WithOpenDeleteForAupViolationConfirmationHandler
} from '../../containers/withDeleteForAupViolationController';
import { generateSkeletonListProps, renderSkeleton } from '../../utils/loadingUtils';
import { UrlFragmentAnchor } from '../UrlFragmentAnchor';
import { WithCurrentUserCommentDeleteProps } from './CommentAuthorControls';

export const commentsContainerAnchor = 'comments-container';

const CommentsContainer = styled.div`
  .comment-container:nth-child(even) {
    background-color: #F9FAFB;
  }
`;

const PaginationWrapper = styled(FlexContainer)`
  margin-top: 20px;
`;

type ParentProps =
  CommentsSectionProps &
  WithDisableCommentRatingProps &
  WithOpenDeleteForAupViolationConfirmationHandler &
  WithPaginationCurrentPageProps &
  WithCurrentUserCommentDeleteProps & {
  toDetails: H.LocationDescriptor;
  includeHeader: boolean;
  pagination: PaginationConfig;
  pageInfo: PageInfo;
  comments: Comment[];
  loading: boolean;
  disableComments: boolean;
};

type Props =
  ParentProps &
  WithQualityRatingControllerProps;

const CommentsComponent: React.SFC<Props> = (props) => {
  const {
    consumerType,
    consumerOid,
    canCommentOrRate,
    disableCommentRating,
    loading,
    disableComments,
    comments,
    pageInfo,
    currentPage,
    includeBuried,
    pagination,
    openDeleteForAupViolationConfirmation,
    toDetails,
    currentUserCanDeleteAllComments
  } = props;

  let { includeHeader } = props;

  let contentHtml;
  let commentCount;
  if (disableComments) {
    // bl: if comments are disabled, then just output a message indicating such
    contentHtml = (
      <Paragraph size="large" color="light">
        <FormattedMessage {...CommentMessages.CommentsAppearOnceApproved} />
      </Paragraph>
    );
    // bl: don't include the header in this case
    includeHeader = false;
  } else if (loading) {
    // jw: if we are loading, then lets render with the header and a loading card.
    contentHtml = <List {...generateSkeletonListProps(10, renderSkeleton)}/>;

    // jw: if there are no comments, let keep that nice and simple as well.
  } else if (!pageInfo || !pageInfo.totalElements) {
    contentHtml = (
      <Paragraph size="large" color="light">
        <FormattedMessage {...CommentMessages.CurrentlyNoComments} />
      </Paragraph>
    );

    // jw: if we have comments, let's set everything up accordingly
  } else {
    const { handleQualityRating, handleOpenDownVoteSelector } = props;
    const { authorRateWarningProps, permissionErrorModalProps, downVoteQualityRatingSelectorProps } = props;

    // jw: let's format the count and render the list for this page.
    commentCount = <LocalizedNumber value={pageInfo.totalElements} />;
    contentHtml = (
      <React.Fragment>
        {authorRateWarningProps && (
          <AuthorRateContentWarning {...authorRateWarningProps} forComment={true} />
        )}
        {permissionErrorModalProps && <PermissionErrorModal {...permissionErrorModalProps}/>}
        {downVoteQualityRatingSelectorProps &&
        <DownVoteQualityRatingSelectorModal {...downVoteQualityRatingSelectorProps}/>
        }
        <CommentsContainer>
          {comments.map((comment: Comment) =>
            <DisplayComment
              key={comment.oid}
              consumerType={consumerType}
              consumerOid={consumerOid}
              canCommentOrRate={canCommentOrRate}
              includeBuried={includeBuried}
              toDetails={toDetails}
              comment={comment}
              currentPage={currentPage}
              handleQualityRating={handleQualityRating}
              handleOpenDownVoteSelector={handleOpenDownVoteSelector}
              disableCommentRating={disableCommentRating}
              openDeleteForAupViolationConfirmation={openDeleteForAupViolationConfirmation}
              currentUserCanDeleteAllComments={currentUserCanDeleteAllComments}
            />
          )}

          <PaginationWrapper centerAll={true}>
            <Pagination {...pagination} />
          </PaginationWrapper>
        </CommentsContainer>
      </React.Fragment>
    );
  }

  // jw: if they cannot comment or rate, then do not include the title. There will not be a posting form
  //     so the section header will suffice.
  // jw: for now, let's include the header if we have a count, since that is the only way to know how many comments
  //     there are. Lame, but we can revisit that later.
  if (!includeHeader && !commentCount)  {
    return contentHtml;
  }

  const title = commentCount
    ? (
      <UrlFragmentAnchor id={commentsContainerAnchor}>
        <FormattedMessage {...CommentMessages.CommentsHeading} values={{commentCount}} />
      </UrlFragmentAnchor>
    )
    : <FormattedMessage {...CommentMessages.Comments} />;

  return (
    <React.Fragment>
      <Heading size={6} uppercase={true}>{title}</Heading>
      {contentHtml}
    </React.Fragment>
  );
};

export const Comments = compose(
  // bl: only load the rating HOC stack if comments aren't disabled.
  branch((props: ParentProps) => !props.disableComments,
    compose(
      withQualityRateComment,
      withHandlers<WithQualityRateCommentProps & ParentProps, WithQualityRatingControllerParentHandlers>({
        handleSubmitQualityRating: (props: WithQualityRateCommentProps & ParentProps) =>
          async (commentOid: string, rating?: QualityRating, reason?: string) =>
          {
            const { qualityRateComment, consumerType, consumerOid, } = props;

            await qualityRateComment({rating, reason}, {consumerType, consumerOid, commentOid});
          }
      }),
      withQualityRatingController(RatingMessages.RateCommentsAction)
    )
  ),
)(CommentsComponent) as React.ComponentClass<ParentProps>;
