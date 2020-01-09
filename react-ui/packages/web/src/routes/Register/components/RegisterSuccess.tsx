import * as React from 'react';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { Heading } from '../../../shared/components/Heading';
import { Paragraph } from '../../../shared/components/Paragraph';
import { Button } from '../../../shared/components/Button';
import { FormattedMessage } from 'react-intl';
import { RegisterMessages } from '../../../shared/i18n/RegisterMessages';
import { WebRoute } from '../../../shared/constants/routes';
import almostThere from '../../../assets/graphic-almost-there.svg';
import styled from '../../../shared/styled';

const RegisterSuccessWrapper = styled<FlexContainerProps>(FlexContainer)`
  padding-top: 80px;
  
  p {
    max-width: 600px;
    text-align: center;
  }
  
  img {
    margin: 50px 0;
  }
`;

interface ParentProps {
  userEmailAddress: string;
}

export const RegisterSuccess: React.SFC<ParentProps> = (props) => {
  const { userEmailAddress } = props;

  const userEmail = <strong>{userEmailAddress}</strong>;

  return (
    <RegisterSuccessWrapper column={true} alignItems="center">
      <Heading size={1}>
        <FormattedMessage {...RegisterMessages.RegisterSuccessTitle}/>
      </Heading>

      <Paragraph size="large">
        <FormattedMessage {...RegisterMessages.RegisterSuccessDescription} values={{userEmail}}/>
      </Paragraph>

      <img src={almostThere}/>

      <Button type="ghost" size="large" href={WebRoute.Home}>
        <FormattedMessage {...RegisterMessages.RegisterSuccessBtnText}/>
      </Button>
    </RegisterSuccessWrapper>
  );
};
