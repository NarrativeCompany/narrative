import * as yup from 'yup';
import { FileUploadInput } from '../../types';
import { fileUploadValidator } from './uploadUtils';

export interface PublicationFormValues {
  name: string;
  description: string;
  logo: FileUploadInput;
}

// TODO: localize yup validation messages #894
export const publicationNameValidator = yup
  .string()
  .min(3, 'Publication Title must be at least 3 characters')
  .max(60, 'Publication Title must be at most 60 characters')
  .required('Publication Title is required.');

export const publicationDescriptionValidator = yup
  .string()
  .min( 10, 'Publication Description must be at least 10 characters')
  .max(256, 'Publication Description must be at most 256 characters')
  .required('Publication Description is required.');

export const sharedPublicationFieldValidators = {
  name: publicationNameValidator,
  description: publicationDescriptionValidator,
  logo: fileUploadValidator
};
