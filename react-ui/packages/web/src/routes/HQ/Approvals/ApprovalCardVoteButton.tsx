import * as React from 'react';
import { IconProps } from 'antd/lib/icon';
import { Icon } from 'antd';
import { VoteButton, VoteButtonProps } from '../../../shared/components/VoteButton';
import styled from '../../../shared/styled';

// tslint:disable-next-line no-any
const ButtonIcon = styled<IconProps & {theme: any}>(Icon)`
  font-size: 22px;
`;

interface Props extends VoteButtonProps {
  iconType: 'like' | 'dislike';
}

export const ApprovalCardVoteButton: React.SFC<Props> = (props) => {
  const { iconType, ...buttonProps } = props;

  return (
    <VoteButton {...buttonProps}>
      <ButtonIcon type={iconType}/>
    </VoteButton>
  );
};
