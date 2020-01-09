import * as React from 'react';
import styled from '../styled';
import { SectionHeader, SectionHeaderProps } from './SectionHeader';

export const distanceBetweenSections = 40;

const SectionWrapper = styled.div`
  margin-bottom: ${distanceBetweenSections}px;
`;

export const Section: React.SFC<SectionHeaderProps> = (props) => {
  const { children, ...headerProps } = props;

  return (
    <SectionWrapper>
      <SectionHeader {...headerProps} />

      {children}
    </SectionWrapper>
  );
};
