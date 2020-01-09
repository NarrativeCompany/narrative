import * as React from 'react';
import { Tooltip } from 'antd';
import { UsdValue } from './UsdValue';
import styled from 'styled-components';
import { Text, TextProps } from '../Text';
import { Block } from '../Block';
import { NrveUsdValue } from '@narrative/shared';

interface Props {
  nrveUsdValue: NrveUsdValue;
}

const NrveWrapper = styled<TextProps>(Text)`
  font-size: inherit;
  margin-left: 5px;
`;

export const UsdAndNrveValue: React.SFC<Props> = (props) => {
  const { nrveUsdValue } = props;

  if (!nrveUsdValue || nrveUsdValue.nrve === '0') {
    return <React.Fragment>0</React.Fragment>;
  }

  const usd = <UsdValue nrveUsdValue={nrveUsdValue}/>;

  let nrveValue;
  if (nrveUsdValue.nrve === nrveUsdValue.nrveRounded) {
    nrveValue = <React.Fragment>{nrveUsdValue.nrve}</React.Fragment>;
  } else {
    nrveValue = (
      <Tooltip title={nrveUsdValue.nrve}>
        {nrveUsdValue.nrveRounded}
      </Tooltip>
    );
  }

  const nrve = (
    <NrveWrapper>
      {nrveValue}
    </NrveWrapper>
  );

  return (
    <Block style={{fontSize: 'inherit', textAlign: 'right'}}>
      {usd}
      {' '}
      {nrve}
    </Block>
  );
};
