import { GraphQLError } from 'graphql';
import { ApolloError } from 'apollo-client';
import { ErrorState, ErrorType, HttpStatusCode } from '../types';
import { normalizeFieldErrors, normalizeMethodErrors } from './normalizeErrors';
import { SetStateViaValueOrFunction } from '../containers';
import { buildEmptyErrorState } from '../api/state/resolvers';

const VALIDATIONERROR_TYPE = 'ValidationError';
const ERROR_TYPE = 'Error';

/**
 * Container for Apollo client error information
 */
export interface ApolloErrorInfo {
  httpStatusCode: number | null;
  url: string | null;
  result: string | null;
  data: string | null;
  graphQLErrors: string | null;
  stack: string | null;
}

/**
 * Container for field errors
 */
export interface FieldError {
  name: string;
  messages: [string];
}

/**
 * Container for methodError property
 */
export interface MethodError {
  methodError: string[] | null;
}

/**
 * Container for "front of the front" consumable validation error information
 */
export interface ValidationError {
  methodErrors?: string;
  fieldErrors?: {[key: string]: string };
  apolloErrorInfo?: ApolloErrorInfo;
}

// tslint:disable:max-classes-per-file

/**
 * Exception to be thrown by Apollo level mutations etc. that can be handled by front end code to display validation
 * errors.
 */
export class ValidationErrorException extends Error {
  public validationError: ValidationError;

  constructor(validationError: ValidationError) {
    super();
    this.validationError = validationError;
    Object.setPrototypeOf(this, ValidationErrorException.prototype);
  }
}

/**
 * Exception to be thrown by Apollo level mutations etc. when a request error occured and was handled globally by the
 * Apollo error handler.  The payload of this exception is informational in case there is a need to introspect or
 * display details for special use cases.
 */
export class RequestTerminatedException extends Error {
  public errorState: ErrorState;

  constructor(errorState: ErrorState) {
    super();
    this.errorState = errorState;
    Object.setPrototypeOf(this, RequestTerminatedException.prototype);
  }
}

/**
 * DTO for returning reshaped error information
 */
export class ErrorResult {
  private errorState: ErrorState | null;
  private validationError: ValidationError | null;

  constructor(errorState: ErrorState | null = null, validationError: ValidationError | null = null) {
    this.errorState = errorState;
    this.validationError = validationError;
  }

  isValidationError() {
    return (this.validationError);
  }

  getValidationError() {
    if (!this.validationError) {
      throw new Error('getValidationError() should never be called for null - call isValidationError() first');
    }
    return this.validationError;
  }

  isErrorState() {
    return (this.errorState);
  }

  getErrorState(): ErrorState {
    if (!this.errorState) {
      throw new Error('getErrorState() should never be called for null - call isValidationError() first');
    }
    return this.errorState;
  }
}

// tslint:enable:max-classes-per-file

/**
 * Reshape error data of all types into a single shape that can be consumed by the client.
 */
// tslint:disable-next-line no-any
// tslint:disable no-string-literal
export const reshapeErrorData =
  // tslint:disable-next-line no-any
  (data?: any, graphQLErrors?: ReadonlyArray<GraphQLError>, networkError?: Error): ErrorResult => {

  const apolloErrorInfo = {} as ApolloErrorInfo;

  if (data){
    apolloErrorInfo.data = JSON.stringify(data) || null;
  }

  if (graphQLErrors) {
    apolloErrorInfo.graphQLErrors = JSON.stringify(graphQLErrors) || null;
  }

  if (networkError) {
    apolloErrorInfo.httpStatusCode = networkError['statusCode'] as HttpStatusCode || null;
    apolloErrorInfo.stack = networkError['stack'] && JSON.stringify(networkError['stack']) || null;

    const result = networkError['result'] || null;
    apolloErrorInfo.result = result && JSON.stringify(result) || null;

    const response = networkError['response'] || null;
    if (response) {
      // This is actually the request URL :/
      apolloErrorInfo.url = response['url'];
    }

    if (result) {
      // Extract the error type from the JSON
      const type = result['_type'];

      // Validation error case
      if (VALIDATIONERROR_TYPE === type) {
        const validationError = {} as ValidationError;
        const methodErrors = result['methodErrors'];
        if (methodErrors) {
          validationError.methodErrors = normalizeMethodErrors(methodErrors);
        }
        const fieldErrors = result['fieldErrors'];
        if (fieldErrors) {
          validationError.fieldErrors = normalizeFieldErrors(fieldErrors);
        }
        validationError.apolloErrorInfo = apolloErrorInfo;
        return new ErrorResult(undefined, validationError);
      }

      // If no validation error then this is a "general" error
      const errorState = buildErrorState(apolloErrorInfo);
      if (ERROR_TYPE === type) {
        errorState.type = result['type'] || null;
        errorState.title = result['title'] || null;
        errorState.message = result['message'] || null;
        errorState.referenceId = result['referenceId'] || null;
        errorState.detail = result['detail'] && JSON.stringify(result['detail']) || null;

      } else if ( // Server not available
          HttpStatusCode.BAD_GATEWAY === apolloErrorInfo.httpStatusCode ||
          HttpStatusCode.SERVICE_UNAVAILABLE === apolloErrorInfo.httpStatusCode ||
          HttpStatusCode.GATEWAY_TIMEOUT === apolloErrorInfo.httpStatusCode
        ) {
        errorState.type = ErrorType.SERVER_UNREACHABLE;
        // tslint:disable-next-line no-console
        console.error(errorState.message, errorState);
        return new ErrorResult(errorState);
      } else {
        // TODO: localize yup validation messages #894
        errorState.message = 'Unhandled network error result type encountered';
        // tslint:disable-next-line no-console
        console.error(errorState.message, errorState);
      }
      return new ErrorResult(errorState);
    } else { // No result in NetworkError case - should never get here
      const errorState = buildErrorState(apolloErrorInfo);
      // TODO: localize yup validation messages #894
      errorState.message = 'NetworkError with no result encountered';
      // tslint:disable-next-line no-console
      console.error(errorState.message, errorState);
      return new ErrorResult(errorState);
    }

  } else { // Non-NetworkError case - should never get here
    const errorState = buildErrorState(apolloErrorInfo);
    // TODO: localize yup validation messages #894
    errorState.message = 'Unexpected non-NetworkError error type encountered';
    // tslint:disable-next-line no-console
    console.error(errorState.message, errorState);
    return new ErrorResult(errorState);
  }
};
// tslint:enable no-string-literal

const buildErrorState = (apolloErrorInfo: ApolloErrorInfo) => {
  const empty = buildEmptyErrorState();
  return Object.assign({...empty}, apolloErrorInfo);
};

/**
 * Extract the exception from an ApolloError if present.  ErrorLink always wraps the returned error in an ApolloError.
 */
// tslint:disable-next-line no-any
export const resolveExceptionFromApolloError = (error: any) => {
  let exception;
  if (error instanceof ApolloError) {
    // Our custom exception is attached to ApolloError as networkError by our errorLink onError
    exception =
      error &&
      error.networkError;
  }

  if (exception) {
    return exception;
  }

  const message = 'No exception found in error';
  // tslint:disable-next-line no-console
  console.error(message, error);
  return new Error('No exception found in error');
};

/**
 * Extract field errors if present
 */
export const extractFieldErrors = (exception: Error) => {
  let fieldErrors = null;

  if (exception instanceof ValidationErrorException) {
    fieldErrors =
      exception.validationError &&
      exception.validationError.fieldErrors;
  }

  return fieldErrors;
};

/**
 * Extract method error if present
 */
export const extractMethodError = (exception: Error): string | null => {
  // jw: default to null here.
  let methodError = null;

  if (exception instanceof ValidationErrorException) {
    // jw: we need to first get the value off of the exception, which can be undefined here.
    const errorResult =
      exception &&
      exception.validationError &&
      exception.validationError.apolloErrorInfo &&
      exception.validationError.apolloErrorInfo.result;

    if (errorResult) {
      // tslint:disable-next-line no-string-literal
      const parsedMethodErrors = JSON.parse(errorResult)['methodErrors'];

      if (parsedMethodErrors && parsedMethodErrors.length) {
        methodError = parsedMethodErrors.map((message: string) => `• ${message}`);
      }
    }
  }

  return methodError;
};

/**
 * Apply an exception to UI state.  State "methodError" should be available in the calling UI if using this helper.
 */
export const applyExceptionToState =
  // tslint:disable-next-line no-any
  (exception: Error, setErrors: any, setState: SetStateViaValueOrFunction<MethodError, {}>) => {
    // Ignore RequestTerminatedException - these will be handled by a global modal
    if (exception instanceof RequestTerminatedException){
      return;
    }

    if (exception instanceof ValidationErrorException) {
      // If exception has field errors, set via Formik setErrors
      applyExceptionFieldErrorsToState(exception, setErrors);

      // If exception has method errors, set on caller's methodError state
      applyExceptionMethodErrorToState(exception, setState);
    } else {
      // TODO: What do we do in the case of an unhandled error?
      // Unhandled exception - lets at least try to display something
      if (exception.message) {
        setState(ss => ({ ...ss, methodError: [exception.message]}));
      } else {
        logException('Unhandled exception encountered', exception);
      }
  }
};

/**
 * Apply field errors to UI state if present.
 */
export const applyExceptionFieldErrorsToState =
  // tslint:disable-next-line no-any
  (exception: Error, setErrors: any, logExceptionToConsole: boolean = false) => {
    if (exception instanceof ValidationErrorException) {
      // If exception has field errors, set via Formik setErrors
      const fieldErrors = extractFieldErrors(exception);

      if (fieldErrors) {
        setErrors(fieldErrors);
      }
    } else if (logExceptionToConsole) {
      logException('Unexpected error in applyExceptionFieldErrorsToState', exception);
    }
};

/**
 * Apply method error to UI state if present.
 */
export const applyExceptionMethodErrorToState =
  (exception: Error, setState: SetStateViaValueOrFunction<MethodError, {}>, logExceptionToConsole: boolean = false) => {
    if (exception instanceof ValidationErrorException) {
      const methodError = extractMethodError(exception);

      if (methodError) {
        setState(ss => ({ ...ss, methodError: [methodError] }));
      }
    } else if (logExceptionToConsole) {
      logException('Unexpected error in applyExceptionMethodErrorToState', exception);
    }
};

/**
 * Merge all errors into a single string
 */
export const mergeErrorsIntoString = (exception: Error) => {
  let res = '';

  if (exception instanceof ValidationErrorException) {
    const fieldErrors = extractFieldErrors(exception);

    if (fieldErrors) {
      res = Object.keys(fieldErrors).map(key => {
        return key + ': ' + fieldErrors[key];
      }).join('.  ');
    }

    // If exception has method errors, set on caller's methodError state
    const methodError = extractMethodError(exception);
    if (methodError) {
      res = res ? res + '.  ' + methodError : methodError;
    }
  }

  return res;
};

/**
 * Log an exception to the console
 */
export const logException = (message: string, exception: Error) => {
  if (exception) {
    let errMessage = `\nException information:\n`;

    if (exception instanceof ValidationErrorException) {
      errMessage += JSON.stringify(exception.validationError);
    } else if (exception instanceof RequestTerminatedException) {
      errMessage += JSON.stringify(exception.errorState);
    } else {
      errMessage += JSON.stringify(exception);
    }

    // tslint:disable-next-line no-console
    console.error(message,  errMessage + `\nCaused By:\n` + exception.stack);
  }
};

export async function handleFormlessServerOperation<R>(operation: () => R): Promise<R | null> {
  try {
    return await operation();

  } catch (e) {
    // jw: capture and ignore any RequestTerminatedExceptions since the underlying error will have been handled by
    //   the global error boundary.
    if (e instanceof RequestTerminatedException){
      return null;
    }
    throw e;
  }
}

/**
 * Test if an exception has a specific field error
 */
export const errorContainsFieldError = (error: Error, fieldName: string) => {
  if (error instanceof ValidationErrorException) {
    const {validationError: {fieldErrors}} = error;
    if (fieldErrors) {
      // tslint:disable no-string-literal
      return !!(fieldErrors[fieldName]);
    }
  }
  return false;
};
