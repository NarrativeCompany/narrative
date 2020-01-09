import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { FlexContainerProps, FlexContainer } from '../../../shared/styled/shared/containers';
import { Text } from '../../../shared/components/Text';
import { MemberPostsMessages } from '../../../shared/i18n/MemberPostsMessages';
import styled from '../../../shared/styled';
import { PostProps } from './MemberPostsListItem';
import { PostDatetimeBullet } from './PostDatetimeBullet';
import { PostNichesBullet } from './PostNichesBullet';
import { PostChannelBullet } from './PostChannelBullet';

const InfoRowWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin-top: 5px;
`;

interface ParentProps extends PostProps {
  isPublished: boolean;
  isPending: boolean;
}

export const MemberPostsListItemInfo: React.SFC<ParentProps> = (props) => {
  const { post, isPublished, isPending } = props;

  const postStatusMessage = isPublished
    ? MemberPostsMessages.Published
    : isPending
      ? MemberPostsMessages.Pending
      : MemberPostsMessages.Draft;

  return (
    <InfoRowWrapper flexWrap="wrap">
      <Text color="warning">
        <FormattedMessage {...postStatusMessage}/>
      </Text>

      <PostDatetimeBullet post={post}/>

      <PostChannelBullet post={post}/>

      <PostNichesBullet post={post}/>
    </InfoRowWrapper>
  );
};
