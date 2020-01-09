import * as React from 'react';
import { branch, compose, renderComponent, withHandlers } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { SEO } from '../../../shared/components/SEO';
import { Label, SettingsGroup } from '../settingsStyles';
import { MemberAccountSettingsMessages } from '../../../shared/i18n/MemberAccountSettingsMessages';
import { Col, Row } from 'antd';
import {
  withCurrentUserEmailAddress,
  WithCurrentUserEmailAddressProps,
  withState,
  WithStateProps
} from '@narrative/shared';
import { ChangeEmailAddressModal } from './ChangeEmailAddressModal';
import { FlexContainer } from '../../../shared/styled/shared/containers';
import { ChangePasswordModal } from './ChangePasswordModal';
import { Link } from '../../../shared/components/Link';
import { DeleteAccountModal } from './deleteaccount/DeleteAccountModal';
import { SectionHeader } from '../../../shared/components/SectionHeader';
import { Loading } from '../../../shared/components/Loading';
import { PendingEmailAddressField } from './components/PendingEmailAddressField';

interface State {
  displayChangeEmailAddress: boolean;
  displayChangePassword: boolean;
  displayDeleteAccount: boolean;
}

const initialState: State = {
  displayChangeEmailAddress: false,
  displayChangePassword: false,
  displayDeleteAccount: false
};

interface WithHandlers {
  handleShowUpdateEmailAddress: () => void;
  handleDismissUpdateEmailAddress: () => void;
  handleShowUpdatePassword: () => void;
  handleDismissUpdatePassword: () => void;
  handleShowDeleteAccount: () => void;
  handleDismissDeleteAccount: () => void;
}

type Props =
  WithStateProps<State> &
  WithHandlers &
  Pick<WithCurrentUserEmailAddressProps, 'emailAddressDetail'>;

const EditAccountSettingsComponent: React.SFC<Props> = (props) => {
  const {
    state,
    handleShowUpdateEmailAddress,
    handleDismissUpdateEmailAddress,
    handleShowUpdatePassword,
    handleDismissUpdatePassword,
    handleShowDeleteAccount,
    handleDismissDeleteAccount
  } = props;

  const { emailAddress, pendingEmailAddress } = props.emailAddressDetail;

  const tosLink = <Link.Legal type="tos"/>;

  return (
    <React.Fragment>
      <SEO title={MemberAccountSettingsMessages.SEOTitle} />

      <SettingsGroup>
        <SectionHeader
          title={<FormattedMessage {...MemberAccountSettingsMessages.SectionCredentials}/>}
          description={<FormattedMessage {...MemberAccountSettingsMessages.SectionCredentialsDescription}/>}
          extra={(<
            Link.Anchor onClick={handleShowDeleteAccount} style={{marginLeft: 50, color: 'RED'}}>
            <FormattedMessage {...MemberAccountSettingsMessages.DeleteAccountLabel}/>
          </Link.Anchor>
          )}
        />

        <FlexContainer alignItems="center">
          <Label uppercase={true} size={6}>
            <FormattedMessage {...MemberAccountSettingsMessages.EmailAddressLabel}/>
          </Label>

          {emailAddress}

          {/* jw: only include the change link if there is not already a pending email */}
          {!pendingEmailAddress &&
            <Link.Anchor onClick={handleShowUpdateEmailAddress} style={{ marginLeft: 50 }}>
              <FormattedMessage {...MemberAccountSettingsMessages.UpdateEmailAddressLabel}/>
            </Link.Anchor>
          }
        </FlexContainer>

        <PendingEmailAddressField emailAddressDetail={props.emailAddressDetail} />

        <FlexContainer alignItems="center">
          <Label uppercase={true} size={6}>
            <FormattedMessage {...MemberAccountSettingsMessages.PasswordLabel}/>
          </Label>

          <Link.Anchor onClick={handleShowUpdatePassword}>
            <FormattedMessage {...MemberAccountSettingsMessages.UpdatePasswordLabel}/>
          </Link.Anchor>
        </FlexContainer>
      </SettingsGroup>

      <SettingsGroup>
        <SectionHeader title={<FormattedMessage {...MemberAccountSettingsMessages.SectionTermsOfService}/>}/>

        <Row type="flex" align="middle" justify="space-between" style={{marginTop: 25}}>
          <Col>
              <FormattedMessage {...MemberAccountSettingsMessages.TermsOfServiceDescription} values={{tosLink}}/>
          </Col>
        </Row>

      </SettingsGroup>

      {/*
        jw: even though we only include the link if there is no pending email address, we need to always include the
            modal, otherwise when the user first changes their email the modal closing animation will be ignored.
      */}
      <ChangeEmailAddressModal
        dismiss={handleDismissUpdateEmailAddress}
        visible={state.displayChangeEmailAddress}
      />

      <ChangePasswordModal
        dismiss={handleDismissUpdatePassword}
        visible={state.displayChangePassword}
      />

      <DeleteAccountModal
        dismiss={handleDismissDeleteAccount}
        visible={state.displayDeleteAccount}
      />

    </React.Fragment>
  );
};

export default compose(
  withCurrentUserEmailAddress,
  branch<WithCurrentUserEmailAddressProps>((props) => props.loading,
    renderComponent(() => <Loading />)
  ),
  withState<State>(initialState),
  withHandlers({
    handleShowUpdateEmailAddress: (props: WithStateProps<State>) => async () => {
      const {setState} = props;

      setState(ss => ({...ss, displayChangeEmailAddress: true}));
    },
    handleDismissUpdateEmailAddress: (props: WithStateProps<State>) => async () => {
      const {setState} = props;
      setState(ss => ({...ss, displayChangeEmailAddress: false}));
    },
    handleShowUpdatePassword: (props: WithStateProps<State>) => async () => {
      const {setState} = props;
      setState(ss => ({...ss, displayChangePassword: true}));
    },
    handleDismissUpdatePassword: (props: WithStateProps<State>) => async () => {
      const {setState} = props;
      setState(ss => ({...ss, displayChangePassword: false}));
    },
    handleShowDeleteAccount: (props: WithStateProps<State>) => async () => {
      const {setState} = props;
      setState(ss => ({...ss, displayDeleteAccount: true}));
    },
    handleDismissDeleteAccount: (props: WithStateProps<State>) => async () => {
      const {setState} = props;
      setState(ss => ({...ss, displayDeleteAccount: false}));
    },
    handleShowRevokeAgreement: (props: WithStateProps<State>) => async () => {
      const {setState} = props;
      setState(ss => ({...ss, displayRevokeAgreement: true}));
    },
    handleDismissRevokeAgreement: (props: WithStateProps<State>) => async () => {
      const {setState} = props;
      setState(ss => ({...ss, displayRevokeAgreement: false}));
    }
  })
)(EditAccountSettingsComponent) as React.ComponentClass<{}>;
