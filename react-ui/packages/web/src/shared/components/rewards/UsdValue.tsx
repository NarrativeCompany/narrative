import * as React from 'react';
import { SharedComponentMessages } from '../../i18n/SharedComponentMessages';
import { FormattedMessage } from 'react-intl';
import { Text, TextProps } from '../Text';
import styled from 'styled-components';
import { NrveUsdValue } from '@narrative/shared';

interface Props extends TextProps {
  nrveUsdValue: NrveUsdValue;
}

const UsdRawValue = styled<TextProps>(Text)`
  color: inherit;
  margin-right: 5px;
`;

export const UsdValue: React.SFC<Props> = (props) => {
  const { nrveUsdValue, ...textProps } = props;

  return (
    <Text color="lightGray" size="inherit" {...textProps}>
      <UsdRawValue>{nrveUsdValue.usd}</UsdRawValue><FormattedMessage {...SharedComponentMessages.USD}/>
    </Text>
  );
};
