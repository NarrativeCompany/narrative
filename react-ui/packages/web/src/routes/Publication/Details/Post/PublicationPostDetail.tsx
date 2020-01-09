import * as React from 'react';
import { compose } from 'recompose';
import { PostDetailConnect, WithPostDetailContextProps } from '../../../PostDetail/components/PostDetailContext';
import { PublicationSidebarViewWrapper } from '../../components/PublicationSidebarViewWrapper';
import { PostSidebar } from '../../../PostDetail/components/PostSidebar';
import PostDetailBody from '../../../PostDetail/PostDetailBody';

const PublicationPostDetailComponent: React.SFC<WithPostDetailContextProps> = (props) => {
  const { postByIdProps } = props;

  // jw: render the two main aspects of the post details, but use the simplified sidebar wrapper for publication pages
  //     since we already have a ViewWrapper in scope.
  return (
    <PublicationSidebarViewWrapper sidebarItems={<PostSidebar {...postByIdProps}/>}>
      <PostDetailBody />
    </PublicationSidebarViewWrapper>
  );
};

export default compose(
  PostDetailConnect
)(PublicationPostDetailComponent) as React.ComponentClass<{}>;
