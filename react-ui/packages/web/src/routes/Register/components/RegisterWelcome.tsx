import * as React from 'react';
import { Heading } from '../../../shared/components/Heading';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { FormattedMessage } from 'react-intl';
import { RegisterMessages } from '../../../shared/i18n/RegisterMessages';
import whiteLogo from '../../../assets/narrative-logo-white.svg';
import largeLogo from '../../../assets/logo-large-full.svg';
import styled from '../../../shared/styled';

const BlueBar = styled.div`
  height: 100%;
  background: linear-gradient(${props => props.theme.primaryBlue}, ${props => props.theme.secondaryBlue});
  min-height: 100%;
  position: relative;
  
  img {
    position: absolute;
    bottom: 0;
  }
`;

const WelcomeHeadingWrapper = styled<FlexContainerProps>(FlexContainer)`
  padding-top: 150px;

  img {
    position: static;
  }
  
  h1 {
    font-size: 64px;
    margin-top: 30px;
  }
  
  @media screen and (max-width: 995px) {
    h1 {
      font-size: 50px;
    }
  }
`;

export const RegisterWelcome: React.SFC<{}> = () => {
  return (
    <BlueBar>
      <WelcomeHeadingWrapper centerAll={true} column={true}>
        <img src={whiteLogo}/>
        <Heading size={1} color="#fff">
          <FormattedMessage {...RegisterMessages.RegisterWelcomeTitle}/>
        </Heading>
      </WelcomeHeadingWrapper>

      <img src={largeLogo}/>
    </BlueBar>
  );
};
