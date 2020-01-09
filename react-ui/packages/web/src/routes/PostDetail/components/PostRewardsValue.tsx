import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { PostDetailMessages } from '../../../shared/i18n/PostDetailMessages';
import { Block } from '../../../shared/components/Block';
import { Paragraph } from '../../../shared/components/Paragraph';
import { Link } from '../../../shared/components/Link';
import { NrveUsdValue } from '@narrative/shared';
import styled from 'styled-components';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { NrveValue } from '../../../shared/components/rewards/NrveValue';
import { UsdValue } from '../../../shared/components/rewards/UsdValue';

interface ParentProps {
  allTimePostRewards: NrveUsdValue;
}

const RewardsRowWrapper = styled<FlexContainerProps>(FlexContainer)`
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  font-size: 18px;
  margin-bottom: 10px;
`;

const RewardsCell = styled<FlexContainerProps>(FlexContainer)`
  flex-direction: row;
`;

export const PostRewardsValue: React.SFC<ParentProps> = (props) => {
  const { allTimePostRewards } = props;
  const narrativeRewardsLink = <Link.About type="rewards"/>;
  const nrveLink = <Link.About type="nrve"/>;
  const points = <NrveValue value={allTimePostRewards}/>;
  return (
    <Block>
      <RewardsRowWrapper>
        <RewardsCell>
          <FormattedMessage {...PostDetailMessages.Points} values={{points}}/>
        </RewardsCell>
        <RewardsCell>
          <UsdValue nrveUsdValue={allTimePostRewards}/>
        </RewardsCell>
      </RewardsRowWrapper>
      <Paragraph>
        <FormattedMessage
          {...PostDetailMessages.NarrativeRewardsForThisPost}
          values={{narrativeRewardsLink, nrveLink}}
        />
      </Paragraph>
    </Block>
  );
};
