import * as React from 'react';
import { compose, withHandlers, withProps } from 'recompose';
import { Comment, withState, WithStateProps } from '@narrative/shared';
import { FlexContainer } from '../../styled/shared/containers';
import { MemberLink } from '../user/MemberLink';
import { LocalizedTime } from '../LocalizedTime';
import styled from '../../styled';
import { CommentAuthorControls, WithCurrentUserCommentDeleteProps } from './CommentAuthorControls';
import { CommentEditingForm } from './CommentEditingForm';
import { MemberAvatar } from '../user/MemberAvatar';
import { WithPaginationCurrentPageProps } from '../../containers/withPaginationController';
import { CommentsSectionProps, WithDisableCommentRatingProps } from './CommentsSection';
import { WithQualityRatingControllerHandlers } from '../../containers/withQualityRatingController';
import { Block } from '../Block';
import { QualityRateObject } from '../rating/QualityRateObject';
import { EnhancedQualityLevel } from '../../enhancedEnums/qualityLevel';
import { WithOpenDeleteForAupViolationConfirmationHandler } from '../../containers/withDeleteForAupViolationController';
import { QualityLevelIcon } from '../QualityLevelIcon';
import * as H from 'history';
import { CommentLink } from './CommentLink';
import { UrlFragmentAnchor } from '../UrlFragmentAnchor';
import { commentBodyStyles } from '../../styled/shared/comment';

interface State {
  forEdit: boolean;
}

export interface CommentProps extends CommentsSectionProps {
  comment: Comment;
}

export interface WithToggleCommentEditFormHandler {
  toggleCommentEditForm: (show: boolean) => void;
}

type ParentProps =
  CommentProps &
  WithDisableCommentRatingProps &
  WithPaginationCurrentPageProps &
  WithOpenDeleteForAupViolationConfirmationHandler &
  WithQualityRatingControllerHandlers &
  WithCurrentUserCommentDeleteProps & {
  toDetails: H.LocationDescriptor;
};

const CommentContainer = styled(FlexContainer)`
  position: relative;
  padding: 10px 10px 10px 0;
  &.buriedCommentContainer {
    opacity: .5;
  }
  
  & .member-username {
    font-size: 14px;
  }
`;

const AvatarWrapper = styled(FlexContainer)`
  width: 60px;
  margin-top: 5px;
  
  img {
    max-width: 40px;
  }
`;

const BodyContainer = styled.div`
  font-size: ${props => props.theme.textFontSizeDefault};
  
  ${commentBodyStyles};
`;

type Props = ParentProps &
  WithToggleCommentEditFormHandler &
  WithDisableCommentRatingProps &
  State;

const DisplayCommentComponent: React.SFC<Props> = (props) => {
  const {
    consumerType,
    consumerOid,
    canCommentOrRate,
    toDetails,
    disableCommentRating,
    comment,
    forEdit,
    toggleCommentEditForm,
    currentPage,
    includeBuried,
    handleQualityRating,
    handleOpenDownVoteSelector,
    openDeleteForAupViolationConfirmation,
    currentUserCanDeleteAllComments
  } = props;
  const { oid, user, body, liveDatetime, qualityRatingFields } = comment;
  const { qualityLevel } = qualityRatingFields;
  const sharedFields = { consumerType, consumerOid, canCommentOrRate, includeBuried };

  let baseContainerClasses = 'comment-container';

  const quality = qualityLevel ? EnhancedQualityLevel.get(qualityLevel) : null;
  if (quality && quality.iconType) {
    if (quality.isLow()) {
      baseContainerClasses += ' buriedCommentContainer';
    }
  }

  return (
    <UrlFragmentAnchor id={oid}>
      <CommentContainer alignItems="flex-start" className={baseContainerClasses}>
        <FlexContainer column={true}>
          <AvatarWrapper justifyContent="center">
            <MemberAvatar user={user} />
          </AvatarWrapper>
          <FlexContainer centerAll={true}>
            <QualityLevelIcon qualityLevel={qualityLevel} style={{marginTop: 10}}/>
          </FlexContainer>
        </FlexContainer>

        <FlexContainer column={true} style={{width: '100%'}}>
          <FlexContainer justifyContent="space-between">
            <Block size="large" color="dark">
              <MemberLink user={user} appendUsername={true} />
            </Block>
            {/* jw: we want the controls to be off to the right */}
            <CommentAuthorControls
              {...sharedFields}
              comment={comment}
              editFormShown={forEdit}
              toggleCommentEditForm={toggleCommentEditForm}
              currentPage={currentPage}
              openDeleteForAupViolationConfirmation={openDeleteForAupViolationConfirmation}
              currentUserCanDeleteAllComments={currentUserCanDeleteAllComments}
            />
          </FlexContainer>

          <Block size="small" color="light" style={{marginBottom: 5}}>
            <CommentLink toConsumer={toDetails} comment={comment} color="inherit" noHoverEffect={true}>
              <LocalizedTime time={liveDatetime} />
            </CommentLink>
          </Block>

          {forEdit &&
          <CommentEditingForm
            {...sharedFields}
            comment={comment}
            toggleCommentEditForm={toggleCommentEditForm}
          />
          }

          {!forEdit && (
            <React.Fragment>
              <BodyContainer dangerouslySetInnerHTML={{__html: body}} />
              {!disableCommentRating &&
              <Block>
                <QualityRateObject
                  objectOid={comment.oid}
                  authorOid={comment.user.oid}
                  fields={comment.qualityRatingFields}
                  current={comment.qualityRatingByCurrentUser}
                  handleOpenDownVoteSelector={handleOpenDownVoteSelector}
                  handleQualityRating={handleQualityRating}
                  isSmall={true}
                />
              </Block>
              }
            </React.Fragment>
          )}
        </FlexContainer>
      </CommentContainer>
    </UrlFragmentAnchor>
  );
};

export const DisplayComment = compose(
  withState({ forEdit: false }),
  // jw: I really dislike relying on the state in the rendering component, so let's move the forEdit down to props
  withProps((props: WithStateProps<State>) => {
    const { state: { forEdit } } = props;

    return { forEdit };
  }),
  // jw: if we are going to render, we will need a handler for when the reply changes (like edits and likes)
  withHandlers({
    toggleCommentEditForm: (props: WithStateProps<State>) => (show: boolean) => {
      const { setState } = props;

      setState(ss => ({...ss, forEdit: show}));
    }
  })
)(DisplayCommentComponent) as React.ComponentClass<ParentProps>;
