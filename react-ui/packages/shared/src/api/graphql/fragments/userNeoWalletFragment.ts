import gql from 'graphql-tag';
import { NrveValueDetailImplFragment } from './nrveValueDetailImplFragment';

export const UserNeoWalletFragment = gql`
  fragment UserNeoWallet on UserNeoWallet {
    oid
    neoAddress
    redemptionStatus
    waitingPeriodEndDatetime
    currentBalance @type(name: "NrveValueDetailImpl") {
      ...NrveValueDetailImpl
    }
  }
  ${NrveValueDetailImplFragment}
`;
