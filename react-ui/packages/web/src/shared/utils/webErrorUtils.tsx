import {
  logException,
  mergeErrorsIntoString,
  RequestTerminatedException,
  ValidationErrorException
} from '@narrative/shared';
import { openNotification } from './notificationsUtil';

/**
 * Show a notification for a validation exception - eat the exception and log otherwise
 */
// tslint:disable-next-line no-any
export const showValidationErrorDialogIfNecessary = (message: any, exception: Error) => {
  if (exception instanceof ValidationErrorException) {
    openNotification.updateFailed(
      exception,
      {
        message,
        description: mergeErrorsIntoString(exception)
      }
    );
  } else if (!(exception instanceof RequestTerminatedException)) {
    logException('Non-validation error encountered', exception);
  }
};
