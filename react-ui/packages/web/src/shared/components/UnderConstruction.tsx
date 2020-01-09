import * as React from 'react';
import { FlexContainer, FlexContainerProps } from '../styled/shared/containers';
import { FormattedMessage } from 'react-intl';
import { SharedComponentMessages } from '../i18n/SharedComponentMessages';
import styled from '../styled';
import { Heading } from './Heading';

// tslint:disable no-var-requires
const underConstruction = require('../../assets/gifs/under-construction.gif');

const InnerWrapper = styled<FlexContainerProps>(FlexContainer)`
  max-width: 680px;
  width: 100%;
  text-align: center;
  position: relative;
  
  h3 {
    position: absolute;
    top: 0;
  }
`;

interface ParentProps {
  message?: string;
}

export const UnderConstruction: React.SFC<ParentProps> = (props) => {
  const { message } = props;

  return (
    <FlexContainer column={true} centerAll={true}>
      <InnerWrapper centerAll={true} column={true}>
        <Heading size={3} weight={300} textAlign="center">
          {message || <FormattedMessage {...SharedComponentMessages.UnderConstruction}/>}
        </Heading>

        <img src={underConstruction} alt=""/>
      </InnerWrapper>
    </FlexContainer>
  );
};
