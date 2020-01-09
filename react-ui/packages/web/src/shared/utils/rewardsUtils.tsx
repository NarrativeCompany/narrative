import { UsdValue } from '../components/rewards/UsdValue';
import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { NrveUsdValue } from '@narrative/shared';
import { NrveValue } from '../components/rewards/NrveValue';
import styled from '../styled';
import { FlexContainer, FlexContainerProps } from '../styled/shared/containers';

const NrveWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin-right: 5px;
`;

export const getRewardsHeaderTitle = (
  descriptor: FormattedMessage.MessageDescriptor,
  loading: boolean,
  rewards: NrveUsdValue,
  showFullDecimal = false) => {
  let points;
  let usd;
  if (loading) {
    points = '';
    usd = '';
  } else {
    points = <NrveValue value={rewards} showFullDecimal={showFullDecimal}/>;
    usd = <UsdValue nrveUsdValue={rewards}/>;
  }

  return (
    <React.Fragment>
      <NrveWrapper>
        <FormattedMessage {...descriptor} values={{ points }}/>
      </NrveWrapper>
      {' '}
      {usd}
    </React.Fragment>
  );
};
