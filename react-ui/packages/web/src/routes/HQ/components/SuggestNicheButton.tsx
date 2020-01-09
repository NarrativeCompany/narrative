import * as React from 'react';
import { compose } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { Button, ButtonProps } from '../../../shared/components/Button';
import { PermissionErrorModal } from '../../../shared/components/PermissionErrorModal';
import { RevokeReasonMessages } from '../../../shared/i18n/RevokeReasonMessages';
import { SuggestNicheButtonMessages } from '../../../shared/i18n/SuggestNicheButtonMessages';
import { WebRoute } from '../../../shared/constants/routes';
import {
  WithPermissionsModalControllerProps,
  withPermissionsModalController
} from '../../../shared/containers/withPermissionsModalController';
import styled from '../../../shared/styled';
import { withLoginModalHelpers, WithLoginModalHelpersProps } from '../../../shared/containers/withLoginModalHelpers';

const StyledButton = styled<ButtonProps>(Button)`
  min-width: 160px;
  
  @media screen and (max-width: 540px) {
    width: 100%;
  }
`;

type ParentProps = ButtonProps;

type Props =
  ParentProps &
  WithPermissionsModalControllerProps &
  WithLoginModalHelpersProps;

const SuggestNicheButtonComponent: React.SFC<Props> = (props) => {
  const { size, type, openLoginModal, handleShowPermissionsModal,
    granted, permissionErrorModalProps } = props;

  let btnProps: ButtonProps;
  if (openLoginModal) {
    btnProps = { onClick: openLoginModal };
  } else if (granted) {
    btnProps = { href: WebRoute.SuggestNiche };
  } else {
    btnProps = { onClick: handleShowPermissionsModal };
  }

  return (
    <React.Fragment>
      <StyledButton
        size={size || 'default'}
        type={type || 'primary'}
        {...btnProps}
      >
        <FormattedMessage {...SuggestNicheButtonMessages.BtnText}/>
      </StyledButton>
      {permissionErrorModalProps && <PermissionErrorModal {...permissionErrorModalProps} />}
    </React.Fragment>
  );
};

export const SuggestNicheButton = compose(
  withLoginModalHelpers,
  withPermissionsModalController(
    'suggestNiches',
    RevokeReasonMessages.SuggestNiche,
    RevokeReasonMessages.SuggestNicheTimeout
  )
)(SuggestNicheButtonComponent) as React.ComponentClass<ParentProps>;
