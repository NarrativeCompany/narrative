import * as React from 'react';
import { compose,  withProps } from 'recompose';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { SEO } from '../../../shared/components/SEO';
import { SubmitButton, SettingsGroup } from '../settingsStyles';
import { Col, Icon, Row } from 'antd';
import { MemberEditProfileMessages } from '../../../shared/i18n/MemberEditProfileMessages';
import { convertInputFieldAddon } from '../../../shared/utils/convertInputAddon';
import { Form, FormikProps, withFormik } from 'formik';
import { FormField } from '../../../shared/components/FormField';
import { FormMethodError } from '../../../shared/components/FormMethodError';
import {
  API_URI,
  MethodError,
  initialFormState,
  applyExceptionToState,
  editProfileFormikUtil,
  EditProfileFormValues,
  withState,
  WithStateProps,
  withUpdateCurrentUserProfile,
  WithUpdateCurrentUserProfileProps
} from '@narrative/shared';
import { Link } from '../../../shared/components/Link';
import {
  withExtractedCurrentUser,
  WithExtractedCurrentUserProps
} from '../../../shared/containers/withExtractedCurrentUser';
import { FormControl } from '../../../shared/components/FormControl';
import { openNotification } from '../../../shared/utils/notificationsUtil';
import { SectionHeader } from '../../../shared/components/SectionHeader';

interface WithProps {
  displayName: string;
  username: string;
  userOID: string;
}

type Props =
  WithProps &
  FormikProps<EditProfileFormValues> &
  WithStateProps<MethodError> &
  InjectedIntlProps &
  WithUpdateCurrentUserProfileProps &
  WithExtractedCurrentUserProps;

const EditMemberSettingsComponent: React.SFC<Props> = (props) => {
  const { intl, state, isSubmitting } = props;

  const profileDownloadLink = (
    <Link.Anchor target="_self" href={`${API_URI}/users/current/profile-zip`}>
      <FormattedMessage {...MemberEditProfileMessages.DownloadProfileDataLabel}/>
    </Link.Anchor>
  );

  return (
    <React.Fragment>
      <SEO title={MemberEditProfileMessages.SEOTitle} />

      <SectionHeader
        title={<FormattedMessage {...MemberEditProfileMessages.SectionIdentity}/>}
        extra={profileDownloadLink}
      />

      <SettingsGroup>
        <Form>
          <FormMethodError methodError={state.methodError} />

          <FormField.Input
            name="displayName"
            prefix={convertInputFieldAddon(<Icon type="user"/>)}
            size="large"
            type="text"
            placeholder={intl.formatMessage(MemberEditProfileMessages.NameLabel)}
            label={intl.formatMessage(MemberEditProfileMessages.NameLabel)}
            minLength={1}
            maxLength={40}
            labelCol={{md: 4}}
            wrapperCol={{lg: 15}}
            style={{marginBottom: 30}}
            extra={<FormattedMessage {...MemberEditProfileMessages.NameDescription}/>}
          />

          <FormField.Input
            name="username"
            addonBefore="@"
            size="large"
            type="text"
            placeholder={intl.formatMessage(MemberEditProfileMessages.HandleLabel)}
            label={intl.formatMessage(MemberEditProfileMessages.HandleLabel)}
            minLength={3}
            maxLength={20}
            labelCol={{md: 4}}
            wrapperCol={{lg: 15}}
            style={{marginBottom: 50}}
            extra={<FormattedMessage {...MemberEditProfileMessages.HandleDescription}/>}
          />

          <Row type="flex" align="middle" justify="end">
            <Col>
              <FormControl>
                <SubmitButton
                  type="primary"
                  htmlType="submit"
                  loading={isSubmitting}
                  style={{minWidth: 200, marginRight: 25}}>
                  <FormattedMessage {...MemberEditProfileMessages.SubmitLabel}/>
                </SubmitButton>
              </FormControl>
            </Col>
          </Row>
          </Form>
      </SettingsGroup>

    </React.Fragment>
  );
};

export default compose(
  injectIntl,
  withState<MethodError>(initialFormState),
  withUpdateCurrentUserProfile,
  withExtractedCurrentUser,
  withProps((props: Props) => {
    return {
        displayName: props.currentUser && props.currentUser.displayName,
        username: props.currentUser && props.currentUser.username,
        userOID: props.currentUser && props.currentUser.oid
      };
  }),
  withFormik<Props, EditProfileFormValues>({
    ...editProfileFormikUtil,

    // Map DB values to the form
    mapPropsToValues: (props: Props) => editProfileFormikUtil.mapPropsToValues({
      displayName: props.displayName,
      username: props.username
    }),

    // Handle form submit
    handleSubmit: async (values, {props, setErrors, setSubmitting}) => {
      const input = {...values};
      const { setState, isSubmitting } = props;

      if (isSubmitting) {
        return;
      }

      setState(ss => ({...ss, methodError: null}));

      try {
        await props.updateCurrentUserProfile({input});
        openNotification.updateSuccess(
          {
            description: '',
            message: props.intl.formatMessage(MemberEditProfileMessages.SuccessLabel),
            duration: 5
          });
      } catch (exception) {
        applyExceptionToState(exception, setErrors, setState);
      }

      setSubmitting(false);
    }
  })
)(EditMemberSettingsComponent) as React.ComponentClass<{}>;
