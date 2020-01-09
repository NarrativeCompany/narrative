import { branch, compose, withHandlers, withProps } from 'recompose';
import {
  withState,
  WithStateProps,
  handleFormlessServerOperation,
  WithRemovePostFromPublicationProps,
  withRemovePostFromPublication,
  WithPostByIdProps
} from '@narrative/shared';
import { Menu } from 'antd';
import { Link } from '../components/Link';
import { FormattedMessage, injectIntl, InjectedIntlProps } from 'react-intl';
import { PostDetailMessages } from '../i18n/PostDetailMessages';
import * as React from 'react';
import { openNotification } from '../utils/notificationsUtil';
import {
  PublicationDetailsConnect,
  WithPublicationDetailsContextProps
} from '../../routes/Publication/components/PublicationDetailsContext';
import { RemovePostFromPublicationModalProps } from '../components/post/RemovePostFromPublicationModal';
import { Button } from '../components/Button';

export interface WithRemovePostFromPublicationControllerProps {
  removePostFromPublicationModalProps?: RemovePostFromPublicationModalProps;
  generateRemovePostFromPublicationMenuItem?: () => React.ReactNode;
  generateRemovePostFromPublicationButton?: () => React.ReactNode;
}

interface State {
  visible?: boolean;
  processing?: boolean;
}

interface Handlers {
  // bl: note: it is vital that this handler has the same name as the optional one listed above.
  generateRemovePostFromPublicationMenuItem: () => React.ReactNode | undefined;
  generateRemovePostFromPublicationButton: () => React.ReactNode | undefined;
  closeRemovePostFromPublicationModal: () => void;
  removePostFromPublication: () => void;
}

type ParentProps = Pick<WithPostByIdProps, 'post' | 'postDetail'>;

type HandlerProps = WithStateProps<State> &
  InjectedIntlProps &
  WithPublicationDetailsContextProps &
  WithRemovePostFromPublicationProps &
  ParentProps;

/*
  jw: Let's centralize the logic for creating a ReactNode from a post. This will be useful for the menu item and the
      notification button.
 */
function generateRemoveNode(
  props: HandlerProps,
  nodeGenerator: (openModalHandler: () => void) =>  React.ReactNode
): React.ReactNode {
  const { publicationDetail, post } = props;
  if (!post || !post.publishedToPublication) {
    return;
  }

  if (publicationDetail.oid !== post.publishedToPublication.oid) {
    // todo:error-handling: this should never happen with the current model.
    return;
  }

  // jw: we are guaranteed to need to update the state at this point!
  const { setState } = props;

  return nodeGenerator(() => setState(ss => ({...ss, visible: true})));
}
export const withRemovePostFromPublicationController = compose(
  PublicationDetailsConnect,
  // jw: we only need to process if we are connected to the publication details and the viewer is an editor.
  branch<WithPublicationDetailsContextProps>((p) => !!p.currentUserRoles && p.currentUserRoles.editor,
    // jw: we want this full stack to resolve if the user is a editor.
    compose(
      withState<State>({}),
      withRemovePostFromPublication,
      injectIntl,
      withHandlers<HandlerProps, Handlers>({
        generateRemovePostFromPublicationMenuItem: (props) => (): React.ReactNode | undefined => {
          const { postDetail } = props;

          return generateRemoveNode(props, (openModalHandler => (
            <Menu.Item key="removePostFromPublicationMenuItem">
              <Link.Anchor onClick={openModalHandler}>
                <FormattedMessage {...(postDetail.pendingPublicationApproval
                  ? PostDetailMessages.RejectPostFromPublication
                  : PostDetailMessages.RemovePostFromPublication
                )}/>
              </Link.Anchor>
            </Menu.Item>
          )));
        },
        generateRemovePostFromPublicationButton: (props) => (): React.ReactNode | undefined => {
          return generateRemoveNode(props, (openModalHandler => (
            <Button
              type="danger"
              onClick={openModalHandler}
              size="small"
            >
              <FormattedMessage {...PostDetailMessages.RejectPostFromPublicationButtonText}/>
            </Button>
          )));
        },
        closeRemovePostFromPublicationModal: ({setState}) => () => {
          setState(ss => ({...ss, visible: undefined}));
        },
        removePostFromPublication: (props) => async (message?: string) => {
          const { removePostFromPublication, intl: { formatMessage }, setState, post } = props;

          if (!post) {
            // todo:error-handling need to report this error. this should always be called with a post in props
            return;
          }

          const newState: State = {};

          setState(ss => ({...ss, processing: true}));
          try {
            const result = await handleFormlessServerOperation(() => removePostFromPublication({message}, post.oid));

            if (result) {
              // Notify the user of success
              await openNotification.updateSuccess({
                description: formatMessage(PostDetailMessages.PostRemovedFromPublication),
                message: formatMessage(PostDetailMessages.PostRemovedFromPublicationTitle)
              });

              newState.visible = undefined;
            }

          } finally {
            setState(ss => ({...ss, ...newState, processing: undefined}));
          }
        },
      }),
      withProps<WithRemovePostFromPublicationControllerProps, WithStateProps<State> & Handlers & ParentProps>(
        (props): WithRemovePostFromPublicationControllerProps => {
          const {
            removePostFromPublication,
            closeRemovePostFromPublicationModal,
            state: { processing, visible },
            postDetail: { pendingPublicationApproval }
          } = props;

          return {
            removePostFromPublicationModalProps: {
              processing,
              pendingPublicationApproval: !!pendingPublicationApproval,
              closeModalHandler: closeRemovePostFromPublicationModal,
              removePostFromPublicationHandler: visible ? removePostFromPublication : undefined
            }
          };
        }
      )
    )
  )
);
