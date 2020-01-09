import * as React from 'react';
import { compose } from 'recompose';
import {
  applyExceptionToState,
  initialFormState,
  MethodError,
  SimpleFormState,
  withState,
  WithStateProps,
  removePostFromPublicationFormikUtil,
  RemovePostFromPublicationFormValues
} from '@narrative/shared';
import { Form, FormikProps, withFormik } from 'formik';
import { FormattedMessage } from 'react-intl';
import { FormMethodError } from '../FormMethodError';
import { ConfirmationModal } from '../ConfirmationModal';
import { Paragraph } from '../Paragraph';
import { Block } from '../Block';
import { SharedComponentMessages } from '../../i18n/SharedComponentMessages';
import { FormField } from '../FormField';
import { PostDetailMessages } from '../../i18n/PostDetailMessages';

export interface RemovePostFromPublicationModalProps {
  removePostFromPublicationHandler?: (message?: string) => void;
  closeModalHandler: () => void;
  pendingPublicationApproval?: boolean;
  processing?: boolean;
}

type Props =
  RemovePostFromPublicationModalProps &
  WithStateProps<MethodError> &
  FormikProps<RemovePostFromPublicationFormValues>;

const RemovePostFromPublicationModalComponent: React.SFC<Props> = (props) => {
  const {
    removePostFromPublicationHandler,
    submitForm,
    pendingPublicationApproval,
    processing,
    closeModalHandler,
    state: { methodError }
  } = props;

  return (
    <ConfirmationModal
      // bl: only visible if we have a removePostFromPublicationHandler.
      visible={!!removePostFromPublicationHandler}
      processing={processing}
      dismiss={closeModalHandler}
      onConfirmation={submitForm}
      title={pendingPublicationApproval
        ? PostDetailMessages.RejectPostFromPublication
        : PostDetailMessages.RemovePostFromPublication
      }
      btnText={pendingPublicationApproval ? PostDetailMessages.RejectPost : PostDetailMessages.RemovePost}
      btnProps={{ type: 'danger' }}
      linkText={SharedComponentMessages.Cancel}
      linkProps={{ onClick: closeModalHandler }}
    >
      <Block style={{maxWidth: 450}}>
        <Paragraph marginBottom="large">
          <FormattedMessage {...(pendingPublicationApproval
            ? PostDetailMessages.RejectPostFromPublicationConfirmation
            : PostDetailMessages.RemovePostFromPublicationConfirmation
          )} />
        </Paragraph>
        <Form>
          <FormMethodError methodError={methodError}/>

          <FormField.TextArea
            name="message"
            label={<FormattedMessage {...PostDetailMessages.RemovePostMessagePlaceholder}/>}
            rows={4}
          />
        </Form>
      </Block>
    </ConfirmationModal>
  );
};

export const RemovePostFromPublicationModal = compose(
  withState<SimpleFormState>(initialFormState),
  withFormik<Props, RemovePostFromPublicationFormValues>({
    ...removePostFromPublicationFormikUtil,
    handleSubmit: async (values, {props, setErrors, resetForm, setSubmitting}) => {
      const { isSubmitting, setState, removePostFromPublicationHandler } = props;

      if (!removePostFromPublicationHandler) {
        // todo:error-handling: We should always have a removePostFromPublicationHandler if we got into here!
        return;
      }

      if (isSubmitting) {
        return;
      }

      setState(ss => ({...ss, methodError: null}));

      try {
        // extract the message and provide it to the handler for processing.
        const { message } = values;

        await removePostFromPublicationHandler(message);

        // now that the message has been applied and the UI is updating, reset the form.
        resetForm();

      } catch (err) {
        applyExceptionToState(err, setErrors, setState);
      }

      setSubmitting(false);
    }
  }),
  )(RemovePostFromPublicationModalComponent) as React.ComponentClass<RemovePostFromPublicationModalProps>;
