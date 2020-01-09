import * as React from 'react';
import styled from '../../styled';
import { SectionHeader, SectionHeaderProps } from '../SectionHeader';

// jw: this component is incredibly basic, but it serves two important purposes:
//     1) it ensures that each section is structured similarly with a SectionHeader at the top.
//     2) it guarantees each section spaced properly

const SectionContainer = styled.div`
  &:not(:last-child) {
    margin-bottom: 50px;
  }
`;

type Props = Pick<SectionHeaderProps, 'title' | 'extra' | 'description'>;

export const ChannelDetailsSection: React.SFC<Props> = (props) => {
  const { children, ...headerProps } = props;

  return (
    <SectionContainer>
      <SectionHeader {...headerProps}/>

      {children}
    </SectionContainer>
  );
};
