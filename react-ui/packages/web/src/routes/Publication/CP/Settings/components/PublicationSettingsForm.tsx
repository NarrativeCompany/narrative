import * as React from 'react';
import { compose } from 'recompose';
import {
  applyExceptionToState,
  initialFormState,
  PublicationSettingsFormParentProps,
  publicationSettingsFormUtil,
  PublicationSettingsFormValues,
  WithUpdatePublicationSettingsProps,
  withState,
  withUpdatePublicationSettings,
  SimpleFormState,
  WithStateProps
} from '@narrative/shared';
import { withRouter, RouteComponentProps, generatePath } from 'react-router';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { Form, FormikProps, withFormik } from 'formik';
import { openNotification } from '../../../../../shared/utils/notificationsUtil';
import { PublicationDetailsMessages } from '../../../../../shared/i18n/PublicationDetailsMessages';
import { FormMethodError } from '../../../../../shared/components/FormMethodError';
import { Button } from '../../../../../shared/components/Button';
import { Section } from '../../../../../shared/components/Section';
import { Link } from '../../../../../shared/components/Link';
import { externalUrls } from '../../../../../shared/constants/externalUrls';
import {
  PublicationNameAndDescriptionFormFields
} from '../../../../CreatePublication/components/PublicationNameAndDescriptionFormFields';
import { FormField } from '../../../../../shared/components/FormField';
import { EnhancedPublicationUrlType } from '../../../../../shared/enhancedEnums/publicationUrlType';
import {
  FileUpload,
  getExistingUploadFile,
  setFileOnFieldValueHelper
} from '../../../../../shared/components/upload/FileUpload';
import { FileUsageType } from '../../../../../shared/utils/fileUploadUtils';
import { Select } from 'antd';
import { FormControl } from '../../../../../shared/components/FormControl';
import { SelectValue } from 'antd/lib/select';
import { EnhancedHorizontalAlignment } from '../../../../../shared/enhancedEnums/horizontalAlignment';
import { WebRoute } from '../../../../../shared/constants/routes';
import { scrollToTop } from '../../../../../shared/utils/scrollUtils';
import {
  EnhancedPublicationContentRewardWriterShare
} from '../../../../../shared/enhancedEnums/publicationContentRewardWriterShare';
import {
  EnhancedPublicationContentRewardRecipientType
} from '../../../../../shared/enhancedEnums/publicationContentRewardRecipientType';
import {
  PublicationDetailsConnect,
  WithPublicationDetailsContextProps
} from '../../../components/PublicationDetailsContext';
import { SelectField, SelectFields } from '../../../../../shared/components/SelectField';

type Props = PublicationSettingsFormParentProps &
  FormikProps<PublicationSettingsFormValues> &
  WithStateProps<SimpleFormState> &
  WithPublicationDetailsContextProps &
  InjectedIntlProps;

const PublicationSettingsFormComponent: React.SFC<Props> = (props) => {
  const {
    isSubmitting,
    setFieldValue,
    state: { methodError },
    publicationSettings: {
      publicationDetail: {
        headerImageUrl,
        publication: { logoUrl }
      }
    },
    intl: { formatMessage },
    values: { headerImageAlignment, contentRewardWriterShare, contentRewardRecipient },
    currentUserRoles
  } = props;

  const currentLogoName = formatMessage(PublicationDetailsMessages.PublicationLogoCurrentFileName);

  const analyticsLink = (
    <Link.Anchor href={externalUrls.fathom} target="_blank">
      <FormattedMessage {...PublicationDetailsMessages.AnalyticsLinkText} />
    </Link.Anchor>
  );

  const enhancedWriterShare = EnhancedPublicationContentRewardWriterShare.get(contentRewardWriterShare);

  const contentRewardRecipientSelectFields: SelectFields =
    EnhancedPublicationContentRewardRecipientType.enhancers.map((enhancedRecipientType) => ({
    value: enhancedRecipientType.type,
    text: <FormattedMessage {...enhancedRecipientType.name}/>
  }));

  return (
    <Form>
      <FormMethodError methodError={methodError}/>

      <Section title={<FormattedMessage {...PublicationDetailsMessages.PublicationInformationSectionTitle} />}>
        <PublicationNameAndDescriptionFormFields useLabels={true} />
      </Section>

      <Section
        title={<FormattedMessage {...PublicationDetailsMessages.PublicationLogoSectionTitle} />}
        description={<FormattedMessage {...PublicationDetailsMessages.PublicationLogoSectionDescription} />}
      >
        <FileUpload
          onChange={setFileOnFieldValueHelper(setFieldValue, 'logo')}
          fileUsageType={FileUsageType.PUBLICATION_LOGO}
          fileList={getExistingUploadFile(currentLogoName, logoUrl)}
        />
      </Section>

      <Section
        title={<FormattedMessage {...PublicationDetailsMessages.HeaderImageSectionTitle} />}
        description={<FormattedMessage {...PublicationDetailsMessages.HeaderImageSectionDescription} />}
      >
        <FileUpload
          onChange={setFileOnFieldValueHelper(setFieldValue, 'headerImage')}
          fileUsageType={FileUsageType.PUBLICATION_HEADER}
          // jw: the file name will not be displayed since we are using an image preview format.
          fileList={getExistingUploadFile('', headerImageUrl)}
          useImagePreview={true}
        />
        <FormControl
          style={{ marginBottom: 0 }}
          label={<FormattedMessage {...PublicationDetailsMessages.HeaderImageAlignmentLabel}/>}
        >
          <Select
            size="large"
            value={headerImageAlignment}
            onChange={(value: SelectValue) => setFieldValue('headerImageAlignment', value)}
          >
            {EnhancedHorizontalAlignment.enhancers.map((helper) =>
              <Select.Option key={`align_${helper.alignment}`} value={helper.alignment}>
                <FormattedMessage {...helper.title} />
              </Select.Option>
            )}
          </Select>
        </FormControl>
      </Section>

      <Section
        title={<FormattedMessage {...PublicationDetailsMessages.LinksSectionTitle} />}
        description={<FormattedMessage {...PublicationDetailsMessages.LinksSectionDescription} />}
      >
        {EnhancedPublicationUrlType.enhancers.map((urlType) => {
          const title = <FormattedMessage {...urlType.title} />;

          return (
            <FormField.Input
              key={`urlField_${urlType.urlType}`}
              name={`urls.${urlType.urlFieldName}`}
              size="large"
              type="text"
              label={
                <span>
                  {urlType.getIcon({marginRight: 7})}
                  <FormattedMessage {...PublicationDetailsMessages.YourLinkUrlLabel} values={{title}} />
                </span>
              }
            />
          );
        })}
      </Section>

      <Section
        title={<FormattedMessage {...PublicationDetailsMessages.AnalyticsSectionTitle} />}
        description={<FormattedMessage
          {...PublicationDetailsMessages.AnalyticsSectionDescription}
          values={{analyticsLink}}
        />}
      >
        <FormField.Input
          name="fathomSiteId"
          size="large"
          type="text"
          label={<FormattedMessage {...PublicationDetailsMessages.AnalyticsIdLabel} />}
        />
      </Section>

      <Section
        title={<FormattedMessage {...PublicationDetailsMessages.RewardSharing} />}
        description={<FormattedMessage {...PublicationDetailsMessages.RewardSharingDescription}/>}
      >
        <FormControl label={<FormattedMessage {...PublicationDetailsMessages.WritersContentCreatorRewards}/>}>
          <Select
            size="large"
            value={contentRewardWriterShare}
            disabled={!currentUserRoles.owner}
            onChange={(value: SelectValue) => setFieldValue('contentRewardWriterShare', value)}
          >
            {EnhancedPublicationContentRewardWriterShare.enhancers.map((helper) =>
              <Select.Option key={`align_${helper.share}`} value={helper.share}>
                <FormattedMessage {...helper.name} />
              </Select.Option>
            )}
          </Select>
        </FormControl>

        {!enhancedWriterShare.isOneHundredPercent() &&
          <SelectField
            label={<FormattedMessage {...PublicationDetailsMessages.PublicationShareRecipient}/>}
            name="contentRewardRecipient"
            value={contentRewardRecipient}
            disabled={!currentUserRoles.owner}
            selectFields={contentRewardRecipientSelectFields}
            size="large"
            placeholder={formatMessage(PublicationDetailsMessages.ChooseOne)}
          />
        }
      </Section>

      <Button size="large" type="primary" htmlType="submit" loading={isSubmitting}>
        <FormattedMessage {...PublicationDetailsMessages.UpdateSettingsButtonText}/>
      </Button>
    </Form>
  );
};

export const PublicationSettingsForm = compose(
  PublicationDetailsConnect,
  injectIntl,
  withState<SimpleFormState>(initialFormState),
  withUpdatePublicationSettings,
  withRouter,
  withFormik<Props & WithUpdatePublicationSettingsProps & RouteComponentProps<{}>, PublicationSettingsFormValues>({
    ...publicationSettingsFormUtil,
    handleSubmit: async (values, {props, setErrors, setSubmitting}) => {
      const input = {...values};
      const {
        setState,
        isSubmitting,
        history,
        intl: { formatMessage },
        publicationSettings: { oid, publicationDetail: { publication: { prettyUrlString } } }
      } = props;

      if (isSubmitting) {
        return;
      }

      setState(ss => ({...ss, methodError: null, isSubmitting: true}));

      let newPrettyUrlString: string | undefined;

      try {
        const settings = await props.updatePublicationSettings(input, oid);

        if (settings) {
          // Notify the user of success
          await openNotification.updateSuccess({
            description: formatMessage(PublicationDetailsMessages.SettingsUpdatedConfirmationDescription),
            message: formatMessage(PublicationDetailsMessages.SettingsUpdatedConfirmationMessage)
          });

          newPrettyUrlString = settings.publicationDetail.publication.prettyUrlString;
        }

        // bl: let's only scroll to top if the prettyUrlString is unchanged. otherwise, we'll be routing below
        if (newPrettyUrlString === prettyUrlString) {
          scrollToTop();
        }

      } catch (exception) {
        applyExceptionToState(exception, setErrors, setState);

      } finally {
        // jw: if the prettyUrlString changed as part of the update then we need to redirect to the new URL as part of
        //     this change.
        if (newPrettyUrlString !== undefined && newPrettyUrlString !== prettyUrlString) {
          history.push(generatePath(WebRoute.PublicationSettings, {id: newPrettyUrlString}));

        } else {
          setSubmitting(false);
          setState(ss => ({...ss, methodError: null, isSubmitting: undefined}));
        }
      }
    },
  }),
)(PublicationSettingsFormComponent) as React.ComponentClass<PublicationSettingsFormParentProps>;
