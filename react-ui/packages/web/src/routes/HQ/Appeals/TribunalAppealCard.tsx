import * as React from 'react';
import styled from '../../../shared/styled/index';
import { Card, CardProps } from '../../../shared/components/Card';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';

// this class is largely taken from NicheCard with a max-width set and body padding removed
const TribunalCard = styled<Props>(({autoHeight, ...rest}) => <Card {...rest}/>)`
  &.ant-card {
    height: ${props => props.autoHeight ? 'auto' : '300px'};
    // max-width: 1026px;
  }
`;

const CardBody = styled<FlexContainerProps>(FlexContainer)`
  height: 100%;
`;

interface ParentProps {
  autoHeight?: boolean;
}

type Props =
  ParentProps &
  CardProps;

export const TribunalAppealCard: React.SFC<Props> = (props) => {
  const { children, cover, actions } = props;

  // set padding to 0 here so the actual body component can touch the card right and left edges
  return (
    <TribunalCard
      bodyStyle={{padding: 0}}
      hoverable={true}
      cover={cover}
      actions={actions}
      autoHeight={true}
    >
      <CardBody column={true}>
        {children}
      </CardBody>
    </TribunalCard>
  );
};
