import * as React from 'react';
import { SectionHeader } from '../../../../shared/components/SectionHeader';
import styled from '../../../../shared/styled';

const SectionBody = styled.div`margin-bottom: 25px;`;

interface Props {
  title: React.ReactNode;
}

export const StatsSection: React.SFC<Props> = (props) => {
  const { title } = props;

  return (
    <React.Fragment>
      <SectionHeader title={title}/>

      <SectionBody>
        {props.children}
      </SectionBody>
    </React.Fragment>
  );
};
