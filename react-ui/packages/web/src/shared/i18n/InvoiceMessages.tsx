import { defineMessages } from 'react-intl';

export const InvoiceMessages = defineMessages({
  TitleForNiche: {
    id: 'invoiceDetails.titleForNiche',
    defaultMessage: 'Invoice For {nicheName}'
  },
  Summary: {
    id: 'invoiceDetails.summary',
    defaultMessage: 'Summary'
  },
  InvoiceId: {
    id: 'invoiceDetails.invoiceId',
    defaultMessage: 'Invoice ID'
  },
  Invoiced: {
    id: 'invoiceDetails.invoiced',
    defaultMessage: 'Invoiced'
  },
  ForNiche: {
    id: 'invoiceDetails.forNiche',
    defaultMessage: 'For Niche'
  },
  Amount: {
    id: 'invoiceDetails.amount',
    defaultMessage: 'Amount'
  },
  PaymentDueBy: {
    id: 'invoiceDetails.paymentDueBy',
    defaultMessage: 'Payment Due By'
  },
  Paid: {
    id: 'invoiceDetails.paid',
    defaultMessage: 'Paid'
  },
  Expired: {
    id: 'invoiceDetails.expired',
    defaultMessage: 'Expired'
  },
  Chargeback: {
    id: 'invoiceDetails.chargeback',
    defaultMessage: 'Chargeback'
  },
  Refunded: {
    id: 'invoiceDetails.refunded',
    defaultMessage: 'Refunded'
  },
  Canceled: {
    id: 'invoiceDetails.canceled',
    defaultMessage: 'Canceled'
  },
  FiatAmount: {
    id: 'invoiceDetails.fiatAmount',
    defaultMessage: ' ({fiatPayment} USD)'
  },
  PaymentInProgress: {
    id: 'invoiceStatusTag.paymentInProgress',
    defaultMessage: 'Payment In Progress'
  },
  'invoiceStatusTag.status.INVOICED': {
    id: 'invoiceStatusTag.status.INVOICED',
    defaultMessage: 'Invoiced'
  },
  'invoiceStatusTag.status.PAID': {
    id: 'invoiceStatusTag.status.PAID',
    defaultMessage: 'Paid'
  },
  'invoiceStatusTag.status.EXPIRED': {
    id: 'invoiceStatusTag.status.EXPIRED',
    defaultMessage: 'Expired'
  },
  'invoiceStatusTag.status.CHARGEBACK': {
    id: 'invoiceStatusTag.status.CHARGEBACK',
    defaultMessage: 'Chargeback'
  },
  'invoiceStatusTag.status.REFUNDED': {
    id: 'invoiceStatusTag.status.REFUNDED',
    defaultMessage: 'Refunded'
  },
  'invoiceStatusTag.status.CANCELED': {
    id: 'invoiceStatusTag.status.CANCELED',
    defaultMessage: 'Canceled'
  },
  Select: {
    id: 'invoicePaymentCard.select',
    defaultMessage: 'Select'
  },
  PayWithNrve: {
    id: 'invoicePayWithNrveOrCard.payWithNrve',
    defaultMessage: 'Pay with {nrveLink}'
  },
  HowWouldYouLikeToPay: {
    id: 'invoicePayWithNrveOrCard.howWouldYouLikeToPay',
    defaultMessage: 'How would you like to pay?'
  },
  NoFees: {
    id: 'invoicePayWithNrveOrCard.noFees',
    defaultMessage: 'No Fees'
  },
  PayWithPayPal: {
    id: 'invoicePayWithNrveOrCard.payWithPayPal',
    defaultMessage: 'Pay with PayPal'
  },
  PaymentValueByCard: {
    id: 'invoicePayWithNrveOrCard.paymentValueByCard',
    defaultMessage: '{fiatPayment} USD'
  },
  IncludesConvenienceFee: {
    id: 'invoicePayWithNrveOrCard.includesConvenienceFee',
    defaultMessage: 'Includes 15% convenience fee of {convenienceFee}'
  },
  IncludesConvenienceFeeTooltip: {
    id: 'invoicePayWithNrveOrCard.includesConvenienceFeeTooltip',
    defaultMessage: 'The 15% convenience fee covers card processing fees, exchange fees, and exchange rate risk.'
  },
  SelectPaymentMethod: {
    id: 'invoicePayWithNrve.selectPaymentMethod',
    defaultMessage: 'Select Payment Method'
  },
  InOrderToSubmitNrvePayment: {
    id: 'invoicePayWithNrve.inOrderToSubmitNrvePayment',
    defaultMessage: 'In order to submit your payment, please provide the NEO Address from which you will make your ' +
    'payment below.'
  },
  NeoAddress: {
    id: 'invoicePayWithNrve.neoAddress',
    defaultMessage: 'NEO Address'
  },
  NeoWallet: {
    id: 'invoicePayWithNrve.neoWallet',
    defaultMessage: 'NEO wallet'
  },
  NeoAddressTooltip: {
    id: 'invoicePayWithNrve.neoAddressTooltip',
    defaultMessage: 'Enter the {nrveLink} address from which you will send your payment.'
  },
  StartPaymentProcess: {
    id: 'invoicePayWithNrve.startPaymentProcess',
    defaultMessage: 'Start Payment Process'
  },
  PaymentQuestions: {
    id: 'invoicePayWithNrve.paymentQuestions',
    defaultMessage: 'Questions?'
  },
  PaymentContact: {
    id: 'invoicePayWithNrve.paymentContact',
    defaultMessage: '{questions} Contact {contactLink}'
  },
  HowDoIPayForMyNiche: {
    id: 'getNrveForNichePurchasingSection.howDoIPayForMyNiche',
    defaultMessage: 'How Do I Pay For My Niche?'
  },
  HowDoIPayForMyPublication: {
    id: 'getNrveForNichePurchasingSection.howDoIPayForMyPublication',
    defaultMessage: 'How Do I Pay For My Publication?'
  },
  HowDoIPay: {
    id: 'getNrveForNichePurchasingSection.howDoIPay',
    defaultMessage: 'How Do I Pay?'
  },
  VisitExchange: {
    id: 'getNrveForNichePurchasingSection.visitExchange',
    defaultMessage: 'Visit an exchange where {nrveLink} is listed and buy some: {latokenLink}, {switcheoLink}, ' +
    'or {bilaxyLink}'
  },
  VisitExchangeLATOKEN: {
    id: 'getNrveForNichePurchasingSection.visitExchangeLATOKEN',
    defaultMessage: 'LATOKEN'
  },
  VisitExchangeSwitcheo: {
    id: 'getNrveForNichePurchasingSection.visitExchangeSwitcheo',
    defaultMessage: 'Switcheo'
  },
  VisitExchangeBilaxy: {
    id: 'getNrveForNichePurchasingSection.visitExchangeBilaxy',
    defaultMessage: 'Bilaxy'
  },
  NewToCrypto: {
    id: 'getNrveForNichePurchasingSection.newToCrypto',
    defaultMessage: 'New to crypto? No problem! We’ve created an easy to use guide just for you: {howToBuyLink}'
  },
  HowToBuyNrve: {
    id: 'getNrveForNichePurchasingSection.howToBuyNrve',
    defaultMessage: 'How to Buy NRVE'
  },
  DontSendFromExchange: {
    id: 'getNrveForNichePurchasingSection.dontSendFromExchange',
    defaultMessage: 'Important: do not send us {nrveLink} directly from an exchange. The funds must be sent from the ' +
    'wallet address you registered with us. It must be a wallet that you control.'
  },
  YourNeoAddress: {
    id: 'invoiceNrvePaymentProcessing.yourNeoAddress',
    defaultMessage: 'your NEO address'
  },
  PaymentInstructions: {
    id: 'invoiceNrvePaymentProcessing.paymentInstructions',
    defaultMessage: 'To complete this payment, you must send {nrveValue} {nrveLink} from {yourNeoAddress} ' +
    'to the following NEO Address:'
  },
  MonitoringPaymentProgress: {
    id: 'invoiceNrvePaymentProcessing.monitoringPaymentProgress',
    defaultMessage: 'Monitoring Payment Progress'
  },
  PageWillReload: {
    id: 'invoiceNrvePaymentProcessing.pageWillReload',
    defaultMessage: 'The page will reload automatically once your payment has gone through. If you have ' +
    'already made your payment, please be patient. It can take up to 10 minutes for your payment to be processed.'
  },
  Narrative: {
    id: 'invoicePayWithCardButton.narrative',
    defaultMessage: 'Narrative'
  },
  PayWithCardDescription: {
    id: 'invoicePayWithCardButton.payWithCardDescription',
    defaultMessage: 'Niche: {nicheName}'
  },
  ProcessingPaymentTitle: {
    id: 'invoicePayWithCardButton.processingPaymentTitle',
    defaultMessage: 'Processing Payment'
  },
  ProcessingPaymentMessage: {
    id: 'invoicePayWithCardButton.processingPaymentMessage',
    defaultMessage: 'Thank you for your payment! Please be patient while we finalize your transaction.'
  },
});
