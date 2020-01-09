import * as React from 'react';
import { Col, Modal, Row } from 'antd';
import { AuthForm, AuthWrapper } from '../../shared/styled/shared/auth';
import { Logo } from '../../shared/components/Logo';
import { FlexContainer } from '../../shared/styled/shared/containers';
import { Heading } from '../../shared/components/Heading';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { compose, withHandlers } from 'recompose';
import {
  applyExceptionMethodErrorToState,
  withErrorState,
  withState,
  WithStateProps,
  withResendCurrentUserVerificationEmail,
  WithResendCurrentUserVerificationEmailProps,
  EmptyInput,
  MethodError
} from '@narrative/shared';
import { WebRoute } from '../../shared/constants/routes';
import { RouterProps, withRouter } from 'react-router';
import { FormMethodError } from '../../shared/components/FormMethodError';
import { MemberAccountSettingsMessages } from '../../shared/i18n/MemberAccountSettingsMessages';
import { Link } from '../../shared/components/Link';
import { DescriptionParagraph } from '../../routes/MemberCP/settingsStyles';
import { openNotification } from '../../shared/utils/notificationsUtil';
import { LoginMessages } from '../../shared/i18n/LoginMessages';
import { Button } from '../../shared/components/Button';
import { logout } from '../../shared/utils/authTokenUtils';

interface ParentProps {
  // tslint:disable-next-line no-any
  handleDismiss: () => any;
  modalVisible: boolean;
}

interface State extends MethodError {
  isSubmitting: boolean;
}

const initialState: State = {
  methodError: null,
  isSubmitting: false,
};

interface WithHandlers {
  // tslint:disable-next-line no-any
  handleResend: () => any;
  // tslint:disable-next-line no-any
  dismiss: () => any;
}

type Props =
  WithStateProps<State> &
  ParentProps &
  WithHandlers &
  InjectedIntlProps;

const EmailConfirmRequiredNotificationModalComponent: React.SFC<Props> = (props) => {
  const { dismiss, handleResend, state, modalVisible } = props;

  return (
    <Modal
      visible={modalVisible}
      onCancel={dismiss}
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
              <FormattedMessage {...MemberAccountSettingsMessages.EmailConfirmationRequiredTitle}/>
            </Heading>

            <DescriptionParagraph>
              <FormattedMessage
                {...MemberAccountSettingsMessages.EmailConfirmationRequiredMessage}
                values={{
                  link:
                    <Link.Anchor onClick={handleResend}>
                      <FormattedMessage {...MemberAccountSettingsMessages.EmailConfirmationRequiredLinkLabel}/>
                    </Link.Anchor>
                  }}
              />
            </DescriptionParagraph>

          </FlexContainer>

          <FormMethodError methodError={state.methodError} />

          <FlexContainer column={true} alignItems="center" style={{ paddingBottom: 35 }}>
            <Button type="primary" onClick={dismiss}>
              <FormattedMessage {...LoginMessages.ReturnHomeLinkText}/>
            </Button>
          </FlexContainer>

        </AuthForm>
      </AuthWrapper>

    </Modal>
  );
};

export const EmailConfirmRequiredNotificationModal = compose(
  injectIntl,
  withState<State>(initialState),
  withRouter,
  withResendCurrentUserVerificationEmail,
  withErrorState,
  withHandlers({
    dismiss: (props: Props & RouterProps) => async () => {
      const { history, handleDismiss } = props;

      // Log the user out
      await logout();

      // This clears error state
      handleDismiss();

      // Go to the home page
      history.push(WebRoute.Home);
    },
    handleResend:
      (props: Props & RouterProps & WithResendCurrentUserVerificationEmailProps) => async () => {
      const { intl, resendCurrentUserVerificationEmail, setState, state: {isSubmitting}} = props;

      if (isSubmitting) {
        return;
      }

      setState(ss => ({...ss, isSubmitting: true}));
      try {
        const input: EmptyInput = { dummy: false };
        await resendCurrentUserVerificationEmail({ input } );
        // Notify the user of success
        await openNotification.updateSuccess(
          {
            description: '',
            message: intl.formatMessage(MemberAccountSettingsMessages.EmailConfirmationResentMessage),
            duration: 0
          });
      } catch (exception) {
        applyExceptionMethodErrorToState(exception, setState,  true);
        setState(ss => ({...ss, isSubmitting: false}));
      }
    }
  })
)(EmailConfirmRequiredNotificationModalComponent) as React.ComponentClass<ParentProps>;
