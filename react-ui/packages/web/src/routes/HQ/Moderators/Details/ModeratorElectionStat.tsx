import * as React from 'react';
import { Heading } from '../../../../shared/components/Heading';
import { themeTypography } from '../../../../shared/styled/theme';
import { LabelAndValue } from '../../../../shared/components/LabelAndValue';
import { FormattedMessage } from 'react-intl';
import styled from '../../../../shared/styled';

const ElectionStatWrapper = styled.div`
  padding: 15px 50px;
  
  h1 {
    margin-bottom: 10px;
  }
`;

interface ParentProps {
  label: FormattedMessage.MessageDescriptor;
  value: number;
}

export const ModeratorElectionStat: React.SFC<ParentProps> = (props) => {
  const { label, value } = props;

  const Label = (
    <Heading size={6} color={themeTypography.textColorLight} uppercase={true} noMargin={true}>
      <FormattedMessage {...label}/>
    </Heading>
  );

  return (
    <ElectionStatWrapper>
      <LabelAndValue label={Label} direction="column-reverse">
        <Heading size={1} weight={600}>{value}</Heading>
      </LabelAndValue>
    </ElectionStatWrapper>
  );
};
