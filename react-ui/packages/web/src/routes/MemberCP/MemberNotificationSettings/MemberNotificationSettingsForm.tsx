import * as React from 'react';
import { compose, withProps } from 'recompose';
import { RouteComponentProps } from 'react-router-dom';
import { Form, withFormik, FormikProps } from 'formik';
import { CheckboxField } from '../../../shared/components/CheckboxField';
import { Button } from '../../../shared/components/Button';
import { FormMethodError } from '../../../shared/components/FormMethodError';
import { SettingsGroup } from '../settingsStyles';
import { MemberNotificationSettingsMessages } from '../../../shared/i18n/MemberNotificationSettingsMessages';
import { FormattedMessage, injectIntl, InjectedIntlProps } from 'react-intl';
import {
  withUpdateUserNotificationSettings,
  WithUpdateUserNotificationSettingsProps,
  UserNotificationSettings,
  UserNotificationSettingsInput,
  withState,
  WithStateProps,
  UserNotificationSettingsFormValues,
  notificationSettingsUtil,
  applyExceptionToState,
  MethodError,
  initialFormState
} from '@narrative/shared';
import { SectionHeader } from '../../../shared/components/SectionHeader';
import { Paragraph } from '../../../shared/components/Paragraph';
import { openNotification } from '../../../shared/utils/notificationsUtil';

interface ParentProps {
  preferences: UserNotificationSettings;
}

type Props =
  FormikProps<UserNotificationSettingsFormValues> &
  WithUpdateUserNotificationSettingsProps &
  RouteComponentProps<{}> &
  WithStateProps<MethodError> &
  InjectedIntlProps &
  ParentProps;

const MemberNotificationSettingsForm: React.SFC<Props> = (props) => {
  const { state, isSubmitting } = props;

  return (
    <Form style={{width: '100%'}}>
      <FormMethodError methodError={state.methodError}/>

      <SettingsGroup>
        <SectionHeader title={<FormattedMessage {...MemberNotificationSettingsMessages.InfluenceNotifications}/>}/>

        <CheckboxField name="notifyWhenFollowed" style={{marginBottom: 0}}>
          <FormattedMessage {...MemberNotificationSettingsMessages.NotifyWhenFollowed}/>
        </CheckboxField>

        <CheckboxField name="notifyWhenMentioned" style={{marginBottom: 0}}>
          <FormattedMessage {...MemberNotificationSettingsMessages.NotifyWhenMentioned}/>
        </CheckboxField>

        <CheckboxField name="suspendAllEmails">
          <Paragraph color="error" style={{display: 'inline'}}>
            <FormattedMessage {...MemberNotificationSettingsMessages.SuspendAllEmails}/>
          </Paragraph>
        </CheckboxField>
      </SettingsGroup>

      <Button size="large" type="primary" htmlType="submit" loading={isSubmitting}>
        <FormattedMessage {...MemberNotificationSettingsMessages.UpdateSettings}/>
      </Button>
    </Form>
  );
};

export default compose(
  withProps((props: Props) => {
    const { preferences } = props;

    return preferences;
  }),
  withState<MethodError>(initialFormState),
  withUpdateUserNotificationSettings,
  injectIntl,
  withFormik<Props, UserNotificationSettingsFormValues>({
    ...notificationSettingsUtil,
    mapPropsToValues: (props: Props) => ({
      notifyWhenMentioned: props.preferences.notifyWhenMentioned,
      notifyWhenFollowed: props.preferences.notifyWhenFollowed,
      suspendAllEmails: props.preferences.suspendAllEmails
    }),
    handleSubmit: async (values, {props, setErrors, setSubmitting}) => {
      const { setState, isSubmitting, intl: { formatMessage } } = props;

      if (isSubmitting) {
        return;
      }

      setState(ss => ({...ss, methodError: null}));

      try {
        const input: UserNotificationSettingsInput = {...values} as UserNotificationSettings;
        await props.updateUserNotificationSettings({input});

        // Notify the user of success
        await openNotification.updateSuccess(
          {
            description: '',
            message: formatMessage(MemberNotificationSettingsMessages.NotificationSettingsUpdated),
            duration: 5
          });

      } catch (exception) {
        applyExceptionToState(exception, setErrors, setState);
      } finally {
        setSubmitting(false);
      }
    }
  })
)(MemberNotificationSettingsForm) as React.ComponentClass<ParentProps>;
