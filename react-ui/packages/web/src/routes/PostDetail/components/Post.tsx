import * as React from 'react';
import { PostHeading } from './PostHeading';
import { PostBody } from './PostBody';
import { CompositionConsumerType, WithPostByIdProps, withState, WithStateProps } from '@narrative/shared';
import { mediaQuery } from '../../../shared/styled/utils/mediaQuery';
import styled from '../../../shared/styled';
import { CommentsSection } from '../../../shared/components/comment/CommentsSection';
import { PostNiches } from './PostNiches';
import { PostActions } from './PostActions';
import { PostShareIcons } from './PostShareIcons';

import 'froala-editor/css/froala_style.min.css';
import 'froala-editor/css/third_party/embedly.min.css';

import { PostAuthor } from './PostAuthor';
import { getPostUrl } from '../../../shared/utils/postUtils';
import { PostCanonicalReference } from './PostCanonicalReference';
import { PostRewards } from './PostRewards';
import { RatingDisabledWarning } from '../../../shared/components/rating/RatingDisabledWarning';
import { compose } from 'recompose';
import {
  PublicationDetailsConnect
} from '../../Publication/components/PublicationDetailsContext';
import { PendingPostCTA } from './PendingPostCTA';
import { PublicationRoleBooleans } from '../../../shared/utils/publicationRoleUtils';

interface State {
  ratingsDisabledModalVisible?: boolean;
}

const PostWrapper = styled.div`
  max-width: 900px;
  ${mediaQuery.lg_up`
    margin-right: 40px;
  `};
  
  ${mediaQuery.sm_down`
    margin-right: 0;
  `};
`;

export const FroalaEmbedlyStyle = (
  <style className="embedly-css">
    {`
      .hdr, .brd {
        display: none;
      }
    `}
  </style>
);

const HideXlAndUp = styled.div`
    ${mediaQuery.hide_lg_up}
`;

// bl: using this instead of WithPublicationDetailsContextProps so that it's clear that currentUserRoles is
// optional, which it will be for posts not in a publication.
interface WithPublicationCurrentUserRolesProps {
  currentUserRoles?: PublicationRoleBooleans;
}

type Props = WithPostByIdProps &
  WithStateProps<State> &
  WithPublicationCurrentUserRolesProps;

const PostComponent: React.SFC<Props> = (props) => {
  const {
    publishedToNiches,
    post,
    postDetail,
    currentUserRoles,
    setState,
    state: { ratingsDisabledModalVisible }
  } = props;
  const { prettyUrlString } = post;
  const postOid = post.oid;

  const ratingsDisabled = !post.postLive;

  return (
      <PostWrapper>
        {FroalaEmbedlyStyle}

        <article itemScope={true} itemProp="https://schema.org/Article">
          <PostHeading {...props}/>
          <PostBody {...props}/>
        </article>
        <PostCanonicalReference {...props}/>

        <PendingPostCTA post={post} postDetail={postDetail} />

        <PostActions
          {...props}
          showRatingsDisabledModal={ratingsDisabled
            ? () => setState(ss => ({...ss, ratingsDisabledModalVisible: true}))
            : undefined}
        />
        {ratingsDisabled &&
          <RatingDisabledWarning
            visible={ratingsDisabledModalVisible}
            dismiss={() => setState(ss => ({...ss, ratingsDisabledModalVisible: undefined}))}
          />}
        {post.postLive && <PostShareIcons {...props}/>}
        <HideXlAndUp><PostAuthor author={post.author}/></HideXlAndUp>
        {publishedToNiches.length > 0 &&
          <HideXlAndUp><PostNiches niches={publishedToNiches}/></HideXlAndUp>
        }
        <HideXlAndUp><PostRewards postOid={postOid}/></HideXlAndUp>

        <CommentsSection
          consumerType={CompositionConsumerType.posts}
          consumerOid={post.oid}
          toDetails={getPostUrl(prettyUrlString, post.oid)}
          allowNewComments={props.postDetail.allowComments}
          disableComments={!post.postLive}
          currentUserCanDeleteAllComments={currentUserRoles && currentUserRoles.editor}
        />
      </PostWrapper>
  );
};

export const Post = compose(
  withState<State>({}),
  PublicationDetailsConnect
)(PostComponent) as React.ComponentClass<WithPostByIdProps>;
