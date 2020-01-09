import * as React from 'react';
import { Card } from '../Card';
import { FlexContainer, FlexContainerProps } from '../../styled/shared/containers';
import { PostAvatar } from '../post/PostAvatar';
import { PostProps } from '../contentStream/ContentStreamItem';
import { PostTitle } from '../post/PostTitle';
import { MemberAvatar } from '../user/MemberAvatar';
import { MemberLink } from '../user/MemberLink';
import styled from '../../styled';
import { TextSizes } from '../Text';

const StyledByline = styled<FlexContainerProps>(FlexContainer)`
  &, & a {
    ${TextSizes.small}
  }
`;

const StyledPostDetails = styled<FlexContainerProps>(FlexContainer)`
  h4, h4 a {
    font-size: 16px;
  }
`;

export const FeaturedPostSidebarItem: React.SFC<PostProps> = (props) => {
  const { post } = props;

  return (
    <Card style={{boxShadow: 'none', padding: 0}}>
      <FlexContainer alignItems="flex-start" justifyContent="space-between">
        <StyledPostDetails alignItems="flex-start" direction="column">
          <PostTitle post={post} size={4} style={{marginBottom: '.5em', flexGrow: 1}}/>
          <StyledByline alignItems="center">
            <MemberAvatar user={post.author} size={20} />
            <MemberLink user={post.author} color="dark" style={{marginLeft: 5}} />
          </StyledByline>
        </StyledPostDetails>
        <PostAvatar post={post} size={50} style={{marginLeft: 10}} />
      </FlexContainer>
    </Card>
  );
};
