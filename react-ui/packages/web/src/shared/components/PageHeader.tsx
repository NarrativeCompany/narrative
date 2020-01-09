import * as React from 'react';
import { FlexContainer, FlexContainerProps } from '../styled/shared/containers';
import { Heading, HeadingSize } from './Heading';
import { Paragraph } from './Paragraph';
import { CustomIcon, IconType } from './CustomIcon';
import styled from '../../shared/styled';

const PageHeaderWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin-bottom: 25px;
`;

const TitleWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin-right: auto;
`;

const IconWrapper = styled.div`
  margin: 5px 10px 0 0;
  
  @media screen and (max-width: 767px) {
    display: none;
  }
`;

const TitleHelper = styled.span`
  align-self: flex-end;
`;

const TitleAndExtraWrapper =
  styled<
    FlexContainerProps &
    {center?: PageHeaderCenterType}
  >(({center, ...rest}) => <FlexContainer {...rest}/>)`
    ${props => shouldCenterTitle(props.center) && `
      align-self: center;
      
      h1, h2, h3, h4, h5, h6 {
       text-align: center;
      }
    `};
    h1 {
      margin-right: 12px;
    }
    
    @media screen and (max-width: 540px) {
      flex-direction: column;
    }
`;

const ExtraWrapperDesktop =
  styled<
    FlexContainerProps &
    {center?: PageHeaderCenterType}
  >(({center, ...rest}) => <FlexContainer {...rest}/>)`
    ${props => props.center && 'display: none;'}

    @media screen and (max-width: 540px) {
      display: none;
    }
  `;

const ExtraWrapperMobile =
  styled<
    FlexContainerProps &
    {center?: PageHeaderCenterType}
  >(({center, ...rest}) => <FlexContainer {...rest}/>)`
    display: none;
    ${props => props.center && `
      display: block;
      margin-top: 20px;
    `}
  
    @media screen and (max-width: 540px) {
      margin-top: 20px;
      display: block;
    }
  `;

const DescriptionWrapper =
  styled<
    FlexContainerProps &
    {center?: PageHeaderCenterType}
  >(({center, ...rest}) => <FlexContainer {...rest}/>)`
    ${props => shouldCenterDescription(props.center) && `
      align-self: center;
      text-align: center;
    `};
    margin-top: 12px;
    
    > span {
      color: ${props => props.theme.textColor};
      font-size: ${props => props.theme.textFontSizeDefault};
    }
  `;

function shouldCenterTitle (center?: PageHeaderCenterType) {
  return center === 'title' ||
    center === 'title-and-description' ||
    center === 'all';
}

function shouldCenterDescription (center?: PageHeaderCenterType) {
  return center === 'description' ||
    center === 'title-and-description' ||
    center === 'all';
}

export type PageHeaderCenterType = 'title' | 'description' | 'title-and-description' | 'all';
export type PageHeaderSize = 'small' | 'default' | number;

export interface PageHeaderProps {
  preTitle?: string | React.ReactNode;
  title: string | React.ReactNode;
  titleHelper?: React.ReactNode;
  description?: string | React.ReactNode;
  extra?: React.ReactNode;
  center?: PageHeaderCenterType;
  iconType?: IconType;
  size?: PageHeaderSize;
  style?: React.CSSProperties;
}

export const PageHeader: React.SFC<PageHeaderProps> = (props) => {
  const { preTitle, title, titleHelper, description, extra, center, iconType, size, style } = props;

  return (
    <PageHeaderWrapper column={true} centerAll={center === 'all'} style={style}>
      <FlexContainer>
        {iconType &&
        <IconWrapper>
          <CustomIcon type={iconType} size={50}/>
        </IconWrapper>}

        <FlexContainer column={true} style={{width: '100%'}} centerAll={center === 'all'}>
          {preTitle &&
          <Paragraph size="small" color="light" uppercase={true}>
            {preTitle}
          </Paragraph>}

          <TitleAndExtraWrapper center={center} flexWrap="wrap">
            <TitleWrapper alignItems="center" flexWrap="wrap">
              <Heading size={getPageHeaderSize(size)} weight={700} noMargin={true}>{title}</Heading>

              {titleHelper && <TitleHelper>{titleHelper}</TitleHelper>}
            </TitleWrapper>

            {extra &&
            <ExtraWrapperDesktop alignItems="center" center={center}>
              {extra}
            </ExtraWrapperDesktop>}
          </TitleAndExtraWrapper>

          <DescriptionWrapper center={center}>
            {description && <span>{description}</span>}
          </DescriptionWrapper>
        </FlexContainer>
      </FlexContainer>

      {extra &&
      <ExtraWrapperMobile alignItems="center" center={center}>
        {extra}
      </ExtraWrapperMobile>}
    </PageHeaderWrapper>
  );
};

function getPageHeaderSize (size?: PageHeaderSize): HeadingSize {
  if (typeof size === 'number') {
    return size as HeadingSize;
  }

  return size === 'small' ? 3 : 1;
}
