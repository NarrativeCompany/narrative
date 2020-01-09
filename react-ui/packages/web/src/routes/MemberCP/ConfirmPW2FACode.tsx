import * as React from 'react';
import { Col, Icon, Row } from 'antd';
import { AuthForm, AuthWrapper } from '../../shared/styled/shared/auth';
import { convertInputFieldAddon } from '../../shared/utils/convertInputAddon';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { LoginMessages } from '../../shared/i18n/LoginMessages';
import { FormikProps, withFormik } from 'formik';
import { compose } from 'recompose';
import {
  pw2FACodeFormFormikUtil,
  PW2FACodeFormValues,
  MethodError,
  withState,
  WithStateProps
} from '@narrative/shared';
import { TwoFactorLoginMessages } from '../../shared/i18n/TwoFactorLoginMessages';
import { Logo } from '../../shared/components/Logo';
import { FlexContainer } from '../../shared/styled/shared/containers';
import { Heading } from '../../shared/components/Heading';
import { FormMethodError } from '../../shared/components/FormMethodError';
import { FormField } from '../../shared/components/FormField';
import { DescriptionParagraph, SubmitButton } from './settingsStyles';
import { TwoFactorAuthenticationCodeInput } from '../../shared/components/TwoFactorAuthenticationCodeInput';
import { DeleteAccountMessages } from '../../shared/i18n/DeleteAccountMessages';

interface State {
  nicheConfirmationChecked?: boolean;
  publicationConfirmationChecked?: boolean;
  isSubmitting?: boolean;
}

interface ParentProps extends MethodError {
  show2FAInput: boolean;
  // tslint:disable-next-line no-any
  onSubmit: (setErrors: any, password: string, verificationCode?: string) => void;
  // tslint:disable-next-line no-any
  fieldErrors: any | null;
  title?: React.ReactNode;
  description?: React.ReactNode;
  submitLabel?: React.ReactNode;
  nicheConfirmationCheckboxLabel?: React.ReactNode;
  publicationConfirmationCheckboxLabel?: React.ReactNode;
}

type Props =
  ParentProps &
  FormikProps<PW2FACodeFormValues> &
  WithStateProps<State> &
  InjectedIntlProps;

const ConfirmPW2FACodeComponent: React.SFC<Props> = (props) => {
  const {
    intl,
    methodError,
    show2FAInput,
    description,
    submitLabel,
    nicheConfirmationCheckboxLabel,
    publicationConfirmationCheckboxLabel,
    setState,
    state: { nicheConfirmationChecked, publicationConfirmationChecked, isSubmitting }
  } = props;

  const buttonMargin = show2FAInput ? 10 : 45;
  const title = props.title || <FormattedMessage {...LoginMessages.ConfirmFormTitle}/>;
  const submitButtonLabel = submitLabel || <FormattedMessage {...TwoFactorLoginMessages.SubmitBtnText}/>;

  return (
    <AuthWrapper centerAll={true}>

      <AuthForm>

        <Row type="flex" align="middle" justify="center" style={{ paddingBottom: 10 }}>
          <Col>
            <Logo/>
          </Col>
        </Row>

        <FlexContainer centerAll={true} column={true} style={{ paddingBottom: 10 }}>
          <Heading size={3}>
            {title}
          </Heading>

          {description &&
            <DescriptionParagraph>
              {description}
            </DescriptionParagraph>
          }
        </FlexContainer>

        <FormMethodError methodError={methodError}/>

        <FormField.Input
          prefix={convertInputFieldAddon(<Icon type="lock"/>)}
          size="large"
          type="password"
          placeholder={intl.formatMessage(LoginMessages.PasswordInputPlaceholder)}
          label={intl.formatMessage(LoginMessages.PasswordInputLabel)}
          name="currentPassword"
          style={{ marginBottom: buttonMargin }}
        />

        {show2FAInput &&
          <TwoFactorAuthenticationCodeInput
            name="twoFactorAuthCode"
            style={{marginBottom: 45}}
          />
        }

        {nicheConfirmationCheckboxLabel &&
          <FormField.Checkbox
            name="confirmationCheckbox"
            checked={nicheConfirmationChecked}
            onChange={() => setState(ss => ({...ss, nicheConfirmationChecked: !nicheConfirmationChecked}))}
          >
            {nicheConfirmationCheckboxLabel}
          </FormField.Checkbox>
        }

        {publicationConfirmationCheckboxLabel &&
          <FormField.Checkbox
            name="confirmationCheckbox"
            checked={publicationConfirmationChecked}
            onChange={() => setState(ss => ({...ss, publicationConfirmationChecked: !publicationConfirmationChecked}))}
            extra={<FormattedMessage {...DeleteAccountMessages.PublicationsLostDescriptionRevokeAgreement}/>}
          >
            {publicationConfirmationCheckboxLabel}
          </FormField.Checkbox>
        }

        <SubmitButton
          size="large"
          type="primary"
          htmlType="submit"
          block={true}
          loading={isSubmitting}
          disabled={
            (nicheConfirmationCheckboxLabel && !nicheConfirmationChecked) ||
            (publicationConfirmationCheckboxLabel && !publicationConfirmationChecked)
          }
          style={{marginBottom: 15}}
        >
          {submitButtonLabel}
        </SubmitButton>

      </AuthForm>

    </AuthWrapper>
  );
};

export const ConfirmPW2FACode = compose(
  injectIntl,
  withState<State>({}),
  withFormik<Props, PW2FACodeFormValues>({
    ...pw2FACodeFormFormikUtil,
    handleSubmit: async (values, {props, setErrors}) => {
      const { onSubmit, setState, state: {isSubmitting} } = props;

      // jw: short out if we are already submitting
      if (isSubmitting) {
        return;
      }

      setState(ss => ({...ss, isSubmitting: true}));
      try {
        await onSubmit(setErrors, values.currentPassword, values.twoFactorAuthCode);

      } finally {
        setState(ss => ({...ss, isSubmitting: undefined}));
      }
    }
  })
)(ConfirmPW2FACodeComponent) as React.ComponentClass<ParentProps>;
