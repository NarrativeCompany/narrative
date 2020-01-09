import * as React from 'react';
import { AuthForm, AuthHeader, AuthWrapper } from '../styled/shared/auth';
import { Col, Form, Icon, Row } from 'antd';
import { Logo } from './Logo';
import { FormattedMessage } from 'react-intl';
import { Button } from './Button';
import { TwoFactorEnabledOnAccountProps } from '@narrative/shared';
import { InputField } from './InputField';
import { convertInputFieldAddon } from '../utils/convertInputAddon';
import { LoginMessages } from '../i18n/LoginMessages';
import { TwoFactorAuthenticationCodeInput } from './TwoFactorAuthenticationCodeInput';
import { injectIntl, InjectedIntlProps } from 'react-intl';
import { compose } from 'recompose';
import { HTMLAttributes } from 'react';
import { Block } from './Block';

const FormItem = Form.Item;

/**
 * A lot of of our modal forms require account verification, so let's centralize as much of the form as we can.
 */

export const distanceBetweenAccountVerificationFields = 10;

interface ParentProps extends TwoFactorEnabledOnAccountProps, Pick<HTMLAttributes<{}>, 'style'> {
  title: FormattedMessage.MessageDescriptor;
  description?: React.ReactNode;
  submitText: FormattedMessage.MessageDescriptor;
  customPasswordLabel?: FormattedMessage.MessageDescriptor;
  excludePassword?: boolean;
  isSubmitting?: boolean;
  footer?: React.ReactNode;
}

type Props = ParentProps &
  InjectedIntlProps;

const AccountVerificationFormWrapperComponent: React.SFC<Props> = (props) => {
  const {
    title,
    description,
    submitText,
    customPasswordLabel,
    excludePassword,
    twoFactorEnabled,
    isSubmitting,
    style,
    children,
    footer,
    intl: { formatMessage }
  } = props;

  return (
    <AuthWrapper centerAll={true} style={style}>
      <AuthForm>

        <Row type="flex" align="middle" justify="center" style={{ marginBottom: 25 }}>
          <Col>
            <Logo/>
          </Col>
        </Row>

        <Row type="flex" align="middle" justify="center" style={{ marginBottom: 25 }}>
          <Col>
            <AuthHeader>
              <FormattedMessage {...title}/>
            </AuthHeader>
          </Col>
        </Row>

        {description &&
          <Block style={{ marginBottom: 20 }}>
            {description}
          </Block>
        }

        {children}

        {!excludePassword &&
          <InputField
            prefix={convertInputFieldAddon(<Icon type="lock"/>)}
            size="large"
            type="password"
            placeholder={formatMessage(LoginMessages.PasswordInputPlaceholder)}
            label={formatMessage(
              customPasswordLabel
                ? customPasswordLabel
                : LoginMessages.PasswordInputLabel
            )}
            name="currentPassword"
            style={{marginBottom: distanceBetweenAccountVerificationFields}}
          />
        }

        {twoFactorEnabled &&
          <TwoFactorAuthenticationCodeInput
            name="twoFactorAuthCode"
            style={{marginBottom: distanceBetweenAccountVerificationFields}}
          />
        }

        {/*
          jw: the inputs have a bit of space in their titles which ultimately makes the 10 margin bottom result in about
              25 pixels of distance between items. With that in mind, let's just set the margin top on this to ensure
              that the button has that same distance.
        */}
        <FormItem style={{marginTop: 25}}>
          <Button size="large" type="primary" htmlType="submit" block={true} loading={isSubmitting}>
            <FormattedMessage {...submitText}/>
          </Button>
        </FormItem>

        {footer}
      </AuthForm>
    </AuthWrapper>
  );
};

export const AccountVerificationFormWrapper = compose(
  injectIntl,
)(AccountVerificationFormWrapperComponent) as React.ComponentClass<ParentProps>;
