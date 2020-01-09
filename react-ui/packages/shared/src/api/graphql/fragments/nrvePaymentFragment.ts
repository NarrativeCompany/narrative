import gql from 'graphql-tag';

export const NrvePaymentFragment = gql`
  fragment NrvePayment on NrvePayment {
    oid
    nrveAmount
    transactionDate
    transactionId
    hasBeenPaid
    fromNeoAddress
    paymentStatus
    paymentNeoAddress
  }
`;
