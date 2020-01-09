import * as React from 'react';
import { Card as StyledCard } from '../../../shared/components/Card';
import { CardProps } from '../../../shared/components/Card';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import styled from '../../../shared/styled/index';

const Card = styled<Props>((props) => <StyledCard {...props}/>)`
  &.ant-card {
    margin-bottom: 15px;
  }
  
  .ant-card-cover {
   height: 20px;
  }
  
  .ant-card-actions {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    border-top: none;
  }
  
  .ant-card-actions > li {
    height: 44px;
    margin: 0;
    border-right: none !important;
  }
  
  .ant-card-actions > li > span {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100%;
  }
  
  .ant-card-body {
    height: ${props => props.actions ? '85%' : '100%'};
  }
`;

const CardBody = styled<FlexContainerProps>(FlexContainer)`
  height: 100%;
`;

type Props =
  CardProps;

export const NicheCard: React.SFC<Props> = (props) => {
  const {children, cover, actions, height} = props;

  return (
    <Card
      bodyStyle={{padding: 20}}
      cover={cover}
      actions={actions}
      height={height}
    >
      <CardBody centerAll={true} column={true}>
        {children}
      </CardBody>
    </Card>
  );
};
