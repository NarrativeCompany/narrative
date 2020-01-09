import * as React from 'react';
import { FlexContainer, FlexContainerProps } from '../styled/shared/containers';
import { Paragraph } from './Paragraph';
import { FormattedMessage } from 'react-intl';
import styled from '../styled';
import whyGuy from '../../assets/gifs/why-guy-anim.gif';
import { NotFoundMessages } from '../i18n/NotFoundMessages';
import { SEO } from './SEO';
import { compose } from 'recompose';

const ImgWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin: 30px 0;
  max-width: 680px;
  width: 100%;
  height: 100%;
  
  img {
    width: 100%;
    height: 100%;
  }
`;

interface ParentProps {
  title?: string;
  message?: React.ReactNode;
  statusCode?: number;
}

type Props = ParentProps;

const NotFoundComponent: React.SFC<Props> = (props) => {
  const { title, message, statusCode } = props;
  const notFoundMessage = message || <FormattedMessage {...NotFoundMessages.DefaultMessage}/>;

  return (
    <FlexContainer column={true} centerAll={true}>
      <SEO
        title={title || NotFoundMessages.Title}
        statusCode={statusCode || 404}
      />
      <ImgWrapper>
        <img src={whyGuy}/>
      </ImgWrapper>

      {typeof message === 'string' ?
      <Paragraph color="light" size="large">
        {notFoundMessage}
      </Paragraph> :
      <React.Fragment>
        {notFoundMessage}
      </React.Fragment>}
    </FlexContainer>
  );
};

export const NotFound = compose(
)(NotFoundComponent) as React.ComponentClass<ParentProps>;
