import * as React from 'react';
import styled from 'styled-components';
import { FlexContainer, FlexContainerProps } from '../styled/shared/containers';
import { AnchorSizeType } from './Link';
import { SharedComponentMessages } from '../i18n/SharedComponentMessages';
import { FormattedMessage } from 'react-intl';
import { Text } from './Text';
import { Tooltip } from 'antd';

const BadgeWrapper = styled.span`
  display: inline-block; 
  line-height: initial;
  border-radius: 3px;
  background: ${p => p.theme.primaryRed};
`;

const BadgeInnerWrapper = styled<FlexContainerProps>(FlexContainer)`
  padding: 3px 3px;
`;

export interface RestrictedBadgeProps {
  style?: React.CSSProperties;
  inheritStyle?: boolean;
  size?: AnchorSizeType;
}

export const RestrictedBadge: React.SFC<RestrictedBadgeProps> = (props) => {
  const { style } = props;

  return (
    <Tooltip title={<FormattedMessage {...SharedComponentMessages.AgeRestrictedBadgeTooltip}/>}>
      <BadgeWrapper style={{...style}}>
        <BadgeInnerWrapper column={true} centerAll={true}>
          <Text color="white" style={{fontSize: '60%'}}>
            <FormattedMessage {...SharedComponentMessages.RestrictedMessageBadgeText}/>
          </Text>
        </BadgeInnerWrapper>
      </BadgeWrapper>
    </Tooltip>
  );
};
