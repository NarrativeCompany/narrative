import * as React from 'react';
import styled from '../styled';
import { SectionHeader } from './SectionHeader';

interface Props {
  title?: string | React.ReactNode;
}

const SummaryGridContainer = styled.div`
  & > div:nth-child(even) {
    background-color: #F9FAFB;
  }
`;

const SummaryGridSection = styled.div`
  margin-bottom: 20px;
`;

export const SummaryGrid: React.SFC<Props> = (props) => {
  const { title } = props;

  return (
    <SummaryGridSection>
      {title && <SectionHeader title={title}/>}

      <SummaryGridContainer>
        {props.children}
      </SummaryGridContainer>
    </SummaryGridSection>
  );
};
