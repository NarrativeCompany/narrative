import * as React from 'react';
import { Card } from './Card';
import styled from '../styled';

// jw: I halved all of the antd default paddings.
const StyledCard = styled(Card)`
  .ant-card-head {
    padding: 0 12px;
  }
  .ant-card-head-title {
    padding: 8px 0;
  }
  .ant-card-body {
    padding: 12px;
  }
`;

interface Props {
  loading?: boolean;
  title: string | React.ReactNode;
}

export const SidebarCard: React.SFC<Props> = (props) => {
  return (
    <StyledCard {...props} style={{marginBottom: '15px'}} />
  );
};
