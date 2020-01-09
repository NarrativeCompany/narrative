import * as React from 'react';
import { branch, compose, withHandlers, withProps } from 'recompose';
import {
  AgeRating,
  withAgeRatePost,
  WithAgeRatePostProps,
  WithPostByIdProps,
  withState,
  handleFormlessServerOperation
} from '@narrative/shared';
import {
  withPermissionsModalController,
  WithPermissionsModalControllerProps
} from '../../../shared/containers/withPermissionsModalController';
import { RatingMessages } from '../../../shared/i18n/RatingMessages';
import {
  AuthorRatingWarningState,
  checkAccessRatingFeature,
  CheckRatingFeatureAccessProps,
  createAuthorRatingWarningProps
} from '../../../shared/containers/withQualityRatingController';
import { ModalConnect, ModalName } from '../../../shared/stores/ModalStore';
import {
  AuthorRateContentWarningProps
} from '../../../shared/components/rating/AuthorRateContentWarning';
import { PermissionErrorModal } from '../../../shared/components/PermissionErrorModal';
import styled from '../../../shared/styled';
import { VoteButton, VoteButtonProps } from '../../../shared/components/VoteButton';
import { Text } from '../../../shared/components/Text';
import { FormattedMessage } from 'react-intl';
import { RateObject } from '../../../shared/components/rating/RateObject';
import { EnhancedAgeRating } from '../../../shared/enhancedEnums/ageRating';
import { AuthorAgeRatePostWarning } from './AuthorAgeRatePostWarning';
import { PostActionProps } from './PostActions';

const StyledButton = styled<VoteButtonProps>(VoteButton)`
  &.ant-btn {
    padding: 0;
    text-align: center;
    width: 75px;
    text-transform: none;
  }
`;

interface Handlers {
  handleAgeRatePost: (rating?: AgeRating) => void;
}

type Props =
  WithPostByIdProps &
  WithPermissionsModalControllerProps &
  Handlers & {
    authorRateWarningProps?: AuthorRateContentWarningProps;
  };

const PostAgeRatingComponent: React.SFC<Props> = (props) => {
  const { permissionErrorModalProps, authorRateWarningProps, postDetail, handleAgeRatePost } = props;

  const currentRating = postDetail.ageRatingByCurrentUser;
  const fields = postDetail.post.ageRatingFields;

  const generalRating = EnhancedAgeRating.get(AgeRating.GENERAL);
  const restrictedRating = EnhancedAgeRating.get(AgeRating.RESTRICTED);
  const rating = EnhancedAgeRating.get(fields.ageRating);

  const handleAgeRating = (forRating: AgeRating) => () => {
    if (currentRating === forRating) {
      handleAgeRatePost(undefined);
    } else {
      handleAgeRatePost(forRating);
    }
  };

  return (
    <React.Fragment>
      {permissionErrorModalProps && <PermissionErrorModal {...permissionErrorModalProps} />}
      {authorRateWarningProps && <AuthorAgeRatePostWarning post={postDetail.post} {...authorRateWarningProps} />}

      <RateObject
        title={RatingMessages.Audience}
        fields={fields}
        scorePlaceholder={<Text><FormattedMessage {...rating.titleMessage} /></Text>}
        progressStrokeColor={rating.themeColor}
        leftTool={(
          <StyledButton
            isButtonActive={currentRating === AgeRating.GENERAL}
            buttonType="primary"
            onClick={handleAgeRating(AgeRating.GENERAL)}
            className="general-vote-button"
          >
            <FormattedMessage {...generalRating.titleMessage} />
          </StyledButton>
        )}
        rightTool={(
          <StyledButton
            isButtonActive={currentRating === AgeRating.RESTRICTED}
            buttonType="danger"
            onClick={handleAgeRating(AgeRating.RESTRICTED)}
            className="restricted-vote-button"
          >
            <FormattedMessage {...restrictedRating.titleMessage} />
          </StyledButton>
        )}
      />
    </React.Fragment>
  );
};

type HandlerProps =
  WithPostByIdProps &
  CheckRatingFeatureAccessProps &
  WithAgeRatePostProps;

export const PostAgeRating = compose(
  // bl: if ratings are disabled, then we don't need the mutation/handler/controller
  branch((props: PostActionProps) => !!props.showRatingsDisabledModal,
    // bl: if ratings are disabled, then just include a handler with a popup error
    withHandlers<PostActionProps, Handlers>({
      handleAgeRatePost: (props: PostActionProps) => (_rating?: AgeRating) => {
        (props.showRatingsDisabledModal as () => void)();
      }
    }),
    // bl: if ratings are enabled, then compose our normal rating HOC stack
    compose(
      withState<AuthorRatingWarningState>({}),
      ModalConnect(ModalName.login),
      withPermissionsModalController('rateContent', RatingMessages.RatePostsAction),
      withAgeRatePost,
      withHandlers<HandlerProps, Handlers>({
        handleAgeRatePost: (props: HandlerProps) => async (rating?: AgeRating) => {
          const { post } = props;

          // bl: don't pass in the author OID if the post is published to a publication. that indicates we do
          // NOT want to do the author check
          if (checkAccessRatingFeature(props, post.publishedToPublication ? '' : post.author.oid)) {
            const { ageRatePost } = props;

            await handleFormlessServerOperation(() => ageRatePost({rating}, post.oid));
          }
        }
      }),
      withProps((props: WithPostByIdProps & CheckRatingFeatureAccessProps) => {
        const { currentUser, post } = props;

        // jw: the point of this withProps is to add the author rating warning props. So, let's short out if this is
        //     not the author since that is the only time we should ever need that modal.
        if (!currentUser || currentUser.oid !== post.author.oid) {
          return null;
        }

        // bl: if the post is published to a publication, then we will allow the author to age rate the post directly
        // via the form like all other users. in that case, return null
        if (post.publishedToPublication) {
          return null;
        }

        const authorRateWarningProps = createAuthorRatingWarningProps(props);

        return { authorRateWarningProps };
      })
    )
  )
)(PostAgeRatingComponent) as React.ComponentClass<PostActionProps>;
