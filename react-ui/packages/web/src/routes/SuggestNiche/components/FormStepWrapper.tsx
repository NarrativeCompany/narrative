import * as React from 'react';
import { PageHeader } from '../../../shared/components/PageHeader';
import styled from '../../../shared/styled/index';

const Step = styled.div`
  margin-top: 25px;
`;

interface ParentProps {
  title: string | React.ReactNode;
  description: string | React.ReactNode;
  centerDescription?: boolean;
  children: React.ReactNode;
}

export const StepWrapper: React.SFC<ParentProps> = (props) => {
  const { title, description, centerDescription, children } = props;
  const center = centerDescription ? 'title-and-description' : 'title';

  return (
    <Step>
      <PageHeader
        title={title}
        description={description}
        center={center}
      />
      {children}
    </Step>
  );
};
