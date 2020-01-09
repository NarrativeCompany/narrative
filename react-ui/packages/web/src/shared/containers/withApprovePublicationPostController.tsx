import { branch, compose, withHandlers, withProps } from 'recompose';
import {
  withState,
  WithStateProps,
  handleFormlessServerOperation,
  WithApprovePublicationPostProps,
  withApprovePublicationPost,
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
import { ApprovePublicationPostModalProps } from '../components/post/ApprovePublicationPostModal';
import { Button } from '../components/Button';
import { ButtonSize } from 'antd/lib/button';

type GenerateButtonType = (size: ButtonSize, label: FormattedMessage.MessageDescriptor) => React.ReactNode;

export interface WithApprovePublicationPostControllerProps {
  approvePublicationPostModalProps?: ApprovePublicationPostModalProps;
  generateApprovePublicationPostMenuItem?: () => React.ReactNode;
  generateApprovePublicationPostButton?: GenerateButtonType;
}

interface State {
  visible?: boolean;
  processing?: boolean;
}

interface Handlers {
  // jw: note: it is vital that this handler has the same name as the optional one listed above.
  generateApprovePublicationPostMenuItem: () => React.ReactNode;
  generateApprovePublicationPostButton: GenerateButtonType | undefined;
  closeApprovePublicationPostModal: () => void;
  approvePublicationPostHandler: () => void;
}

interface ParentHandlerProps {
  // this function will override the state change that normally would close the modal
  approveSuccessHandler?: () => void;
}

export type WithApprovePublicationPostControllerParentProps = Pick<WithPostByIdProps, 'postDetail'> &
  Pick<WithPublicationDetailsContextProps, 'publicationDetail' | 'currentUserRoles'> &
  ParentHandlerProps;

type HandlerProps = WithStateProps<State> &
  InjectedIntlProps &
  WithApprovePublicationPostProps &
  WithApprovePublicationPostControllerParentProps;

/*
  jw: Let's centralize the logic for creating a ReactNode from a post. This will be useful for the menu item and the
      notification button.
 */
function generateApprovalNode(
  props: HandlerProps,
  nodeGenerator: (openModalHandler: () => void) =>  React.ReactNode
): React.ReactNode | undefined {
  const { publicationDetail, postDetail: { post } } = props;
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

export const withApprovePublicationPostController = compose(
  PublicationDetailsConnect,
  // jw: we only need to process if we are connected to the publication details and the viewer is an editor.
  branch<WithApprovePublicationPostControllerParentProps>(
    // jw: note: we are not going to include this if the post is not pending approval.
    (p) => !!p.currentUserRoles && p.currentUserRoles.editor && !!p.postDetail.pendingPublicationApproval,
    // jw: we want this full stack to resolve if the user is an editor and the post is not live.
    compose(
      withState<State>({}),
      withApprovePublicationPost,
      injectIntl,
      withHandlers<HandlerProps, Handlers>({
        generateApprovePublicationPostMenuItem: (props) => (): React.ReactNode | undefined => {
          return generateApprovalNode(props, (openModalHandler => (
            <Menu.Item key="approvePublicationPostMenuItem">
              <Link.Anchor
                onClick={openModalHandler}
              >
                <FormattedMessage {...PostDetailMessages.ApprovePostInPublication}/>
              </Link.Anchor>
            </Menu.Item>
          )));
        },
        generateApprovePublicationPostButton: (props) =>
          (size: ButtonSize, label: FormattedMessage.MessageDescriptor): React.ReactNode | undefined => {
          return generateApprovalNode(props, (openModalHandler => (
            <Button
              type="primary"
              onClick={openModalHandler}
              size={size}
            >
              <FormattedMessage {...label}/>
            </Button>
          )));
        },
        closeApprovePublicationPostModal: ({setState}) => () => {
          setState(ss => ({...ss, visible: undefined}));
        },
        approvePublicationPostHandler: (props) => async () => {
          const {
            approvePublicationPost,
            approveSuccessHandler,
            intl: { formatMessage },
            setState,
            postDetail: { post }
          } = props;

          if (!post) {
            // todo:error-handling need to report this error. this should always be called with a post in props
            return;
          }

          const newState: State = {};

          setState(ss => ({...ss, processing: true}));
          try {
            const result = await handleFormlessServerOperation(() => approvePublicationPost(post.oid));

            if (result) {
              // Notify the user of success
              await openNotification.updateSuccess({
                description: formatMessage(PostDetailMessages.PostApprovedInPublication),
                message: formatMessage(PostDetailMessages.PostApprovedInPublicationTitle)
              });

              if (approveSuccessHandler) {
                approveSuccessHandler();
              } else {
                newState.visible = undefined;
              }
            }

          } finally {
            if (!approveSuccessHandler) {
              setState(ss => ({...ss, ...newState, processing: undefined}));
            }
          }
        }
      }),
      withProps<WithApprovePublicationPostControllerProps, WithStateProps<State> & Handlers>((props) => {
        const {
          approvePublicationPostHandler,
          closeApprovePublicationPostModal,
          state: { processing, visible }
        } = props;

        return {
          approvePublicationPostModalProps: {
            processing,
            close: closeApprovePublicationPostModal,
            approvePost: visible ? approvePublicationPostHandler : undefined
          }
        };
      })
    )
  )
);
