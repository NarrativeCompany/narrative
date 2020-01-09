import { MethodError } from './errorUtils';

export interface SimpleFormState extends MethodError {
  isSubmitting?: boolean;
}

export const initialFormState: MethodError = {
  methodError: null,
};
