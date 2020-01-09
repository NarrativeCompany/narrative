import * as React from 'react';
import { Card, CardProps } from '../Card';
import styled from '../../styled';
import { Text } from '../Text';
import { FormattedMessage } from 'react-intl';
import { SharedComponentMessages } from '../../i18n/SharedComponentMessages';
import { generateDummyData } from '../../utils/loadingUtils';

export function generateOpenPositionPlaceholders(slots: number): React.ReactNode | null {
  if (slots <= 0) {
    // jw:todo:error-handling: Why is someone passing such a nonsensical input?
    return null;
  }

  const dummyData = generateDummyData(slots);

  return (
    <React.Fragment>
      {dummyData.map((index) => <OpenModeratorPositionPlaceholder key={index} />)}
    </React.Fragment>
  );
}

const StyledCard = styled<CardProps>(Card)`
  &.ant-card {
    text-align: center;
    margin-bottom: 15px;
    
    .ant-card-body {
      padding: 10px 15px;
    }
  }
`;

export const OpenModeratorPositionPlaceholder: React.SFC<{}> = () => {
  return (
    <StyledCard noBoxShadow={true}>
      <Text color="dark"><FormattedMessage {...SharedComponentMessages.OpenPosition}/></Text>
    </StyledCard>
  );
};
