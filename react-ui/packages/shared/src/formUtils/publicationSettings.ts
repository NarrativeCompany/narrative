import {
  PublicationFormValues,
  sharedPublicationFieldValidators,
  fileUploadValidator
} from './shared';
import {
  HorizontalAlignment,
  PublicationUrlsInput,
  FileUploadInput,
  PublicationContentRewardWriterShare,
  PublicationContentRewardRecipientType
} from '../types';
import * as yup from 'yup';
import { ObjectSchema } from 'yup';
import { WithPublicationSettingsProps } from '../api/publication';
import { omitProperties } from '../utils';

export type PublicationSettingsFormParentProps = Pick<WithPublicationSettingsProps, 'publicationSettings'>;

export interface PublicationSettingsFormValues extends PublicationFormValues {
  headerImage: FileUploadInput;
  headerImageAlignment: HorizontalAlignment;
  fathomSiteId?: string;
  urls: PublicationUrlsInput;
  contentRewardWriterShare: PublicationContentRewardWriterShare;
  contentRewardRecipient?: PublicationContentRewardRecipientType;
}

// TODO: localize yup validation messages #894
const headerImageAlignmentValidator = yup
  .mixed()
  .oneOf([
    HorizontalAlignment.LEFT,
    HorizontalAlignment.CENTER,
    HorizontalAlignment.RIGHT,
  ], 'Header Image alignment is a required field')
  .required();

// TODO: localize yup validation messages #894
const fathomSiteIdValidator = yup
  .string()
  .nullable()
  .matches(/[A-Z]{5}|[A-Z]{8}/, 'Invalid Fathom Site ID');

const urlValidator = yup
  .string()
  .nullable()
  .url();

// TODO: localize yup validation messages #894
const contentRewardWriterShareValidator = yup
  .mixed()
  .oneOf([
    PublicationContentRewardWriterShare.ONE_HUNDRED_PERCENT,
    PublicationContentRewardWriterShare.NINETY_PERCENT,
    PublicationContentRewardWriterShare.SEVENTY_FIVE_PERCENT,
    PublicationContentRewardWriterShare.FIFTY_PERCENT,
    PublicationContentRewardWriterShare.TWENTY_FIVE_PERCENT,
    PublicationContentRewardWriterShare.TEN_PERCENT,
    PublicationContentRewardWriterShare.ZERO_PERCENT,
  ], 'Writer\'s Content Creator Rewards is a required field')
  .required();

// TODO: localize yup validation messages #894
const contentRewardRecipientValidator = yup
  .mixed()
  .when('contentRewardWriterShare', {
    is: (val) => val === PublicationContentRewardWriterShare.ONE_HUNDRED_PERCENT,
    then: yup.mixed().nullable(),
    otherwise: yup
      .mixed()
      .oneOf([
        PublicationContentRewardRecipientType.OWNER,
        PublicationContentRewardRecipientType.ADMINS,
        PublicationContentRewardRecipientType.EDITORS,
      ], 'Publication Share Recipient is a required field')
      .required()
  });

const publicationSettingsValidationSchema: ObjectSchema<PublicationSettingsFormValues> =
  yup.object({
    ...sharedPublicationFieldValidators,
    headerImage: fileUploadValidator,
    headerImageAlignment: headerImageAlignmentValidator,
    fathomSiteId: fathomSiteIdValidator,
    contentRewardWriterShare: contentRewardWriterShareValidator,
    contentRewardRecipient: contentRewardRecipientValidator,
    urls: yup.object({
      websiteUrl: urlValidator,
      twitterUrl: urlValidator,
      facebookUrl: urlValidator,
      instagramUrl: urlValidator,
      youtubeUrl: urlValidator,
      snapchatUrl: urlValidator,
      pinterestUrl: urlValidator,
      linkedInUrl: urlValidator
    })
  });

export const publicationSettingsFormUtil = {
  validationSchema: publicationSettingsValidationSchema,
  mapPropsToValues: (props: PublicationSettingsFormParentProps) => {
    const { publicationSettings: {
      publicationDetail: {
        fathomSiteId,
        headerImageAlignment,
        urls,
        publication: {
          name,
          description
        }
      },
      contentRewardWriterShare,
      contentRewardRecipient
    } } = props;

    return {
      name,
      description,
      logo: {},
      headerImage: {},
      headerImageAlignment,
      // jw: need to convert the null from fathomSiteId to undefined for the form data.
      fathomSiteId: fathomSiteId || undefined,
      urls: omitProperties(urls, ['__typename']),
      contentRewardWriterShare,
      contentRewardRecipient: contentRewardRecipient || undefined
    };
  }
};
