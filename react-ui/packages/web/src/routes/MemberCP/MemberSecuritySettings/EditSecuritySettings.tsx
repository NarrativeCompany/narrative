import * as React from 'react';
import { branch, compose, lifecycle, renderComponent, withHandlers, withProps } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { SEO } from '../../../shared/components/SEO';
import { MemberSecuritySettingsMessages } from '../../../shared/i18n/MemberSecuritySettingsMessages';
import { SwitchComponent } from '../../../shared/components/Switch';
import { SharedComponentMessages } from '../../../shared/i18n/SharedComponentMessages';
import {
  applyExceptionToState,
  DisableTwoFactorAuthInput,
  MethodError,
  withCurrentUserTwoFactorAuthState,
  WithCurrentUserTwoFactorAuthStateProps,
  withDisableTwoFactorAuth,
  WithDisableTwoFactorAuthProps,
  withState,
  WithStateProps
} from '@narrative/shared';
import { ConfirmPW2FACodeModal } from '../ConfirmPW2FACodeModal';
import { Enable2FAModal } from './Enable2FAModal';
import { ContainedLoading } from '../../../shared/components/Loading';
import { DescriptionParagraph } from '../settingsStyles';
import { Link } from '../../../shared/components/Link';
import { externalUrls } from '../../../shared/constants/externalUrls';
import { Enable2FAMessages } from '../../../shared/i18n/Enable2FAMessages';
import { injectGlobal } from '../../../shared/styled';
import { SectionHeader } from '../../../shared/components/SectionHeader';

interface State extends MethodError {
  twoFactorSwitchChecked: boolean;
  displayEnable2FA: boolean;
  displayConfirmPWand2FA: boolean;
  show2FAInput: boolean;
  // tslint:disable-next-line no-any
  fieldErrors: any;
}
const initialState: State = {
  twoFactorSwitchChecked: false,
  displayEnable2FA: false,
  displayConfirmPWand2FA: false,
  show2FAInput: false,
  methodError: null,
  fieldErrors: null
};

interface WithHandlers {
  handleSwitchChange: (checked: boolean) => void;
  handleEnable2FASuccess: () => void;
  handleEnable2FADismiss: () => void;
  handleDisable2FASubmit: (password: string, verificationCode: string) => void;
  handleDisable2FADismiss: () => void;
}

interface WithProps {
  currentUser2FAEnabled: boolean;
}

type Props =
  WithProps &
  WithHandlers &
  WithDisableTwoFactorAuthProps &
  WithStateProps<State>;

const injectStyle = () => injectGlobal`
  @media print {
    body.with-printable-modal {
      height: 1vh;
      
      .ant-modal-mask {
        background-color: white;
      }
    }
  }
`;

const EditSecuritySettingsComponent: React.SFC<Props> = (props) => {
  const {
    state,
    handleSwitchChange,
    handleDisable2FASubmit,
    handleDisable2FADismiss,
    handleEnable2FASuccess,
    handleEnable2FADismiss
  } = props;

  injectStyle();

  const iosLink = (
    <Link.Anchor href={externalUrls.iosGoogleAuthenticator} target="_blank">
      <FormattedMessage {...Enable2FAMessages.StepOneIOSLinkText}/>
    </Link.Anchor>
  );

  const androidLink = (
    <Link.Anchor href={externalUrls.androidGoogleAuthenticator} target="_blank">
      <FormattedMessage {...Enable2FAMessages.StepOneAndroidLink}/>
    </Link.Anchor>
  );

  return (
    <React.Fragment>
      <SEO title={MemberSecuritySettingsMessages.SEOTitle} />

      <SectionHeader title={<FormattedMessage {...MemberSecuritySettingsMessages.SectionSecurity}/>}/>

      <DescriptionParagraph>
        <FormattedMessage {...MemberSecuritySettingsMessages.TwoFactorAuthMessage} values={{iosLink, androidLink}}/>
      </DescriptionParagraph>

      <SwitchComponent
        checked={state.twoFactorSwitchChecked}
        onChange={handleSwitchChange}
        label={<FormattedMessage {...MemberSecuritySettingsMessages.TwoFactorAuthControlLabel}/>}
        checkedMessage={<FormattedMessage{...SharedComponentMessages.Enabled}/>}
        uncheckedMessage={<FormattedMessage{...SharedComponentMessages.Disabled}/>}
      />

      <ConfirmPW2FACodeModal
        dismiss={handleDisable2FADismiss}
        handleSubmit={handleDisable2FASubmit}
        show2FAInput={state.show2FAInput}
        methodError={state.methodError}
        fieldErrors={state.fieldErrors}
        visible={state.displayConfirmPWand2FA}
      />

      <Enable2FAModal
        success={handleEnable2FASuccess}
        dismiss={handleEnable2FADismiss}
        visible={state.displayEnable2FA}
      />
    </React.Fragment>
  );
};

export default compose(
  withCurrentUserTwoFactorAuthState,
  branch((props: WithCurrentUserTwoFactorAuthStateProps) => props.currentUserTwoFactorAuthStateData.loading,
    renderComponent(() => <ContainedLoading/>)
  ),
  withDisableTwoFactorAuth,
  withState<State>(initialState),
  withProps((props: WithCurrentUserTwoFactorAuthStateProps) => {
    const { currentUserTwoFactorAuthStateData: { getCurrentUserTwoFactorAuthState } } = props;
    const currentUser2FAEnabled =
      getCurrentUserTwoFactorAuthState &&
      getCurrentUserTwoFactorAuthState.enabled;

    return { currentUser2FAEnabled };
  }),
  lifecycle<Props, {}>({
    // tslint:disable-next-line object-literal-shorthand
    componentDidMount: function () {
      const { setState, currentUser2FAEnabled } = this.props;
      setState(ss => ({...ss, twoFactorSwitchChecked: currentUser2FAEnabled}));
    }
  }),
  withHandlers({

    handleSwitchChange: (props: WithStateProps<State>) =>  (checked: boolean) => {
      const { setState } = props;

      // Based on the check value passed to this handler - take the appropriate action below
      if (checked) {
        // Show the 2FA code dialog in order to turn on 2FA
        setState(ss => ({...ss, displayEnable2FA: true}));
      } else {
        // Show the dialog for confirmation of password/2FA code to shut off 2FA
        setState(ss => ({...ss, displayConfirmPWand2FA: true, show2FAInput: true}));
      }
    },

    // ENABLING 2FA - Handle a successful submission of the 2FA verification form
    handleEnable2FASuccess: (props: WithStateProps<State>) => async () => {
      const { setState } = props;
      // Success - set the switch state appropriately
      setState(ss => ({...ss, twoFactorSwitchChecked: true}));
    },

    // ENABLING 2FA - Handle a dismiss of the 2FA verrification form
    handleEnable2FADismiss: (props: WithStateProps<State>) => async () => {
      const { setState } = props;
      // Failure - go back to previous state
      setState(ss => ({...ss, displayEnable2FA: false}));
    },

    // DISABLING 2FA - Handle a submission of the 2FA verification form
    handleDisable2FASubmit: (props: WithStateProps<State> & WithDisableTwoFactorAuthProps) =>
      // tslint:disable-next-line no-any
      async (setErrors: any, currentPassword: string, twoFactorAuthCode: string) => {
      const { setState } = props;
      const input: DisableTwoFactorAuthInput = {currentPassword, twoFactorAuthCode};

      try {
        await props.disableTwoFactorAuth({input});
        // Success - dismiss the dialog
        setState(ss => ({...ss, twoFactorSwitchChecked: false, displayConfirmPWand2FA: false, show2FAInput: false}));
      } catch (exception) {
        applyExceptionToState(exception, setErrors, setState);
      }
    },

    // DISABLING 2FA - Handle a dismissal of the 2FA verification form
    handleDisable2FADismiss: (props: WithStateProps<State>) => async () => {
      const { setState } = props;
      setState(ss => ({...ss, displayConfirmPWand2FA: false, show2FAInput: false, twoFactorSwitchChecked: true}));
    }
  })
 )(EditSecuritySettingsComponent) as React.ComponentClass<{}>;
