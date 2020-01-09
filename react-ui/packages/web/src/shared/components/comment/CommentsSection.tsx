import * as React from 'react';
import { RouteComponentProps, withRouter } from 'react-router';
import { branch, compose, withHandlers, withProps } from 'recompose';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { CommentMessages } from '../../i18n/CommentMessages';
import { Comments, commentsContainerAnchor } from './Comments';
import * as H from 'history';
import { CommentingForm } from './CommentingForm';
import { openNotification } from '../../utils/notificationsUtil';
import { SectionHeader } from '../SectionHeader';
import {
  WithPaginationControllerProps,
  withQueryParamPaginationController
} from '../../containers/withPaginationController';
import {
  getQueryArg,
  withComments,
  WithCommentsProps,
  COMMENT_PAGE_SIZE,
  WithCompositionConsumerFields,
  WithDeleteCommentProps,
  withDeleteComment
} from '@narrative/shared';
import { withExtractedCurrentUser, WithExtractedCurrentUserProps } from '../../containers/withExtractedCurrentUser';
import { getRevokeReasonProps, getSignInMessage } from '../../utils/revokeReasonMessagesUtil';
import { RevokeReasonMessages } from '../../i18n/RevokeReasonMessages';
import { isPermissionGranted, PermissionType } from '../../containers/withPermissionsModalController';
import { Paragraph } from '../Paragraph';
import { ModalConnect, ModalName, ModalStoreProps } from '../../stores/ModalStore';
import { SharedComponentMessages } from '../../i18n/SharedComponentMessages';
import { SwitchComponent } from '../Switch';
import { createUrl } from '../../utils/routeUtils';
import { FlexContainer } from '../../styled/shared/containers';
import { Label } from '../../../routes/MemberCP/settingsStyles';
import {
  withDeleteForAupViolationController,
  WithDeleteForAupViolationControllerParentHandlers, WithDeleteForAupViolationControllerProps,
} from '../../containers/withDeleteForAupViolationController';
import { DeleteForAupViolationConfirmation } from '../tribunal/DeleteForAupViolationConfirmation';
import { commentParam } from './CommentLink';
import { UrlFragmentAnchor } from '../UrlFragmentAnchor';
import { scrollToElement } from '../../utils/scrollUtils';
import { WithCurrentUserCommentDeleteProps } from './CommentAuthorControls';

const commentsPageParam = 'commentsPage';
// bl: commentsAnchor is the anchor above the entire CommentsSection (including the comment form)
// not to be confused with the commentsContainerAnchor defined in Comments.tsx, which is located
// at the Comments heading displayed below the comment form and above the actual comments
// we use this #comments anchor for external links anchored to the Comments section.
export const commentsAnchor = 'comments';

export interface WithNewCommentHandler {
  handleNewComment: () => void;
}
export interface WithBuriedCommentHandler {
  handleBuriedCommentSwitchChange: (checked: boolean) => void;
}

export interface WithDisableCommentRatingProps {
  disableCommentRating?: boolean;
}

interface ParentProps extends
  WithCompositionConsumerFields,
  WithDisableCommentRatingProps,
  WithCurrentUserCommentDeleteProps {
  // jw: if not specified than this will be assumed to be true
  typeSpecificPostingPermission?: PermissionType;
  toDetails: H.LocationDescriptor;
  allowNewComments: boolean;
  disableComments?: boolean;
}

// jw: these props will be needed throughout this stack of Components
export interface CommentsSectionProps extends WithCompositionConsumerFields {
  canCommentOrRate: boolean;
  includeBuried?: boolean;
}

type Props =
  ParentProps &
  WithNewCommentHandler &
  WithBuriedCommentHandler &
  WithExtractedCurrentUserProps &
  WithPaginationControllerProps &
  WithDeleteForAupViolationControllerProps &
  WithCommentsProps &
  CommentsSectionProps &
  ModalStoreProps &
  RouteComponentProps<{}>;

const CommentsSectionComponent: React.SFC<Props> = (props) => {

  const {
    toDetails,
    handleNewComment,
    handleBuriedCommentSwitchChange,
    buriedCommentCount,
    pagination,
    currentPage,
    includeBuried,
    pageInfo,
    comments,
    loading,
    consumerType,
    consumerOid,
    currentUserGlobalPermissions,
    canCommentOrRate,
    currentUser,
    typeSpecificPostingPermission,
    modalStoreActions,
    allowNewComments,
    disableComments,
    disableCommentRating,
    openDeleteForAupViolationConfirmation,
    deleteForAupViolationConfirmationProps,
    currentUserCanDeleteAllComments
  } = props;

  let headerMessage;

  // jw: if comments are disabled then let's give a message appropriate to that.
  if (!allowNewComments) {
    headerMessage = comments.length > 0
      ? <FormattedMessage {...CommentMessages.CommentsNoLongerAllowed}/>
      : <FormattedMessage {...CommentMessages.CommentsNotAllowed}/>;

    // jw: if the user is signed in, and does not have the postComments permission then let's show them
    // the revoke reason.
  } else if (
    !canCommentOrRate &&
    currentUser &&
    currentUserGlobalPermissions &&
    currentUserGlobalPermissions.postComments &&
    // jw: we only want to include a permission error if there was no other permission as a factor for this
    !typeSpecificPostingPermission
  ) {
    headerMessage = getRevokeReasonProps(
      RevokeReasonMessages.PostComments,
      currentUserGlobalPermissions.postComments,
      currentUser
    ).errorMessage;

    // jw: let's give a login prompt to guests, prompting them to login in comment.
  } else if (!currentUser) {
    headerMessage = getSignInMessage(RevokeReasonMessages.PostComments, modalStoreActions);
  }

  const sharedFields = { consumerType, consumerOid, canCommentOrRate, includeBuried };

  let buriedCommentSwitch = null;
  // zb: show the switch if comment rating is allowed and we have some buried comments
  if (!disableCommentRating && buriedCommentCount) {
    buriedCommentSwitch = (
      <FlexContainer>
        <SwitchComponent
          checked={includeBuried}
          loading={loading}
          onChange={handleBuriedCommentSwitchChange}
        />
        <Label
          size={6}
          style={{cursor: 'pointer'}}
          onClick={() => {
            if (!loading) {
              handleBuriedCommentSwitchChange(!includeBuried);
            }
          }}
        >
          <FormattedMessage
            {...SharedComponentMessages.ShowBuriedCommentsWithValue}
            values={{buriedCommentCount}}
          />
        </Label>
      </FlexContainer>
    );
  }

  return (
    <React.Fragment>
      <UrlFragmentAnchor id={commentsAnchor}>
        <SectionHeader
          title={<FormattedMessage {...CommentMessages.Comments} />}
          extra={buriedCommentSwitch}
        />
      </UrlFragmentAnchor>

      {headerMessage && <Paragraph marginBottom="large">{headerMessage}</Paragraph>}

      {/* jw: Let's provide a mechanism for users to comment */}
      {canCommentOrRate && allowNewComments && !disableComments &&
      <CommentingForm
        {...sharedFields}
        handleNewComment={handleNewComment}
      />}

      {/* jw: display any comments that have already been posted */}
      <Comments
        {...sharedFields}
        toDetails={toDetails}
        includeHeader={canCommentOrRate}
        pagination={pagination}
        currentPage={currentPage}
        pageInfo={pageInfo}
        comments={comments}
        loading={loading}
        disableComments={!!disableComments}
        disableCommentRating={disableCommentRating}
        openDeleteForAupViolationConfirmation={openDeleteForAupViolationConfirmation}
        currentUserCanDeleteAllComments={currentUserCanDeleteAllComments}
      />

      {/* jw: include the option to delete comments for AUP violation if given props */}
      {deleteForAupViolationConfirmationProps &&
        <DeleteForAupViolationConfirmation
          {...deleteForAupViolationConfirmationProps}
          deleteButtonMessage={CommentMessages.DeleteComment}
        />
      }
    </React.Fragment>
  );
};

type AupDeletionHandlerProps =
  WithDeleteCommentProps &
  ParentProps;

export const CommentsSection = compose(
  withRouter,
  // jw: let's resolve the currentPage
  withProps((props: RouteComponentProps<{}> & WithDisableCommentRatingProps) => {
    const { location: { search }, disableCommentRating } = props;

    const commentOid = getQueryArg(search, commentParam);
    const includeBuriedQueryString = getQueryArg(search, 'includeBuried');

    let includeBuriedComments = false;
    if (!disableCommentRating) {
      includeBuriedComments = (includeBuriedQueryString === 'true');
    }

    return { commentOid, includeBuried: includeBuriedComments };
  }),
  branch((props: ParentProps) => !props.disableComments,
    // bl: if comments are disabled (e.g. because the post is not live), then there's no need to load this
    // HOC stack since we don't want to load the comments from the server.
    withQueryParamPaginationController<WithCommentsProps>(
      withComments,
      // tslint:disable-next-line:no-any
      (props: any) => {
        const { toDetails, includeBuried } = props;

        return createUrl(toDetails, {includeBuried: includeBuried || undefined});
      },
      commentsPageParam,
      commentsContainerAnchor
    )
  ),
  injectIntl,
  withHandlers({
    handleNewComment: (
      props: InjectedIntlProps & ParentProps & RouteComponentProps<{}> & WithCommentsProps
    ) => async () => {
      const {
        intl: { formatMessage },
        refetchComments,
        includeBuried,
        consumerType,
        consumerOid,
        commentOid,
        currentPage,
        toDetails
      } = props;

      openNotification.updateSuccess({
        description: '',
        message: formatMessage(CommentMessages.CommentPosted),
        duration: 5
      });

      // jw: if we were loaded with a comment, or the page is anything but the first one we need to direct the browser
      //     to the first page, where the new comment will be loaded.
      if (commentOid || currentPage !== 1) {
        const newURL = createUrl(
          `${toDetails}`,
          {includeBuried: includeBuried || undefined, [commentsPageParam]: undefined, [commentParam]: undefined},
          commentsContainerAnchor
        );

        // jw: this should cause the router to resolve the first page of comments and anchor the browser to the
        //     comments container header.
        props.history.push(newURL);

        // jw: Since we are on the first page, let's just refetch the results
      } else {
        // if we're already on the first page of results, we need to refetch in order to update the comments list
        await refetchComments({
          queryFields: { consumerType, consumerOid},
          pageInput: {
            size: COMMENT_PAGE_SIZE,
            page: 0,
            includeBuried
          }
        });

        // bl: now that we've loaded the comments, scroll to the same anchor at the top of the list of comments
        scrollToElement(commentsContainerAnchor);
      }
    }
  }),
  withHandlers({
    handleBuriedCommentSwitchChange: (
      props: InjectedIntlProps & ParentProps & RouteComponentProps<{}> & WithCommentsProps
    ) => async(checked: boolean) => {
      const { toDetails } = props;

      // Only pass includeBuried if it is true, otherwise remove it if it is already present
      // jw: note: we want to force them back to the first page, so specifying currentPage: undefined
      const newURL = createUrl(
          `${toDetails}`,
          { [commentsPageParam]: undefined, includeBuried: checked || undefined }
        );

      // zb: Added a new state check in scrollToTop so we can push history with the new state
      // without causing the page to scroll to the top, which is exactly what we want
      props.history.push(newURL, {scrollToTop: false});
    }
  }),
  withExtractedCurrentUser,
  // jw: let's resolve the viewers right to comment
  withProps((props: ParentProps & WithExtractedCurrentUserProps) => {
    const { typeSpecificPostingPermission, currentUserGlobalPermissions } = props;

    // jw: this is a signed in user (as detected by the presence of their permissions) and either...
    const canCommentOrRate = isPermissionGranted('postComments', currentUserGlobalPermissions) &&
      (
        // jw: if we were not given a type specific permission
        !typeSpecificPostingPermission ||
        // jw: or they have the permission
        isPermissionGranted(typeSpecificPostingPermission, currentUserGlobalPermissions)
      );

    return { canCommentOrRate };
  }),
  ModalConnect(ModalName.login),

  // jw: we have a bit of work to do in order to include the delete for AUP violation framework.
  // jw: first, we need the delete HOC
  withDeleteComment,
  // jw: next: we need some handlers
  withHandlers<AupDeletionHandlerProps, WithDeleteForAupViolationControllerParentHandlers>({
    // jw: we need to add a handleDeleteObjectForAupViolation function for withDeleteForAupViolationController
    handleDeleteObjectForAupViolation: (props) => async (commentOid: string) => {
      const { deleteComment, consumerType, consumerOid } = props;

      // jw: easy stuff, just delete the post
      await deleteComment({ consumerType, consumerOid, commentOid });
    },
    // jw: we need to refresh the comments once a comment is deleted.
    onObjectDeletedForAupViolation: (_props) => async () => {
      // jw: technically there is nothing for us to do for this, since the withDeleteComment HOC handles everything.
    }
  }),
  withDeleteForAupViolationController(CommentMessages.Comment)
)(CommentsSectionComponent) as React.ComponentClass<ParentProps>;
