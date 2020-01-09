import * as React from 'react';
import styled from '../../styled';
import { Tag, TagProps } from '../Tag';
import { FormattedMessage } from 'react-intl';
import { PostDetailMessages } from '../../i18n/PostDetailMessages';

const StyledTag = styled<TagProps>(Tag)`
  &.ant-tag {
    margin-left: 15px;
    display: inline-block;
    vertical-align: top;
  }
`;

export const LiveOnNetworkTag: React.SFC<{}> = () => {
  return (
    <StyledTag color="green" size="normal" notLinked={true}>
      <FormattedMessage {...PostDetailMessages.LiveOnNetwork}/>
    </StyledTag>
  );
};
