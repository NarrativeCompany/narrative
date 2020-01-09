import * as React from 'react';
import { Icon, Popover } from 'antd';
import { Card, CardColor, CardProps } from '../Card';
import { Heading } from '../Heading';
import { FlexContainer, FlexContainerProps } from '../../styled/shared/containers';
import { Niche } from '@narrative/shared';
import styled from '../../styled';

const NicheTagCard =
  styled<CardProps & {iconType?: NicheTagIconType}>(({iconType, ...rest}) => <Card {...rest}/>)`
    &.ant-card {
      margin: 0 16px 16px 0;
      min-width: 200px;
      ${p => p.iconType === 'plus' && `cursor: pointer`};
    }
    
    h4 {
      margin-right: 25px;
      max-width: 260px;
    }
    
    @media screen and (max-width: 575px) {
      &.ant-card {
        width: 100%;
        margin-right: 0;
      }
    }
  `;

const IconWrapper = styled<FlexContainerProps>(FlexContainer)`
  position: absolute;
  top: 0;
  right: 0;
  height: 40px;
  width: 40px;
  cursor: pointer;
  
  .anticon {
    font-size: 16px;
    color: #fff;
  }
`;

type NicheTagIconType = 'plus' | 'close';

interface ParentProps {
  niche: Niche;
  color: CardColor;
  iconType?: NicheTagIconType;
  onClick: (niche: Niche) => void;
}

export const NicheTag: React.SFC<ParentProps> = (props) => {
  const { niche, color, iconType, onClick } = props;

  return (
    <Popover content={niche.description} trigger="hover" placement="bottom" overlayStyle={{ maxWidth: 275 }}>
      <NicheTagCard
        color={color}
        bodyStyle={{ padding: 15 }}
        height={85}
        onClick={() => iconType === 'plus' && onClick(niche)}
        iconType={iconType}
      >
        <IconWrapper centerAll={true} onClick={() => iconType !== 'plus' && onClick(niche)}>
          <Icon type={iconType || 'close'}/>
        </IconWrapper>

        <Heading size={4} color="#fff" weight={400}>
          {niche.name}
        </Heading>
      </NicheTagCard>
    </Popover>
  );
};
