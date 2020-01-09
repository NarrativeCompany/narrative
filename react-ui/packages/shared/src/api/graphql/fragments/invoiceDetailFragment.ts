import gql from 'graphql-tag';
import { NrvePaymentFragment } from './nrvePaymentFragment';
import { FiatPaymentFragment } from './fiatPaymentFragment';
import { NicheAuctionInvoiceFragment } from './nicheAuctionInvoiceFragment';

export const InvoiceDetailFragment = gql`
  fragment InvoiceDetail on InvoiceDetail {
    oid
    type
    status
    invoiceDatetime
    paymentDueDatetime
    updateDatetime

    nrveAmount
    usdAmount
    
    nrvePayment @type(name: "NrvePayment") {
      ...NrvePayment
    } 
    fiatPayment @type(name: "FiatPayment") {
      ...FiatPayment
    }
    nicheAuctionInvoice @type(name: "NicheAuctionInvoice") {
      ...NicheAuctionInvoice
    }
  }
  ${NrvePaymentFragment}
  ${FiatPaymentFragment}
  ${NicheAuctionInvoiceFragment}
`;
