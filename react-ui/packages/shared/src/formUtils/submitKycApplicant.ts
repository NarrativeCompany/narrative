import * as yup from 'yup';
import { ObjectSchema } from 'yup';
import { FileInput, KycIdentificationType } from '../types';

export const KYC_IDENTIFICATION_TYPE = 'kycIdentificationType';
export const DOC_FRONT_IMAGE = 'docFrontImage';
export const DOC_BACK_IMAGE = 'docBackImage';
export const LIVE_PHOTO_IMAGE = 'livePhotoImage';

export interface KycApplicantFormValues {
  kycIdentificationType?: KycIdentificationType;
  docFrontImage?: FileInput;
  docBackImage?: FileInput;
  livePhotoImage?: FileInput;
}

export const kycApplicantInitialValues: KycApplicantFormValues = {
  kycIdentificationType: undefined,
  docFrontImage: undefined,
  docBackImage: undefined,
  livePhotoImage: undefined
};

export const fileSizeMax = 1042 * 1024 * 10; // blob.size in bytes (10MB)

const kycIdentificationType = yup
  .mixed()
  .oneOf([
    KycIdentificationType.DRIVERS_LICENSE,
    KycIdentificationType.PASSPORT,
    KycIdentificationType.GOVERNMENT_ID,
  ], 'Identification type is a required field')
  .required();

const docFrontImage = yup.mixed();

const docBackImage = yup.mixed();

const livePhotoImage = yup.mixed();

export const kycApplicantSchema: ObjectSchema<KycApplicantFormValues> = yup.object({
  kycIdentificationType,
  docFrontImage,
  docBackImage,
  livePhotoImage
});

export const kycApplicantFormikUtil = {
  validationSchema: kycApplicantSchema,
  mapPropsToValues: () => (kycApplicantInitialValues)
};
