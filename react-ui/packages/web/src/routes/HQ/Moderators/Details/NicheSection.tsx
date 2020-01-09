import * as React from 'react';
import { Card } from '../../../../shared/components/Card';
import { Niche } from '@narrative/shared';
import styled from '../../../../shared/styled';

const NicheSectionWrapper = styled.div`
  margin: 40px 0 0;
`;

interface ParentProps {
  niche: Niche;
}

export const NicheSection: React.SFC<ParentProps> = (props) => {
  const { niche } = props;

  if (!niche) {
    return null;
  }

  return (
    <NicheSectionWrapper>
      {/* Commenting this out for the time being - we may replace this text with something else */}
      {/*<NicheDescriptionWrapper>*/}
        {/*<Paragraph size="large">*/}
          {/*Please select <strong>up to to 4 candidates</strong> to moderate the following niche:*/}
        {/*</Paragraph>*/}
      {/*</NicheDescriptionWrapper>*/}

      <Card.Channel channel={niche} titleSize={3} />
    </NicheSectionWrapper>
  );
};
