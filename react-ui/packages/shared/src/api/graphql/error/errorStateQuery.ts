import gql from 'graphql-tag';
import { ErrorStateFragment } from '../fragments/errorStateFragment';

export const errorStateQuery = gql`
  query ErrorStateQuery {
    errorState @client {
      ...ErrorState
    }
  }
  ${ErrorStateFragment}
`;
