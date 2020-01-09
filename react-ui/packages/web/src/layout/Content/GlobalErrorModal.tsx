import * as React from 'react';
import { compose } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { FlexContainer } from '../../shared/styled/shared/containers';
import { Paragraph } from '../../shared/components/Paragraph';
import { ErrorState, ErrorType, withClearErrorState, WithClearErrorStateProps } from '@narrative/shared';
import { GlobalErrorModalMessages } from '../../shared/i18n/GlobalErrorModalMessages';
import { ErrorModal } from '../../shared/components/ErrorModal';

interface ParentProps {
  errorState: ErrorState;
  // tslint:disable-next-line no-any
  handleDismiss: () => any;
  modalVisible: boolean;
}

type Props =
  ParentProps &
  WithClearErrorStateProps;

const GlobalErrorModalComponent: React.SFC<Props> = (props) => {
  const {
    errorState: {title, message, type},
    handleDismiss, modalVisible
  } = props;

  let titleOverride: FormattedMessage.MessageDescriptor | undefined;
  let renderError;

  if (type) {
    let errorMsg;
    switch (ErrorType[type]) {
      case ErrorType.ACCESS_DENIED:
        titleOverride = GlobalErrorModalMessages.AccessDeniedTitle;
        errorMsg = <FormattedMessage {...GlobalErrorModalMessages.ErrorTypeAccessDenied} values={{message}}/>;
        break;
      case ErrorType.EMAIL_VERIFICATION_REQUIRED:
        errorMsg =
          <FormattedMessage {...GlobalErrorModalMessages.ErrorTypeEmailVerificationRequired} values={{message}}/>;
        break;
      case ErrorType.LOGIN_REQUIRED:
        errorMsg = <FormattedMessage {...GlobalErrorModalMessages.ErrorTypeLoginRequired} values={{message}}/>;
        break;
      case ErrorType.NOT_FOUND:
        errorMsg = <FormattedMessage {...GlobalErrorModalMessages.ErrorTypeNotFound} values={{message}}/>;
        break;
      case ErrorType.TOS_AGREEMENT_REQUIRED:
        errorMsg = <FormattedMessage {...GlobalErrorModalMessages.ErrorTypeTOSRequired} values={{message}}/>;
        break;
      case ErrorType.SERVER_UNREACHABLE:
        errorMsg = <FormattedMessage {...GlobalErrorModalMessages.ErrorTypeServerUnreachable} values={{message}}/>;
        break;
      default:
        errorMsg = <FormattedMessage {...GlobalErrorModalMessages.ErrorTypeUnknownError} values={{message}}/>;
    }
    renderError = (
      <FlexContainer>
        {errorMsg}
      </FlexContainer>
    );
  } else {
     if (title) {
      renderError = (
        <React.Fragment>
          {title}
          <Paragraph>
            {message}
           </Paragraph>
        </React.Fragment>
      );
    } else if (message) {
      renderError = (
        <React.Fragment>
          <FormattedMessage {...GlobalErrorModalMessages.Error} values={{message}}/>
          <Paragraph>
            {message}
          </Paragraph>
        </React.Fragment>
      );
    } else {
       renderError = (
         <FlexContainer>
           <Paragraph>
             <FormattedMessage {...GlobalErrorModalMessages.ErrorTypeUnknownError}/>;
           </Paragraph>
         </FlexContainer>
       );
    }
  }

  return (
    <ErrorModal
      visible={modalVisible}
      dismiss={handleDismiss}
      title={<FormattedMessage {...(titleOverride || GlobalErrorModalMessages.UnhandledErrorLabel)}/>}
      description={renderError}
      gifType="robot"
      btnText={<FormattedMessage {...GlobalErrorModalMessages.UnhandledErrorDismissLabel}/>}
    />
  );
};

export const GlobalErrorModal = compose(
  withClearErrorState
)(GlobalErrorModalComponent) as React.ComponentClass<ParentProps>;
