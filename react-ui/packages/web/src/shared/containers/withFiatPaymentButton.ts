import { compose, withHandlers, withProps } from 'recompose';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import {
  FiatPaymentProcessorType,
  withState,
  WithStateProps,
} from '@narrative/shared';
import { SharedComponentMessages } from '../i18n/SharedComponentMessages';
import { showValidationErrorDialogIfNecessary } from '../utils/webErrorUtils';

interface State {
  processingPayment: boolean;
}

export interface WithFiatPaymentButtonProps {
  onFiatPaymentToken: (paymentToken: string, processorType: FiatPaymentProcessorType) => void;
  processingPayment: boolean;
}

interface ParentProps {
  processFiatPaymentToken: (paymentToken: string, processorType: FiatPaymentProcessorType) => boolean;
}

export const withFiatPaymentButton = compose(
  withState({
    processingPayment: false
  }),
  injectIntl,
  withHandlers({
    onFiatPaymentToken:
      (props: ParentProps & WithStateProps<State> & InjectedIntlProps) =>
      async (paymentToken: string, processorType: FiatPaymentProcessorType) =>
    {
      const { processFiatPaymentToken, setState, intl: { formatMessage } } = props;

      // jw: let the parent know about the fact that the payment is starting to be processed.
      setState(ss => ({...ss, processingPayment: true}));
      try {
        const success = await processFiatPaymentToken(paymentToken, processorType);

        // jw: we are not updating the state for successful requests because we are assuming that the processor will be
        //     triggering a rerender/redirect as part of the successful handling.
        if (!success) {
          setState(ss => ({...ss, processingPayment: false}));
        }

      // jw: in the event of an error, stop processing and show it to the user.
      } catch (error) {
        setState(ss => ({...ss, processingPayment: false}));

        showValidationErrorDialogIfNecessary(formatMessage(SharedComponentMessages.FormErrorTitle), error);
      }
    }
  }),
  withProps((props: WithStateProps<State>) => {
    // jw: to ease all consumers, let's pull the processingPayment flag out of the state. This makes the signature much
    //     simpler.
    const { processingPayment } = props.state;

    return { processingPayment };
  })
);
