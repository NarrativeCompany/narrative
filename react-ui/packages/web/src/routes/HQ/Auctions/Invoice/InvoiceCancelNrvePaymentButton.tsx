import * as React from 'react';
import { compose } from 'recompose';
import { InvoiceProps } from './AuctionInvoice';
import { FormattedMessage } from 'react-intl';
import { Form, withFormik, FormikProps } from 'formik';
import { Form as AntForm } from 'antd';
import { Button } from '../../../../shared/components/Button';
import { FlexContainer } from '../../../../shared/styled/shared/containers';
import { FormMethodError } from '../../../../shared/components/FormMethodError';
import {
  WithDeleteInvoiceNrvePaymentProps,
  withDeleteInvoiceNrvePayment,
  applyExceptionFieldErrorsToState,
  MethodError,
  initialFormState,
  withState,
  WithStateProps,
  InvoiceDetail
} from '@narrative/shared';
import { HandleInvoiceUpdateCallback } from './InvoiceDetails';
import { InvoiceNrvePaymentProcessPolling } from './InvoiceNrvePaymentProcessPolling';
import { SharedComponentMessages } from '../../../../shared/i18n/SharedComponentMessages';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import { showValidationErrorDialogIfNecessary } from '../../../../shared/utils/webErrorUtils';

const FormItem = AntForm.Item;

type ParentProps =
  InvoiceProps &
  HandleInvoiceUpdateCallback;

type Props =
  ParentProps &
  FormikProps<{}> &
  WithDeleteInvoiceNrvePaymentProps &
  InjectedIntlProps &
  WithStateProps<MethodError>;

const InvoiceCancelNrvePaymentComponent: React.SFC<Props> = (props) => {
  const { invoice, handleInvoiceUpdate, state, isSubmitting } = props;

  return (
    <FlexContainer centerAll={true}>
      <Form style={{width: '100%'}}>
        <FormMethodError methodError={state.methodError}/>

        <FormItem style={{textAlign: 'center'}}>
          <Button size="default" type="danger" htmlType="submit" loading={isSubmitting}>
            <FormattedMessage {...SharedComponentMessages.Cancel}/>
          </Button>
        </FormItem>
      </Form>
      {/* jw: since we are allowing them to close it, let's also check to see it gets paid by the processor */}
      <InvoiceNrvePaymentProcessPolling invoice={invoice} handleInvoiceUpdate={handleInvoiceUpdate}/>
    </FlexContainer>
  );
};

export const InvoiceCancelNrvePaymentButton = compose(
  withState<MethodError>(initialFormState),
  withDeleteInvoiceNrvePayment,
  injectIntl,
  withFormik<Props, {}>({
    handleSubmit: async ({}, {props, setErrors, setSubmitting}) => {
      const { invoice, deleteInvoiceNrvePayment, handleInvoiceUpdate, setState, intl, isSubmitting } = props;

      if (isSubmitting) {
        return;
      }

      setState(ss => ({...ss, methodError: null}));

      try {
        const newInvoice: InvoiceDetail = await deleteInvoiceNrvePayment(invoice.oid);

        if (handleInvoiceUpdate) {
          handleInvoiceUpdate(newInvoice);
        }
      } catch (error) {
        applyExceptionFieldErrorsToState(error, setErrors);
        showValidationErrorDialogIfNecessary(intl.formatMessage(SharedComponentMessages.FormErrorTitle), error);
      }

      setSubmitting(false);
    }
  })
)(InvoiceCancelNrvePaymentComponent) as React.ComponentClass<ParentProps>;
