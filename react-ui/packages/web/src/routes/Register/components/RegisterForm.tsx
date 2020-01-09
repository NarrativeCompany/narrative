import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { Heading } from '../../../shared/components/Heading';
import { FlexContainer } from '../../../shared/styled/shared/containers';
import { Button } from '../../../shared/components/Button';
import { SectionHeader } from '../../../shared/components/SectionHeader';
import { FormField } from '../../../shared/components/FormField';
import { Link } from '../../../shared/components/Link';
import { Recaptcha } from '../../../shared/components/Recaptcha';
import { WebRoute } from '../../../shared/constants/routes';
import { RegisterMessages } from '../../../shared/i18n/RegisterMessages';
import { SharedComponentMessages } from '../../../shared/i18n/SharedComponentMessages';
import { MAX_EMAIL_ADDRESS_LENGTH, MAX_DISPLAY_NAME_LENGTH, MAX_USERNAME_LENGTH, MethodError } from '@narrative/shared';
import styled from '../../../shared/styled/index';
import { RecaptchaHandlers } from '../../../shared/utils/recaptchaUtils';
import { FormMethodError } from '../../../shared/components/FormMethodError';

const { Input, Checkbox } = FormField;

const formItemLayout = {
  labelCol: {
    sm: 24,
    md: 8,
    lg: 6,
    xl: 4
  },
  wrapperCol: {
    sm: 24,
    md: 16,
    lg: 18,
    xl: 18
  },
};

export const FormGroupWrapper = styled.div`
  margin-bottom: 50px;
`;

interface ParentProps extends RecaptchaHandlers, MethodError {
  onRegisterSuccess: (emailAddress: string) => void;
  onNextClick: () => void;
  isValidating: boolean;
  recaptchaError?: string;
}

export const RegisterForm: React.SFC<ParentProps> = (props) => {
  const {
    isValidating,
    onNextClick,
    handleRecaptchaVerifyCallback,
    handleRecaptchaExpiredCallback,
    recaptchaError,
    methodError
  } = props;

  return (
    <React.Fragment>
      <FormGroupWrapper>
        <FlexContainer justifyContent="space-between" alignItems="center">
          <Heading size={2}>
            <FormattedMessage {...RegisterMessages.FormTitle}/>
          </Heading>

          <Link to={WebRoute.Home} color="light">
            <FormattedMessage {...SharedComponentMessages.Cancel}/>
          </Link>
        </FlexContainer>
      </FormGroupWrapper>

      <FormMethodError methodError={methodError} />

      <FormGroupWrapper>
        <SectionHeader
          title={<FormattedMessage {...RegisterMessages.CredentialsTitle}/>}
          description={<FormattedMessage {...RegisterMessages.CredentialsDescription}/>}
        />

        <Input
          {...formItemLayout}
          label={<FormattedMessage {...RegisterMessages.EmailFieldLabel}/>}
          name="emailAddress"
          size="large"
          maxLength={MAX_EMAIL_ADDRESS_LENGTH}
        />
        {/* bl: intentionally not putting a maxLength on password so that if they copy/paste or auto-generate
           a password longer than 40 characters, they'll get an error and know to fix it vs. just having
           the end lopped off unknowingly. */}
        <Input
          {...formItemLayout}
          label={<FormattedMessage {...RegisterMessages.PasswordFieldLabel}/>}
          name="password"
          type="password"
          size="large"
        />
      </FormGroupWrapper>

      <FormGroupWrapper>
        <SectionHeader
          title={<FormattedMessage {...RegisterMessages.IdentityTitle}/>}
          description={<FormattedMessage {...RegisterMessages.IdentityDescription}/>}
        />

        <Input
          {...formItemLayout}
          label={<FormattedMessage {...RegisterMessages.NameFieldLabel}/>}
          extra={<FormattedMessage {...RegisterMessages.NameFieldExtra}/>}
          name="displayName"
          size="large"
          maxLength={MAX_DISPLAY_NAME_LENGTH}
        />

        <Input
          {...formItemLayout}
          label={<FormattedMessage {...RegisterMessages.HandleFieldLabel}/>}
          extra={<FormattedMessage {...RegisterMessages.HandleFieldExtra}/>}
          name="username"
          size="large"
          addonBefore="@"
          maxLength={MAX_USERNAME_LENGTH}
        />
      </FormGroupWrapper>

      <FormGroupWrapper>
        <SectionHeader
          title={<FormattedMessage {...SharedComponentMessages.TermsOfService}/>}
          style={{marginBottom: 20}}
        />

        <Checkbox name="hasAgreedToTos" hasFeedback={false}>
          <FormattedMessage
            {...RegisterMessages.CheckboxText}
            values={{termsOfService: <Link.Legal type="tos"/>}}
          />
        </Checkbox>
      </FormGroupWrapper>

      <FormGroupWrapper>
        <SectionHeader
          title={<FormattedMessage {...RegisterMessages.RecaptchaTitle}/>}
          style={{marginBottom: 20}}
        />

        <Recaptcha
          verifyCallback={handleRecaptchaVerifyCallback}
          expiredCallback={handleRecaptchaExpiredCallback}
          error={recaptchaError}
        />
      </FormGroupWrapper>

      <FormGroupWrapper>
        <Button type="primary" size="large" loading={isValidating} onClick={onNextClick} style={{ minWidth: 190 }}>
          <FormattedMessage {...SharedComponentMessages.NextBtnText}/>
        </Button>
      </FormGroupWrapper>
    </React.Fragment>
  );
};
