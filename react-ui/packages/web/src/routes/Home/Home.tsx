import * as React from 'react';
import { branch, compose, renderComponent } from 'recompose';
import {
  withExtractedCurrentUser,
  WithExtractedCurrentUserProps
} from '../../shared/containers/withExtractedCurrentUser';
import { Discover } from '../Discover/Discover';
import { MyStream } from './components/MyStream';
import { Loading } from '../../shared/components/Loading';
import { JoinCommunityCtaCarousel } from './components/JoinCommunityCtaCarousel';

export const Home: React.SFC<WithExtractedCurrentUserProps> = (props) => {
  if (props.currentUser) {
    return <MyStream />;
  }
  // jw: since this is the discover page for a guest, let's include the JoinCommunityBlock
  return <Discover banner={<JoinCommunityCtaCarousel/>} />;
};

export default compose(
  withExtractedCurrentUser,
  branch((props: WithExtractedCurrentUserProps) => !!props.currentUserLoading,
    renderComponent(() => <Loading />)
  )
)(Home);
