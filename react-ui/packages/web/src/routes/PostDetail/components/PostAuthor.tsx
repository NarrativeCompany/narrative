import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { SectionHeader } from '../../../shared/components/SectionHeader';
import { Card } from '../../../shared/components/Card';
import { PostDetailMessages } from '../../../shared/i18n/PostDetailMessages';
import { User } from '@narrative/shared';
import styled from '../../../shared/styled';

const PostAuthorWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin-bottom: 50px;
`;

interface ParentProps {
  author: User;
}

export const PostAuthor: React.SFC<ParentProps> = (props) => {
  const { author } = props;

  return (
    <PostAuthorWrapper column={true}>
      <SectionHeader title={<FormattedMessage {...PostDetailMessages.AuthorSectionTitle}/>}/>
      <Card.User user={author} cardProps={{noBoxShadow: true }}/>
    </PostAuthorWrapper>
  );
};
