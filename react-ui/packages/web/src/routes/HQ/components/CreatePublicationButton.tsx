import * as React from 'react';
import { compose } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { Button, ButtonProps } from '../../../shared/components/Button';
import { CreatePublicationMessages } from '../../../shared/i18n/CreatePublicationMessages';
import { WebRoute } from '../../../shared/constants/routes';
import styled from '../../../shared/styled';
import { RevokeReasonMessages } from '../../../shared/i18n/RevokeReasonMessages';
import { withLoginModalHelpers, WithLoginModalHelpersProps } from '../../../shared/containers/withLoginModalHelpers';
import {
  withPermissionsModalController,
  WithPermissionsModalControllerProps
} from '../../../shared/containers/withPermissionsModalController';
import { PermissionErrorModal } from '../../../shared/components/PermissionErrorModal';

const StyledButton = styled<ButtonProps>(Button)`
  min-width: 160px;
  &.ant-btn
  {
    margin-left:14px; 
  }
  
  @media screen and (max-width: 540px) {
    width: 100%;
    &.ant-btn
    {
      margin-top: 14px;
      margin-left: 0;
    }
  }
`;

interface ParentProps extends ButtonProps {
  btnText?: FormattedMessage.MessageDescriptor;
}

type Props =
  ParentProps &
  WithPermissionsModalControllerProps &
  WithLoginModalHelpersProps;

const CreatePublicationComponent: React.SFC<Props> = (props) => {
  const { size, type, btnText, openLoginModal, granted, permissionErrorModalProps, handleShowPermissionsModal } = props;
  let btnProps: ButtonProps;
  if (openLoginModal) {
    btnProps = { onClick: openLoginModal };

  } else if (!granted) {
    btnProps = { onClick: handleShowPermissionsModal };

  } else {
    btnProps = { href: WebRoute.CreatePublication };
  }

  return (
    <React.Fragment>
      <StyledButton
        size={size || 'default'}
        type={type || 'primary'}
        {...btnProps}
      >
        <FormattedMessage {...(btnText || CreatePublicationMessages.BtnText)}/>
      </StyledButton>
      {permissionErrorModalProps && <PermissionErrorModal {...permissionErrorModalProps} />}
    </React.Fragment>
  );
};

export const CreatePublicationButton = compose(
  withLoginModalHelpers,
  withPermissionsModalController('createPublications', RevokeReasonMessages.CreatePublication)
)(CreatePublicationComponent) as React.ComponentClass<ParentProps>;
