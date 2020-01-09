import {
  Post,
  withDeletePost,
  WithStateProps,
  withState,
  WithDeletePostProps
} from '@narrative/shared';
import { branch, compose, withHandlers, withProps } from 'recompose';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import { showValidationErrorDialogIfNecessary } from '../utils/webErrorUtils';
import { SharedComponentMessages } from '../i18n/SharedComponentMessages';
import {
  WithCurrentUserProps,
  withExtractedCurrentUser,
  WithExtractedCurrentUserProps
} from './withExtractedCurrentUser';
import { DeletePostConfirmationProps } from '../../routes/Post/components/DeletePostConfirmation';

interface State {
  postForDeletion?: Post;
  isDeletingPost: boolean;
}

export interface WithOpenDeletePostConfirmationHandler {
  handleOpenDeletePostConfirmation?: (post: Post) => void;
}

interface WithCloseDeletePostConfirmationHandler {
  handleCloseDeletePostConfirmation: () => void;
}

interface WithDeletePostHandler {
  handleDeletePost: () => void;
}

export interface WithPostDeletedHandler {
  onPostDeleted: () => void;
}

export interface WithDeletePostControllerProps extends WithOpenDeletePostConfirmationHandler {
  deletePostConfirmationProps?: DeletePostConfirmationProps;
}

type HandleDeletePostProps =
  WithDeletePostProps &
  WithStateProps<State> &
  InjectedIntlProps &
  WithPostDeletedHandler &
  WithCurrentUserProps;

type SetupDeleteConfirmationProps =
  WithStateProps<State> &
  WithCurrentUserProps &
  WithDeletePostHandler &
  WithCloseDeletePostConfirmationHandler;

export const withDeletePostController = compose(
  withState<State>({isDeletingPost: false}),
  withDeletePost,
  injectIntl,
  withExtractedCurrentUser,
  // jw: let's only add the handlers and confirmation if we have a current user. Otherwise, what is the point!
  branch((props: WithExtractedCurrentUserProps) => !!props.currentUser,
    compose(
      withHandlers({
        handleOpenDeletePostConfirmation: (props: WithStateProps<State> & WithCurrentUserProps) =>
          async (post: Post) =>
        {
          const { setState, currentUser } = props;

          // jw: if the current user is not the author, then there is nothing to do.
          if (currentUser.oid !== post.author.oid) {
            return;
          }

          setState(ss => ({ ...ss, postForDeletion: post }));
        },
        handleCloseDeletePostConfirmation: (props: WithStateProps<State>) => async () => {
          const { setState } = props;

          setState(ss => ({ ...ss, postForDeletion: undefined }));
        },
        handleDeletePost: (props: HandleDeletePostProps) => async () => {
          const { deletePost, intl, onPostDeleted, setState, currentUser, state: { postForDeletion } } = props;

          // jw: if we do not have a post to delete, there is nothing to do.
          if (!postForDeletion) {
            return;
          }

          // jw: if the current user is not the author then there is nothing to do.
          if (currentUser.oid !== postForDeletion.author.oid) {
            return;
          }

          setState(ss => ({ ...ss, isDeletingPost: true }));

          try {
            await deletePost(postForDeletion.oid);

            if (onPostDeleted) {
              onPostDeleted();
            }
          } catch (err) {
            showValidationErrorDialogIfNecessary(intl.formatMessage(SharedComponentMessages.FormErrorTitle), err);
          }

          setState(ss => ({ ...ss, isDeletingPost: false, postForDeletion: undefined }));
        }
      }),
      withProps((props: SetupDeleteConfirmationProps) => {
        const {
          handleCloseDeletePostConfirmation,
          handleDeletePost,
          currentUser,
          state: { isDeletingPost, postForDeletion }
        } = props;

        let deletePostConfirmationProps: DeletePostConfirmationProps | undefined;
        if (currentUser) {
          deletePostConfirmationProps = {
            visible: !!postForDeletion,
            dismiss: handleCloseDeletePostConfirmation,
            onDeletePost: handleDeletePost,
            isLoading: isDeletingPost
          };
        }

        return { deletePostConfirmationProps };
      })
    )
  )
);
