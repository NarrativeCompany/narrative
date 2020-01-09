import { FieldError } from './errorUtils';

export function normalizeFieldErrors (fieldErrors: FieldError[] | null) {
  const errMap: { [key: string]: string } = {};

  if (!fieldErrors) {
    return {};
  }

  fieldErrors.forEach(fieldError => {
    return errMap[fieldError.name] = fieldError.messages.join('. ');
  });

  return errMap;
}

export function normalizeMethodErrors (methodErrors: string[]) {
  return methodErrors.join('. ');
}
