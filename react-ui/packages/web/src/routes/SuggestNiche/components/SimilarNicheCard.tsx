import * as React from 'react';
import { Card, CardProps } from '../../../shared/components/Card';
import { Heading } from '../../../shared/components/Heading';
import { Paragraph } from '../../../shared/components/Paragraph';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { Niche } from '@narrative/shared';
import styled from '../../../shared/styled';

const NicheCard = styled<CardProps>(Card)`
  margin: 0 4px 25px !important;
`;

const CardContentWrapper = styled<FlexContainerProps>(FlexContainer)`
  h3 {
    margin-bottom: 10px;
  }
`;

interface ParentProps {
  niche: Niche;
}

export const SimilarNicheCard: React.SFC<ParentProps> = (props) => {
  const { niche } = props;

  return (
    <NicheCard>
      <CardContentWrapper column={true}>
        <Heading size={3}>{niche.name}</Heading>

        <Paragraph>{niche.description}</Paragraph>
      </CardContentWrapper>
    </NicheCard>
  );
};
