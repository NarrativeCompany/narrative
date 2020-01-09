import { compose, withHandlers, withProps } from 'recompose';
import {
  QualityRating,
  withState,
  WithStateProps,
  handleFormlessServerOperation
} from '@narrative/shared';
import {
  PermissionsModalControllerWithProps,
  withPermissionsModalController,
  WithPermissionsModalControllerProps
} from './withPermissionsModalController';
import { FormattedMessage } from 'react-intl';
import { ModalStoreProps, ModalConnect, ModalName } from '../stores/ModalStore';
import { DownVoteQualityRatingSelectorModalProps } from '../components/rating/DownVoteQualityRatingSelectorModal';
import { AuthorRateContentWarningProps } from '../components/rating/AuthorRateContentWarning';

interface DownVoteObject {
  ratedObjectOid: string;
  authorOid: string;
  currentRating?: QualityRating;
}

// jw: this is extracted out so that all of the checkAccessRatingFeature method can be shared externally
export interface AuthorRatingWarningState {
  isShowAuthorRatingWarning?: boolean;
}

interface State extends AuthorRatingWarningState {
  downVoteObjectsQualityRating?: DownVoteObject;
  isSubmittingQualityRating?: boolean;
}

export interface WithQualityRatingControllerParentHandlers {
  handleSubmitQualityRating: (ratedObjectOid: string, rating?: QualityRating, reason?: string) => void;
}

export type CheckRatingFeatureAccessProps =
  WithPermissionsModalControllerProps &
  WithStateProps<AuthorRatingWarningState> &
  ModalStoreProps;

// jw: this method will ensure access, and show any relevant error if the view does not have access.
export function checkAccessRatingFeature(props: CheckRatingFeatureAccessProps, authorOid: string): boolean {
  const { granted, currentUser, modalStoreActions, handleShowPermissionsModal } = props;

  // jw: prompt guests to login
  if (!currentUser) {
    modalStoreActions.updateModalVisibility(ModalName.login);
    return false;
  }

  // jw: prevent authors from rating their own content
  if (currentUser.oid === authorOid) {
    const { setState } = props;

    setState(ss => ({ ...ss, isShowAuthorRatingWarning: true }));
    return false;
  }

  // jw: if the user does not have the right to vote, give them the permissions modal.
  if (!granted) {
    handleShowPermissionsModal();
    return false;
  }

  // jw: finally, it looks like they are good to go.
  return true;
}

// jw: similar to above, let's expose a easy way to create the author rating warning props
export function createAuthorRatingWarningProps
  (props: WithStateProps<AuthorRatingWarningState>): AuthorRateContentWarningProps
{
  const { state, setState } = props;

  const visible = state.isShowAuthorRatingWarning;
  return {
    visible: visible !== undefined && visible,
    dismiss: () => setState(ss => ({ ...ss, isShowAuthorRatingWarning: undefined }))
  };
}

export interface WithQualityRatingControllerHandlers {
  handleOpenDownVoteSelector: (ratedObjectOid: string, authorOid: string, currentRating?: QualityRating) => void;
  handleQualityRating: (ratedObjectOid: string, authorOid: string, rating?: QualityRating, reason?: string) => void;
}

type AllHandlerProps =
  WithQualityRatingControllerParentHandlers &
  CheckRatingFeatureAccessProps &
  WithStateProps<State>;

export interface WithQualityRatingControllerProps extends
  WithQualityRatingControllerHandlers,
  PermissionsModalControllerWithProps
{
  downVoteQualityRatingSelectorProps?: DownVoteQualityRatingSelectorModalProps;
  authorRateWarningProps?: AuthorRateContentWarningProps;
}

export function withQualityRatingController(ratingAction: FormattedMessage.MessageDescriptor) {
  return compose(
    withState({}),
    withPermissionsModalController('rateContent', ratingAction),
    ModalConnect(ModalName.login),
    withHandlers<AllHandlerProps, WithQualityRatingControllerHandlers>({
      handleOpenDownVoteSelector: (props: AllHandlerProps) => (
        ratedObjectOid: string,
        authorOid: string,
        currentRating?: QualityRating
      ) => {
        if (!checkAccessRatingFeature(props, authorOid)) {
          return;
        }

        const { setState } = props;

        setState(ss => ({ ...ss, downVoteObjectsQualityRating: {ratedObjectOid, authorOid, currentRating} }));
      },
      handleQualityRating: (props: AllHandlerProps) =>
        async (ratedObjectOid: string, authorOid: string, rating?: QualityRating, reason?: string) =>
      {
        if (!checkAccessRatingFeature(props, authorOid)) {
          return;
        }

        // jw: if we are already submitting then let's short out. Let's not hammer the server.
        if (props.state.isSubmittingQualityRating) {
          return;
        }

        const { setState, handleSubmitQualityRating } = props;

        setState(ss => ({ ...ss, isSubmittingQualityRating: true }));

        try {
          await handleFormlessServerOperation(() => handleSubmitQualityRating(ratedObjectOid, rating, reason));

        } finally {
          setState(ss => ({ ...ss, isSubmittingQualityRating: undefined }));
        }
      }
    }),
    withProps((props: WithQualityRatingControllerHandlers &
      WithStateProps<State> &
      WithPermissionsModalControllerProps
    ) => {
      const { setState, state, currentUser, handleQualityRating } = props;

      const ratedObject = state.downVoteObjectsQualityRating;

      // jw: we only need to include the rating selector for users who are logged in, otherwise they will get the login
      //     modal. See checkAccessRatingFeature for order of checks
      let downVoteQualityRatingSelectorProps: DownVoteQualityRatingSelectorModalProps | undefined;
      // jw: similar to above, let's only include the cannot rate own content modal if this is a logged in user.
      let authorRateWarningProps: AuthorRateContentWarningProps | undefined;
      if (currentUser) {
        downVoteQualityRatingSelectorProps = {
          visible: ratedObject !== undefined,
          onDownVoteRatingSelected: (rating: QualityRating, reason?: string) => {
            if (!ratedObject) {
              // todo:error-handling: How could the user select a option if the selector isn't visible?
              return;
            }
            handleQualityRating(ratedObject.ratedObjectOid, ratedObject.authorOid, rating, reason);
          },
          dismiss: () => setState(ss => ({ ...ss, downVoteObjectsQualityRating: undefined })),
          currentRating: ratedObject && ratedObject.currentRating
        };

        authorRateWarningProps = createAuthorRatingWarningProps(props);
      }

      return { downVoteQualityRatingSelectorProps, authorRateWarningProps };
    })
  );
}
