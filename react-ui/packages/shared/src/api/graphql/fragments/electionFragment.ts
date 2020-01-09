import gql from 'graphql-tag';

export const ElectionFragment = gql`
  fragment Election on Election {
    oid 
    type
    status
    nominationStartDatetime
    availableSlots
    nomineeCount
  }
`;
