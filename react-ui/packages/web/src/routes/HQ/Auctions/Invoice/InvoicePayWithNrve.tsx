import * as React from 'react';
import { compose } from 'recompose';
import { Form as AntForm } from 'antd';
import { InvoiceProps } from './AuctionInvoice';
import { Card } from '../../../../shared/components/Card';
import { Link } from '../../../../shared/components/Link';
import { GetNrveForPurchasingSection } from './GetNrveForPurchasingSection';
import { Paragraph } from '../../../../shared/components/Paragraph';
import { FormattedMessage } from 'react-intl';
import { Form, withFormik, FormikProps } from 'formik';
import { InvoiceMessages } from '../../../../shared/i18n/InvoiceMessages';
import { HandleForcePayWithNrveCallback } from './InvoicePaymentOptions';
import { Icon, Tooltip } from 'antd';
import { Heading } from '../../../../shared/components/Heading';
import { Button } from '../../../../shared/components/Button';
import { FlexContainer } from '../../../../shared/styled/shared/containers';
import { InputField } from '../../../../shared/components/InputField';
import { FormMethodError } from '../../../../shared/components/FormMethodError';
import styled from '../../../../shared/styled';
import {
  putNeoAddressUtil,
  NeoAddressValues,
  WithPutInvoiceNrvePaymentProps,
  withPutInvoiceNrvePayment,
  applyExceptionFieldErrorsToState,
  MethodError,
  initialFormState,
  withState,
  WithStateProps,
  NrvePayment,
  InvoiceDetail
} from '@narrative/shared';
import { HandleInvoiceUpdateCallback } from './InvoiceDetails';
import { SharedComponentMessages } from '../../../../shared/i18n/SharedComponentMessages';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import { showValidationErrorDialogIfNecessary } from '../../../../shared/utils/webErrorUtils';

const FormItem = AntForm.Item;

type ParentProps =
  InvoiceProps &
  HandleInvoiceUpdateCallback &
  HandleForcePayWithNrveCallback;

type Props =
  ParentProps &
  FormikProps<NeoAddressValues> &
  WithPutInvoiceNrvePaymentProps &
  WithStateProps<MethodError> &
  InjectedIntlProps;

const SelectPaymentContainer = styled(Heading)`
  margin-bottom: 10px;
`;

const InvoicePayWithNrveComponent: React.SFC<Props> = (props) => {
  const { invoice, handleForcePayWithNrve, state, isSubmitting } = props;

  const contactLink =
    <Link.Anchor href="mailto:support@narrative.org">support@narrative.org</Link.Anchor>;
  // todo: we need to use Text.tsx once we have it.
  const questions = (
    <strong>
      <FormattedMessage {...InvoiceMessages.PaymentQuestions}/>
    </strong>
  );

  const nrveLink = <Link.About type="nrve"><FormattedMessage {...InvoiceMessages.NeoWallet}/></Link.About>;

  const neoAddressLabel = (
    <Tooltip title={<FormattedMessage {...InvoiceMessages.NeoAddressTooltip} values={{nrveLink}}/>}>
      <span><FormattedMessage {...InvoiceMessages.NeoAddress}/></span>
    </Tooltip>
  );

  return (
    <React.Fragment>
      {invoice.fiatPayment &&
      <SelectPaymentContainer
        isLink={true}
        size={4}
        onClick={() => {
          if (handleForcePayWithNrve) {
            handleForcePayWithNrve(false);
          }
        }}>
        <Icon type="left" />
        <FormattedMessage {...InvoiceMessages.SelectPaymentMethod}/>
      </SelectPaymentContainer>}

      <Card>
        <Paragraph marginBottom="large">
          <FormattedMessage {...InvoiceMessages.InOrderToSubmitNrvePayment}/>
        </Paragraph>

        <FlexContainer centerAll={true}>
          <Form style={{width: '100%'}}>
            <FormMethodError methodError={state.methodError}/>

            <InputField
              type="input"
              label={neoAddressLabel}
              name="neoAddress"
            />

            <FormItem style={{textAlign: 'center'}}>
              <Button size="default" type="primary" htmlType="submit" loading={isSubmitting}>
                <FormattedMessage {...InvoiceMessages.StartPaymentProcess}/>
              </Button>
            </FormItem>
          </Form>
        </FlexContainer>

        <GetNrveForPurchasingSection invoice={invoice}/>

        <Paragraph>
          <FormattedMessage {...InvoiceMessages.PaymentContact} values={{questions, contactLink}}/>
        </Paragraph>
      </Card>
    </React.Fragment>
  );
};

export const InvoicePayWithNrve = compose(
  withState<MethodError>(initialFormState),
  withPutInvoiceNrvePayment,
  injectIntl,
  withFormik<Props, NeoAddressValues>({
    ...putNeoAddressUtil,
    handleSubmit: async (values, {props, setErrors, setSubmitting}) => {
      const { invoice, putInvoiceNrvePayment, handleInvoiceUpdate, intl, setState, isSubmitting } = props;

      if (isSubmitting) {
        return;
      }

      setState(ss => ({...ss, methodError: null}));

      // jw: we don't really care about the response object. As long as this returns, then we got a 200 OK, and
      //     life is good.
      try {
        const input: NeoAddressValues = {
          neoAddress: values.neoAddress
        };
        const nrvePayment: NrvePayment = await putInvoiceNrvePayment(input, invoice.oid);

        if (handleInvoiceUpdate) {
          const newInvoice: InvoiceDetail = {
            ...invoice,
            nrvePayment
          };
          handleInvoiceUpdate(newInvoice);
        }
      } catch (error) {
        applyExceptionFieldErrorsToState(error, setErrors);
        setState(ss => ({...ss, isSubmitting: false}));
        showValidationErrorDialogIfNecessary(intl.formatMessage(SharedComponentMessages.FormErrorTitle), error);
      }

      setSubmitting(false);
    }
  })
)(InvoicePayWithNrveComponent) as React.ComponentClass<ParentProps>;
