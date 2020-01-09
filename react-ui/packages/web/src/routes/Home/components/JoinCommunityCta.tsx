import * as React from 'react';
import styled from 'styled-components';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { HomeMessages } from '../../../shared/i18n/HomeMessages';
import { FormattedMessage } from 'react-intl';
import { Heading, HeadingProps } from '../../../shared/components/Heading';
import { Button } from '../../../shared/components/Button';
import { WebRoute } from '../../../shared/constants/routes';
import { generatePath } from 'react-router';
import { CustomIcon, IconType } from '../../../shared/components/CustomIcon';
import { mediaQuery } from '../../../shared/styled/utils/mediaQuery';
import { externalUrls } from '../../../shared/constants/externalUrls';

interface CtaContainerProps extends FlexContainerProps {
  bgColor: string;
}

const CtaContainer = styled<CtaContainerProps>(({bgColor, ...rest}) => <FlexContainer {...rest}/>)`
  position: relative;
  width: 100%;
  background-color: ${p => p.bgColor};
  border-radius: 6px;
  padding: 40px 20px;

  ${mediaQuery.lg_up`
    padding-left: 230px;
  `};
  
  h2 {
    font-size: 36px;
    line-height: 42px;
  }
  
  ${mediaQuery.sm_down`
    padding: 20px;
    padding-bottom: 30px;
  
    h2, h4 {
      span {
        font-size: 90%;
      }
    }
  `};
  
  ${mediaQuery.xs`
    h2 span {
      font-size: 75%;
      line-height: 75%;
    }
  `};
`;

const Headline = styled<HeadingProps>(Heading)`
  margin-bottom: 15px;
  font-weight: normal;
`;

const Message = styled<HeadingProps>(Heading)`
  min-height: 175px;
  font-weight: normal;
  
  ${mediaQuery.md_down`
      min-height: 150px;
  `};
`;

const IconContainer = styled<FlexContainerProps>(FlexContainer)`
  ${mediaQuery.lg_up`
    img {
      width: 200px !important;
      height: 200px !important;
    }
    
    position: absolute;
    top: 0;
    left: 0;
    right: calc(100% - 250px);
    bottom: 0;
  `};
  ${mediaQuery.xs`
    img {
      width: 125px !important;
      height: 125px !important;
    }
  `};
`;

const ButtonsContainer = styled<FlexContainerProps>(FlexContainer)`
  > a:not(:last-child) {
    margin-right: 20px;
  }
  ${mediaQuery.md_down`
    justify-content: center;
  `};
  
  ${mediaQuery.xs`
    > a:not(:last-child) {
      margin: 0;
      margin-bottom: 15px;
    }
    flex-direction: column;
    align-items: center;
  `};
`;

interface Props {
  message: FormattedMessage.MessageDescriptor;
  iconType: IconType;
  backgroundColor: string;
  headlineColor: string;
  messageColor: string;
}

export const JoinCommunityCta: React.SFC<Props> = (props) => {
  const { message, iconType, headlineColor, messageColor, backgroundColor } = props;

  return (
    <CtaContainer column={true} bgColor={backgroundColor}>
      <IconContainer centerAll={true}>
        <CustomIcon type={iconType} size={150}/>
      </IconContainer>
      <Headline size={4} color={headlineColor} uppercase={true}>
        <FormattedMessage {...HomeMessages.WelcomeToTheWorldsJournal} />
      </Headline>
      <Message size={2} color={messageColor}>
        <FormattedMessage {...message} />
      </Message>
      <ButtonsContainer>
        <Button size="large" href={generatePath(WebRoute.Register)} type="primary">
          <FormattedMessage {...HomeMessages.BecomeANarrator} />
        </Button>
        <Button size="large" href={externalUrls.narrativeAboutWebsite} type="ghost-grey" target="_blank">
          <FormattedMessage {...HomeMessages.LearnMore} />
        </Button>
      </ButtonsContainer>
    </CtaContainer>
  );
};
