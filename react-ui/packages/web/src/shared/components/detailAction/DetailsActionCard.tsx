import * as React from 'react';
import * as H from 'history';
import styled, { css } from '../../styled';
import { FlexContainer, FlexContainerProps } from '../../styled/shared/containers';
import { Link } from '../Link';
import { Heading } from '../Heading';
import { Icon } from 'antd';
import { CustomIcon, IconType } from '../CustomIcon';
import { HighlightedCard, HighlightColor, HighlightedCardStyleProps } from '../HighlightedCard';

const innerPadding = css`
  padding: 10px;
`;

const ActionCard = styled<HighlightedCardStyleProps>(HighlightedCard)`
  &.ant-card {
    margin-bottom: 20px;
  
    .ant-card-body {
      padding: 0;
    }
  }
`;

const HeaderRow = styled<FlexContainerProps>(FlexContainer)`
  ${innerPadding}
`;

const IconColumn = styled.div`
  color: ${props => props.theme.textColorLight};
  width: 40px;
  min-width: 40px;
  text-align: center;
`;

const TitleColumn = styled.div`
  width: 100%;
  text-align: left;
`;

const ExtraColumn = styled.div`
  width: 80px;
  text-align: center;
`;

const CountdownColumn = styled.div`
  min-width: 150px;
  text-align: left;
`;

const BodyContainer = styled.div`
  border-top: solid 1px ${props => props.theme.borderGrey};
  ${innerPadding}
`;

interface Props {
  sideColor?: HighlightColor;
  icon?: IconType;
  title: string | React.ReactNode;
  countDown: React.ReactNode | null;
  toDetails?: H.LocationDescriptor;
  extraHeaderInfo?: React.ReactNode;
  footerText?: React.ReactNode;
}

export const DetailsActionCard: React.SFC<Props> = (props) => {
  const { icon, title, countDown, toDetails, extraHeaderInfo, footerText, sideColor } = props;

  const headerRow = (
    <HeaderRow alignItems="center">
      {icon && <IconColumn><CustomIcon type={icon} size="sm"/></IconColumn>}

      <TitleColumn>
        <Heading size={4} weight={300} noMargin={true}>{title}</Heading>
      </TitleColumn>

      {extraHeaderInfo && <ExtraColumn>{extraHeaderInfo}</ExtraColumn>}

      {countDown && <CountdownColumn>{countDown}</CountdownColumn>}

      {/* jw: if we are linking to a details page, include the right caret icon */}
      {toDetails &&
        <IconColumn>
          <Icon type="right" style={{fontSize: '24px'}} />
        </IconColumn>}
    </HeaderRow>
  );

  return (
    <ActionCard highlightColor={sideColor}>
      {/* fc: wrapping this with the shared Link won't cause any text style changes */}
      {toDetails
        ? <Link to={toDetails}>{headerRow}</Link>
        : headerRow}

      {props.children && <BodyContainer>{props.children}</BodyContainer>}

      {footerText && toDetails &&
      <BodyContainer style={{textAlign: 'center'}}>
        <Link to={toDetails}>{footerText}</Link>
      </BodyContainer>}
    </ActionCard>
  );
};
