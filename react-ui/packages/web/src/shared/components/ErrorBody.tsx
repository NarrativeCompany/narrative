import * as React from 'react';
import { FlexContainer, FlexContainerProps } from '../styled/shared/containers';
import { Heading, HeadingProps } from './Heading';
import { Paragraph } from './Paragraph';
import styled from '../styled';
import Icon from 'antd/lib/icon';

// tslint:disable no-var-requires
const robot = require('../../assets/gifs/robot.gif');
const newman = require('../../assets/gifs/newman.gif');
// tslint:enable no-var-requires

const animations = {
  robot,
  newman
};

const ErrorHeading =
  styled<HeadingProps & {titleType?: ErrorModalTitleType}>(({titleType, ...rest}) => <Heading {...rest}/>)`
      .anticon {
        display: none;
        margin-right: 10px;
      }
      
     ${props => props.titleType === 'error' && `
        color: ${props.theme.primaryRed};
        
        .anticon {
          display: inline-block;
        }
     `}
  `;

const GifWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin: 20px auto;
  max-height: 350px;
  max-width: 400px;
  
  img {
    width: 100%;
  }
`;

const ExtraMessageWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin-bottom: 15px;
`;

export type ErrorModalTitleType = 'default' | 'error';
export type ErrorModalGifType = 'robot' | 'newman';

export interface ErrorBodyParentProps {
  title: React.ReactNode;
  titleType?: ErrorModalTitleType;
  description: React.ReactNode;
  gifType: ErrorModalGifType;
  extraInfo?: React.ReactNode;
}

export const ErrorBody: React.SFC<ErrorBodyParentProps> = (props) => {
  const { title, titleType, description, gifType, extraInfo } = props;

  return (
    <React.Fragment>
      <ErrorHeading size={1} titleType={titleType}>
        <Icon type="exclamation-circle-o"/>
        {title}
      </ErrorHeading>

      <Heading size={3} weight={300} textAlign="center">{description}</Heading>

      <GifWrapper>
        <img src={animations[gifType]} alt={gifType}/>
      </GifWrapper>

      {extraInfo &&
      <ExtraMessageWrapper alignItems="center">
        <Paragraph size="large">{extraInfo}</Paragraph>
      </ExtraMessageWrapper>}
    </React.Fragment>
  );
};
