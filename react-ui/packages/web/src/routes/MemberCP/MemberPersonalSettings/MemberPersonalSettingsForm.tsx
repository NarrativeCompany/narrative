import * as React from 'react';
import { compose, withProps } from 'recompose';
import { RouteComponentProps } from 'react-router-dom';
import { Form, FormikProps, withFormik } from 'formik';
import { CheckboxField } from '../../../shared/components/CheckboxField';
import { SelectField, SelectFields } from '../../../shared/components/SelectField';
import { Button } from '../../../shared/components/Button';

import { FormMethodError } from '../../../shared/components/FormMethodError';
import { SettingsGroup } from '../settingsStyles';
import { MemberPersonalSettingsMessages } from '../../../shared/i18n/MemberPersonalSettingsMessages';
import {
  applyExceptionToState,
  initialFormState,
  MethodError,
  personalSettingsUtil,
  UserAgeStatus,
  UserPersonalSettings,
  UserPersonalSettingsFormValues,
  UserPersonalSettingsInput,
  withState,
  WithStateProps,
  withUpdateUserPersonalSettings,
  WithUpdateUserPersonalSettingsProps
} from '@narrative/shared';
import { SectionHeader } from '../../../shared/components/SectionHeader';
import {
  withExtractedCurrentUser,
  WithExtractedCurrentUserProps
} from '../../../shared/containers/withExtractedCurrentUser';
import { Link } from '../../../shared/components/Link';
import { EnhancedQualityFilter } from '../../../shared/enhancedEnums/qualityFilter';
import { openNotification } from '../../../shared/utils/notificationsUtil';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';

interface ParentProps {
  preferences: UserPersonalSettings;
}

type Props =
  FormikProps<UserPersonalSettingsFormValues> &
  WithUpdateUserPersonalSettingsProps &
  RouteComponentProps<{}> &
  WithStateProps<MethodError> &
  ParentProps &
  InjectedIntlProps &
  WithExtractedCurrentUserProps;

const MemberPersonalSettingsForm: React.SFC<Props> = (props) => {
  const { state, isSubmitting, currentUserAgeStatus } = props;

  const selectFields: SelectFields = EnhancedQualityFilter.enhancers.map((enhancedQualityFilter) => ({
    value: enhancedQualityFilter.qualityFilter,
    text: <FormattedMessage {...enhancedQualityFilter.titleMessage}/>
  }));

  let linkAndMessage = null;
  if (currentUserAgeStatus === UserAgeStatus.UNKNOWN) {
    linkAndMessage = (
      <React.Fragment>
        <FormattedMessage {...MemberPersonalSettingsMessages.AgeRestrictionExplanation}/>&nbsp;
        <Link.About type="certification">
          <FormattedMessage {...MemberPersonalSettingsMessages.CertificationLinkText}/>
        </Link.About>
      </React.Fragment>
    );
  } else if (currentUserAgeStatus === UserAgeStatus.UNDER_18) {
    linkAndMessage = (
      <FormattedMessage {...MemberPersonalSettingsMessages.CertifiedButUnder18Explanation}/>
    );
  }

  return (
    <Form style={{ width: '100%' }}>
      <FormMethodError methodError={state.methodError}/>

      <SettingsGroup>
        <SectionHeader title={<FormattedMessage {...MemberPersonalSettingsMessages.SectionContentFiltering}/>}/>

        <SelectField name="qualityFilter" style={{ width: 275 }} selectFields={selectFields}/>

        <CheckboxField
          name="displayAgeRestrictedContent"
          disabled={currentUserAgeStatus !== UserAgeStatus.OVER_18}
          help={linkAndMessage}
        >
          <FormattedMessage {...MemberPersonalSettingsMessages.AgeRestrictedContentLabel}/>
        </CheckboxField>
      </SettingsGroup>

      <SettingsGroup>
        <SectionHeader title={<FormattedMessage {...MemberPersonalSettingsMessages.Followers}/>}/>
        
        <CheckboxField name="hideMyFollowers">
          <FormattedMessage {...MemberPersonalSettingsMessages.HideMyFollowersLabel}/>
        </CheckboxField>

        <CheckboxField name="hideMyFollows">
          <FormattedMessage {...MemberPersonalSettingsMessages.HideMyFollowsLabel}/>
        </CheckboxField>
      </SettingsGroup>

      <Button size="large" type="primary" htmlType="submit" loading={isSubmitting}>
        <FormattedMessage {...MemberPersonalSettingsMessages.UpdateSettings}/>
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
  withExtractedCurrentUser,
  withUpdateUserPersonalSettings,
  injectIntl,
  withFormik<Props, UserPersonalSettingsFormValues>({
    ...personalSettingsUtil,
    mapPropsToValues: (props: Props) => ({
      qualityFilter: props.preferences.qualityFilter,
      displayAgeRestrictedContent: props.preferences.displayAgeRestrictedContent,
      hideMyFollowers: props.preferences.hideMyFollowers,
      hideMyFollows: props.preferences.hideMyFollows
    }),
    handleSubmit: async (values, {props, setErrors, setSubmitting}) => {
      const { setState, isSubmitting, intl: { formatMessage } } = props;

      if (isSubmitting) {
        return;
      }

      setState(ss => ({...ss, methodError: null}));

      try {
        const input: UserPersonalSettingsInput = {...values} as UserPersonalSettings;
        await props.updateUserPersonalSettings(input);

        // Notify the user of success
        await openNotification.updateSuccess(
          {
            description: '',
            message: formatMessage(MemberPersonalSettingsMessages.SettingsUpdated),
            duration: 5
          });
      } catch (exception) {
        applyExceptionToState(exception, setErrors, setState);

      } finally {
        setSubmitting(false);
      }
    }
  })
)(MemberPersonalSettingsForm) as React.ComponentClass<ParentProps>;
