import * as React from 'react';
import { compose } from 'recompose';
import { Link } from '../../../shared/components/Link';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { FormattedMessage } from 'react-intl';
import { HeaderAuthButtonsMessages } from '../../../shared/i18n/HeaderAuthButtonsMessages';
import { ModalConnect, ModalName, ModalStoreProps } from '../../../shared/stores/ModalStore';
import { WebRoute } from '../../../shared/constants/routes';
import styled from '../../../shared/styled';
import { generatePath } from 'react-router';

const AuthButtonsWrapper = styled<FlexContainerProps>(FlexContainer)`
  a:first-child {
    margin-right: 25px;
    letter-spacing: 0.5px;
  }
  
  @media screen and (max-width: 767px) {
    a:first-child {
      display: none;
    }
  }
`;

type Props =
  ModalStoreProps;

const HeaderAuthButtonsComponent: React.SFC<Props> = (props) => {
  const { modalStoreActions } = props;

  return (
    <AuthButtonsWrapper justifyContent="flex-end">
      <Link color="light" to={generatePath(WebRoute.Register)}>
        <FormattedMessage {...HeaderAuthButtonsMessages.Register}/>
      </Link>

      <Link.Anchor color="light" onClick={() => modalStoreActions.updateModalVisibility(ModalName.login)}>
        <FormattedMessage {...HeaderAuthButtonsMessages.SignIn}/>
      </Link.Anchor>
    </AuthButtonsWrapper>
  );
};

export const HeaderAuthButtons = compose(
  ModalConnect(ModalName.login)
)(HeaderAuthButtonsComponent) as React.ComponentClass<{}>;
