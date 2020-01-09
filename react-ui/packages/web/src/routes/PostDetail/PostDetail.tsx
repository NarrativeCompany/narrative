import * as React from 'react';
import { branch, compose, lifecycle, renderComponent, withProps } from 'recompose';
import { NotFound } from '../../shared/components/NotFound';
import { LayoutBgConnect, LayoutBgStoreProps } from '../../shared/stores/LayoutBgStore';
import {
  includeScript,
  withPostById,
  WithPostByIdProps
} from '@narrative/shared';
import { convertUrlIdToIdForApi, getIdForApi, IdRouteProps } from '../../shared/utils/routeUtils';
import { PostDetailContext } from './components/PostDetailContext';
import PostDetailBody from './PostDetailBody';
import PublicationLayout from '../Publication/PublicationLayout';
import { SidebarViewWrapper } from '../../shared/components/SidebarViewWrapper';
import { PostSidebar } from './components/PostSidebar';
import { LoadingViewWrapper } from '../../shared/components/LoadingViewWrapper';

// tslint:disable-next-line no-any
declare var embedly: any;

export const PostDetail: React.SFC<WithPostByIdProps> = (props) => {
  const { post: { publishedToPublication, oid, prettyUrlString } } = props;

  return (
    <PostDetailContext.Provider value={{postByIdProps: {...props}}}>
      {publishedToPublication
        ? <PublicationLayout
            publicationId={getIdForApi(publishedToPublication.prettyUrlString)}
            postId={prettyUrlString ? prettyUrlString : `_${oid}`}
        />
        : <SidebarViewWrapper
            style={{paddingTop: 36} }
            sidebarItems={<PostSidebar {...props}/>}
          >
            <PostDetailBody />
        </SidebarViewWrapper>
      }
    </PostDetailContext.Provider>
  );
};

export default compose(
  LayoutBgConnect,
  lifecycle<LayoutBgStoreProps, {}>({
    // tslint:disable object-literal-shorthand
    componentWillMount: function () {
      includeScript('https://cdn.embedly.com/widgets/platform.js', () => {
        embedly('defaults', {
          cards: {
            override: true,
            controls: '0'
          }
        });
      });
    }
    // tslint:enable object-literal-shorthand
  }),
  withProps((props: IdRouteProps) => {
    const postId = convertUrlIdToIdForApi(props);

    return { postId };
  }),
  withPostById,
  branch((props: WithPostByIdProps) => props.postDetailLoading,
    // jw: since this is a root level component lets use the LoadingViewWrapper so that we get the gradient and all
    //     other ViewWrapper features along with the loading indicator.
    renderComponent(() => <LoadingViewWrapper/>)
  ),
  branch((props: WithPostByIdProps) => !props.post,
    renderComponent(() => <NotFound/>)
  ),
  branch<WithPostByIdProps>(({post}) => !post.publishedToPublication,
    // tslint:disable object-literal-shorthand
    lifecycle<LayoutBgStoreProps, {}>({
      componentWillMount: function () {
        this.props.changeLayoutBg('white');
      },
      componentWillUnmount: function () {
        this.props.changeLayoutBg();
      }
    })
    // tslint:enable object-literal-shorthand
  )
)(PostDetail) as React.ComponentClass<{}>;
