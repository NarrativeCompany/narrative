import { branch, compose, withHandlers, withProps } from 'recompose';
import { FeaturePostModalsProps } from '../components/post/FeaturePostModals';
import {
  Post,
  PostDetail,
  withState,
  WithStateProps,
  FeaturePostDuration,
  withFeaturePost,
  withUnfeaturePost,
  WithFeaturePostProps,
  WithUnfeaturePostProps,
  handleFormlessServerOperation
} from '@narrative/shared';
import { Menu } from 'antd';
import { Link } from '../components/Link';
import { FormattedMessage, injectIntl, InjectedIntlProps } from 'react-intl';
import { PostDetailMessages } from '../i18n/PostDetailMessages';
import * as React from 'react';
import { openNotification } from '../utils/notificationsUtil';
import { PublicationDetailsMessages } from '../i18n/PublicationDetailsMessages';
import { EnhancedFeaturePostDuration } from '../enhancedEnums/featurePostDuration';
import {
  PublicationDetailsConnect,
  WithPublicationDetailsContextProps
} from '../../routes/Publication/components/PublicationDetailsContext';

// jw: this controller produces the properties for the feature post modals if the viewer is a editor within any
//     publications. The other piece are functions to generate the menu items for
export interface WithFeaturePostControllerProps {
  featurePostModalsProps?: FeaturePostModalsProps;
  generateFeaturePostMenuItem?: (postDetail: PostDetail) => React.ReactNode;
}

// jw: we need these pieces of data in the state in order to know what to show, and what is being operated on.
interface State {
  postForFeaturing?: Post;
  showUnfeatureModal?: boolean;
  processingFeatureOperation?: boolean;
}

// jw: I am going to define all of these
interface Handlers {
  // jw: note: it is vital that this handler has the same name as the optional one listed above.
  generateFeaturePostMenuItem: (postDetail: PostDetail) => React.ReactNode | undefined;
  closeFeaturedPostModals: () => void;
  featureSelectedPost: (duration: FeaturePostDuration) => void;
  unfeatureSelectedPost: () => void;
}

type HandlerProps = WithStateProps<State> &
  InjectedIntlProps &
  WithPublicationDetailsContextProps &
  WithFeaturePostProps &
  WithUnfeaturePostProps;

export const withFeaturePostController = compose(
  PublicationDetailsConnect,
  // jw: we only need to process if we are connected to the publication details and the viewer is an editor.
  branch<WithPublicationDetailsContextProps>((p) => !!p.currentUserRoles && p.currentUserRoles.editor,
    // jw: we want this full stack to resolve if the user is a editor.
    compose(
      withState<State>({}),
      withFeaturePost,
      withUnfeaturePost,
      injectIntl,
      withHandlers<HandlerProps, Handlers>({
        generateFeaturePostMenuItem: (props) => (postDetail: PostDetail): React.ReactNode | undefined => {
          const { post } = postDetail;
          // jw: first, if the post is not published to a publication then short out!
          if (!post.publishedToPublication) {
            return;
          }

          const { publicationDetail } = props;
          if (publicationDetail.oid !== post.publishedToPublication.oid) {
            // todo:error-handling: this should never happen with the current model.
            return;
          }

          // jw: if the post is not live yet then do not include the option to feature it.
          if (postDetail.pendingPublicationApproval) {
            return;
          }

          // jw: we are guaranteed to need to update the state at this point!
          const { setState } = props;

          // jw: if the post is currently featured then let's always give the ability to unfeature.
          if (post.featuredInPublication) {
            return (
              <Menu.Item key="unfeaturePostMenuItem">
                <Link.Anchor
                  onClick={() => setState(ss => ({
                    ...ss,
                    postForFeaturing: post,
                    showUnfeatureModal: true
                  }))}
                >
                  <FormattedMessage {...PostDetailMessages.UnfeaturePost}/>
                </Link.Anchor>
              </Menu.Item>
            );
          }

          return (
            <Menu.Item key="featurePostMenuItem">
              <Link.Anchor
                onClick={() => setState(ss => ({
                  ...ss,
                  postForFeaturing: post,
                  showUnfeatureModal: undefined,
                }))}
              >
                <FormattedMessage {...PostDetailMessages.FeaturePost}/>
              </Link.Anchor>
            </Menu.Item>
          );
        },
        closeFeaturedPostModals: ({setState}) => () => {
          setState(ss => ({
            ...ss,
            postForFeaturing: undefined,
            showUnfeatureModal: undefined
          }));
        },
        featureSelectedPost: (props) => async (duration: FeaturePostDuration) => {
          const { featurePost, intl: { formatMessage }, setState, state: { postForFeaturing }} = props;

          if (!postForFeaturing) {
            // todo:error-handling: We should always have a postForFeaturing here.
            return;
          }

          const newState: State = {};

          setState(ss => ({...ss, processingFeatureOperation: true}));
          try {
            const result = await handleFormlessServerOperation(() => featurePost({duration}, postForFeaturing.oid));

            if (result) {
              const durationType = EnhancedFeaturePostDuration.get(duration);
              const durationText = formatMessage(durationType.titleLc);

              // Notify the user of success
              await openNotification.updateSuccess({
                description: formatMessage(PublicationDetailsMessages.PostFeaturedDescription, {durationText}),
                message: formatMessage(PublicationDetailsMessages.PostFeaturedTitle)
              });

              newState.postForFeaturing = undefined;
              newState.showUnfeatureModal = undefined;
            }

          } finally {
            setState(ss => ({...ss, ...newState, processingFeatureOperation: undefined}));
          }
        },
        unfeatureSelectedPost: (props) => async () => {
          const { unfeaturePost, intl: { formatMessage }, setState, state: { postForFeaturing }} = props;

          if (!postForFeaturing) {
            // todo:error-handling: We should always have a postForFeaturing here.
            return;
          }

          const newState: State = {};

          setState(ss => ({...ss, processingFeatureOperation: true}));
          try {
            const result = await handleFormlessServerOperation(() => unfeaturePost(postForFeaturing.oid));

            if (result) {
              // Notify the user of success
              await openNotification.updateSuccess({
                description: formatMessage(PublicationDetailsMessages.PostUnfeaturedDescription),
                message: formatMessage(PublicationDetailsMessages.PostUnfeaturedTitle)
              });

              newState.postForFeaturing = undefined;
              newState.showUnfeatureModal = undefined;
            }

          } finally {
            setState(ss => ({...ss, ...newState, processingFeatureOperation: undefined}));
          }
        }
      }),
      withProps<WithFeaturePostControllerProps, WithStateProps<State> & Handlers>((props) => {
        const { state, featureSelectedPost, unfeatureSelectedPost, closeFeaturedPostModals } = props;
        const { postForFeaturing, showUnfeatureModal, processingFeatureOperation } = state;

        let featurePostHandler: ((duration: FeaturePostDuration) => void) | undefined;
        let unfeaturePostHandler: (() => void) | undefined;

        if (postForFeaturing) {
          if (showUnfeatureModal) {
            unfeaturePostHandler = unfeatureSelectedPost;

          } else if (postForFeaturing.titleImageUrl) {
            featurePostHandler = featureSelectedPost;
          }
        }

        return {
          featurePostModalsProps: {
            post: postForFeaturing,
            processing: processingFeatureOperation,
            closeModalHandler: closeFeaturedPostModals,
            featurePostHandler,
            unfeaturePostHandler
          }
        };
      })
    )
  )
);
