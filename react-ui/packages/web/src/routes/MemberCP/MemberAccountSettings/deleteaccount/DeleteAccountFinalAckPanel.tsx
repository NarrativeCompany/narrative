import { DialogHeader } from '../DialogHeader';
import { FormattedMessage, injectIntl, InjectedIntlProps } from 'react-intl';
import { DeleteAccountMessages } from '../../../../shared/i18n/DeleteAccountMessages';
import * as React from 'react';
import { AuthHeader } from '../../../../shared/styled/shared/auth';
import { ContinueCancelButtons } from '../ContinueCancelButtons';
import { MethodError } from '@narrative/shared';
import { SharedComponentMessages } from '../../../../shared/i18n/SharedComponentMessages';
import { FlexContainer } from '../../../../shared/styled/shared/containers';
import { InputField } from '../../../../shared/components/InputField';
import { convertInputFieldAddon } from '../../../../shared/utils/convertInputAddon';
import { Icon, Popover } from 'antd';
import { LoginMessages } from '../../../../shared/i18n/LoginMessages';
import { TwoFactorAuthenticationCodeInput } from '../../../../shared/components/TwoFactorAuthenticationCodeInput';
import { compose } from 'recompose';
import { Link } from '../../../../shared/components/Link';
import { externalUrls } from '../../../../shared/constants/externalUrls';
import { Enable2FAMessages } from '../../../../shared/i18n/Enable2FAMessages';

interface ParentProps extends MethodError {
  // tslint:disable-next-line no-any
  handleDismiss: () => any;
  loading?: boolean;
  show2FAInput?: boolean;
}

type Props = ParentProps &
  InjectedIntlProps;

export const DeleteAccountFinalAckPanelComponent: React.SFC<Props> = (props) => {
  const { methodError, handleDismiss, loading, show2FAInput, intl: { formatMessage } } = props;

  let description;
  if (show2FAInput) {
    const androidLink = (
      <Link.Anchor href={externalUrls.androidGoogleAuthenticator} target="_blank">
        <FormattedMessage {...Enable2FAMessages.StepOneAndroidLink}/>
      </Link.Anchor>
    );

    const iosLink = (
      <Link.Anchor href={externalUrls.iosGoogleAuthenticator} target="_blank">
        <FormattedMessage {...Enable2FAMessages.StepOneIOSLinkText}/>
      </Link.Anchor>
    );

    const popoverContent = (
      <React.Fragment>
        <FormattedMessage {...DeleteAccountMessages.AvailableFor} values={{androidLink, iosLink}}/>
      </React.Fragment>
    );

    const googleAuthApp = (
      <Popover content={popoverContent} trigger="hover">
        <Link.Anchor><FormattedMessage {...DeleteAccountMessages.TwoFactorSummaryMessageLinkText}/></Link.Anchor>
      </Popover>
    );

    description = (
      <FormattedMessage
        {...DeleteAccountMessages.AcknowledgeMessageWithTwoFactorAuth}
        values={{googleAuthApp}}
      />
    );

  } else {
    description = <FormattedMessage {...DeleteAccountMessages.AcknowledgeMessage}/>;
  }

  return (
    <React.Fragment>

      <DialogHeader
        title={
          <AuthHeader style={{color: 'RED', fontWeight: 'lighter'}}>
            <FormattedMessage {...DeleteAccountMessages.AcknowledgeTitle}/>
          </AuthHeader>
        }
        description={description}
        includeFormMethodError={true}
        methodError={methodError}
      />

      <FlexContainer column={true}  justifyContent="flex-start">
        <InputField
          prefix={convertInputFieldAddon(<Icon type="lock"/>)}
          size="default"
          type="password"
          placeholder={formatMessage(LoginMessages.PasswordInputPlaceholder)}
          name="currentPassword"
          style={{marginBottom: 10}}
        />

        {show2FAInput &&
          <TwoFactorAuthenticationCodeInput
            name="twoFactorAuthCode"
            style={{marginBottom: 10}}
          />
        }
      </FlexContainer>

      <ContinueCancelButtons
        continueLabel={<FormattedMessage {...DeleteAccountMessages.FinalAcknowledgeButtonLabel}/>}
        cancelLabel={<FormattedMessage {...SharedComponentMessages.Cancel}/>}
        continueHTMLType="submit"
        continueEnabled={true}
        handleCancel={handleDismiss}
        stackVertical={true}
        continueStyle={{width: 200, marginTop: 40}}
        loading={loading}
      />

    </React.Fragment>
  );
};

export const DeleteAccountFinalAckPanel = compose(
  injectIntl
)(DeleteAccountFinalAckPanelComponent) as React.ComponentClass<ParentProps>;
