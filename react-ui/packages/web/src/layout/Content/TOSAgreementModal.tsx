import * as React from 'react';
import { Col, Modal, Row } from 'antd';
import { AuthForm, AuthWrapper } from '../../shared/styled/shared/auth';
import { Logo } from '../../shared/components/Logo';
import { FlexContainer } from '../../shared/styled/shared/containers';
import { Heading } from '../../shared/components/Heading';
import { FormattedMessage } from 'react-intl';
import { compose, withHandlers } from 'recompose';
import {
  applyExceptionMethodErrorToState,
  UserRenewTosAgreementInput,
  withClearErrorState,
  WithClearErrorStateProps,
  withErrorState,
  withRenewTermsOfService,
  WithRenewTermsOfServiceProps,
  withState,
  WithStateProps,
  MethodError
} from '@narrative/shared';
import { logout } from '../../shared/utils/authTokenUtils';
import { WebRoute } from '../../shared/constants/routes';
import { RouterProps, withRouter } from 'react-router';
import { FormMethodError } from '../../shared/components/FormMethodError';
import { MemberAccountSettingsMessages } from '../../shared/i18n/MemberAccountSettingsMessages';
import { Link } from '../../shared/components/Link';
import { DescriptionParagraph } from '../../routes/MemberCP/settingsStyles';
import { FormButtonGroup } from '../../shared/components/FormButtonGroup';
import { openNotification } from '../../shared/utils/notificationsUtil';
import { injectIntl, InjectedIntlProps } from 'react-intl';
import { SharedComponentMessages } from '../../shared/i18n/SharedComponentMessages';
import { reloadForLoginStateChange } from '../../apolloClientInit';

interface ParentProps {
  // tslint:disable-next-line no-any
  handleDismiss: () => any;
  modalVisible: boolean;
}

interface State extends MethodError{
  methodError: string[] | null;
  isSubmitting: boolean;
}

const initialState: State = {
  methodError: null,
  isSubmitting: false,
};

interface WithHandlers {
  // tslint:disable-next-line no-any
  handleAgree: () => any;
  // tslint:disable-next-line no-any
  handleDismiss: () => any;
}

type Props =
  WithStateProps<State> &
  ParentProps &
  WithRenewTermsOfServiceProps &
  WithClearErrorStateProps &
  WithHandlers &
  InjectedIntlProps;

const TOSAgreementModalComponent: React.SFC<Props> = (props) => {
  const { handleDismiss, handleAgree, state, modalVisible } = props;

  return (
    <Modal
      visible={modalVisible}
      onCancel={handleDismiss}
      footer={null}
      destroyOnClose={true}
    >
      <AuthWrapper centerAll={true}>
        <AuthForm>
          <Row type="flex" align="middle" justify="center" style={{paddingBottom: 25}}>
            <Col>
              <Logo/>
            </Col>
          </Row>

          <FlexContainer column={true} alignItems="center" style={{ paddingBottom: 10 }}>

            <Heading size={3}>
              <FormattedMessage {...MemberAccountSettingsMessages.TOSAcceptTitle}/>
            </Heading>

            <DescriptionParagraph>
              <FormattedMessage
                {...MemberAccountSettingsMessages.TOSAcceptDescription}
                values={{tosLink: <Link.Legal type="tos"/>}}
              />
            </DescriptionParagraph>

          </FlexContainer>

          <FormMethodError methodError={state.methodError} />

          <FormButtonGroup
            btnText={<FormattedMessage {...MemberAccountSettingsMessages.TOSAcceptSubmitLabel}/>}
            linkText={<FormattedMessage {...SharedComponentMessages.Cancel}/>}
            direction="column-reverse"
            btnProps={{block: true, loading: state.isSubmitting, onClick: handleAgree}}
            linkProps={{onClick: handleDismiss}}
          />

        </AuthForm>
      </AuthWrapper>

    </Modal>
  );
};

export const TOSAgreementModal = compose(
  injectIntl,
  withState<State>(initialState),
  withClearErrorState,
  withRouter,
  withRenewTermsOfService,
  withErrorState,
  withHandlers({
    handleDismiss: (props: Props & RouterProps) => async () => {
      const { history, handleDismiss } = props;
      // Log out and go to the home page
      handleDismiss();
      await logout();
      history.push(WebRoute.Home);
    },
    handleAgree:
      (props: Props & RouterProps & WithClearErrorStateProps) => async () => {
      const { intl, renewTermsOfService, handleDismiss, setState, state: {isSubmitting}} = props;

      if (isSubmitting) {
        return;
      }

      const input: UserRenewTosAgreementInput = {hasAgreedToTos: true};

      setState(ss => ({...ss, isSubmitting: true}));
      try {
        await renewTermsOfService({input});
        await handleDismiss();
        // Notify the user of success
        await openNotification.deleteSuccess(
          {
            description: '',
            message: intl.formatMessage(MemberAccountSettingsMessages.AcceptTOSSuccessful),
            duration: 0
          });

        // zb: normally we would reset the store here, but until
        // it is fixed we will be reloading the page until the
        // apollo resetStore() is fixed
        // TODO: #1036 Fix me when underlying Apollo issues are resolved

        // Reset the store so active queries that would have failed will be re-fetched
        // await resetStore();
        reloadForLoginStateChange();
      } catch (exception) {
        applyExceptionMethodErrorToState(exception, setState,  true);
        setState(ss => ({...ss, isSubmitting: false}));
      }
    }
  })
)(TOSAgreementModalComponent) as React.ComponentClass<ParentProps>;
