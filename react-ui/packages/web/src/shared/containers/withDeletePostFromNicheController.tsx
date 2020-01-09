import * as React from 'react';
import { branch, compose, withHandlers, withProps } from 'recompose';
import { withExtractedCurrentUser, WithExtractedCurrentUserProps } from './withExtractedCurrentUser';
import {
  Post,
  Niche,
  withState,
  withDeletePostFromNiche,
  WithStateProps,
  WithDeletePostFromNicheProps
} from '@narrative/shared';
import { Menu } from 'antd';
import { DeletePostFromNicheConfirmationProps } from '../components/post/DeletePostFromNicheConfirmation';
import { FormattedMessage } from 'react-intl';
import { SharedComponentMessages } from '../i18n/SharedComponentMessages';
import { Link } from '../components/Link';

interface ParentProps {
  post: Post;
}

interface State {
  removePostFromNiche?: Niche;
  removingPostFromNiche?: boolean;
}

interface Handlers {
  deletePostFromNicheConfirmed: () => void;
  confirmDeletePostFromNiche: (niche: Niche) => void;
  dismissDeletePostConfirmation: () => void;
}

export interface WithDeletePostFromNicheControllerProps {
  deletePostFromNicheMenuItems?: [React.ReactNode];
  deletePostFromNicheConfirmationProps?: DeletePostFromNicheConfirmationProps;
}

export const withDeletePostFromNicheController = compose(
  withExtractedCurrentUser,
  // jw: only continue doing anything if we have a currentUser
  branch((p: WithExtractedCurrentUserProps) => !!p.currentUser,
    compose(
      // jw: first, setup the state
      withState<State>({}),
      // jw: next, we need the deletion function
      withDeletePostFromNiche,
      // jw: next, we will need these handlers for the menu items to work.
      withHandlers({
        confirmDeletePostFromNiche: (props: WithStateProps<State>) => (niche: Niche) => {
          props.setState(ss => ({...ss, removePostFromNiche: niche}));
        },
        deletePostFromNicheConfirmed: (props: WithStateProps<State> & WithDeletePostFromNicheProps & ParentProps) =>
          async () =>
        {
          const { post, setState, state: { removePostFromNiche }, deletePostFromNiche } = props;

          if (!removePostFromNiche) {
            // jw:todo:error-handling: We should always have a niche in state if we get here.
            return;
          }

          setState(ss => ({...ss, removingPostFromNiche: true}));
          try {
            await deletePostFromNiche(post.oid, removePostFromNiche.oid);
          } finally {
            setState(ss => ({...ss, removingPostFromNiche: undefined, removePostFromNiche: undefined}));
          }
        },
        dismissDeletePostConfirmation: (props: WithStateProps<State>) => () => {
          props.setState(ss => ({...ss, removePostFromNiche: undefined}));
        }
      }),

      // jw: now that the above is complete, let's gather all our resources and set this bad boy up!
      withProps((props: WithExtractedCurrentUserProps & WithStateProps<State> & ParentProps & Handlers) => {
        const { currentUser, post } = props;

        if (!currentUser || !post) {
          // jw:todo:error-handling: We should always have a post and currentUser by this point.
          return null;
        }

        // jw: if the post is not published to any niches, short out.
        if (!post.publishedToNiches || !post.publishedToNiches.length) {
          return null;
        }

        // jw: now that we are closer to actually creating the menu items, we need to start pulling callbacks
        const { confirmDeletePostFromNiche } = props;

        // jw: let's create a place to gather the menu items
        const deletePostFromNicheMenuItems: React.ReactNode[] = [];

        // jw: now, we can iterate over all of the niches that the post was published to
        post.publishedToNiches.forEach((niche) => {
          // jw: if the niche does not exist (which ts says is possible), or it does not have an owner, short out.
          if (!niche || !niche.owner) {
            return;
          }

          // jw: if the current user is the owner, let's add a menu item.
          if (currentUser.oid === niche.owner.oid) {
            const nicheName = niche.name;
            deletePostFromNicheMenuItems.push(
              <Menu.Item key={`removeFromNiche${niche.oid}`}>
                <Link.Anchor onClick={() => confirmDeletePostFromNiche(niche)}>
                  <FormattedMessage {...SharedComponentMessages.RemoveFromNiche} values={{nicheName}}/>
                </Link.Anchor>
              </Menu.Item>
            );
          }
        });

        // jw: if we still do not have any menu items, then short out, no need to include the popup or the menu.
        if (!deletePostFromNicheMenuItems.length) {
          return null;
        }

        const {
          state: { removingPostFromNiche, removePostFromNiche },
          dismissDeletePostConfirmation,
          deletePostFromNicheConfirmed
        } = props;

        // jw: let's create the delete post from niche modal properties now that we have everything we need.
        const deletePostFromNicheConfirmationProps: DeletePostFromNicheConfirmationProps = {
          visible: !!removePostFromNiche,
          processing: removingPostFromNiche,
          dismiss: dismissDeletePostConfirmation,
          onConfirmation: deletePostFromNicheConfirmed,
          niche: removePostFromNiche
        };

        return { deletePostFromNicheMenuItems, deletePostFromNicheConfirmationProps };
      })
    )
  )
);
