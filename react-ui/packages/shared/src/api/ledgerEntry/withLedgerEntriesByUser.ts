import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { ledgerEntriesByUserQuery } from '../graphql/ledgerEntry/ledgerEntriesByUserQuery';
import { LedgerEntriesByUserQuery, LedgerEntriesByUserQueryVariables } from '../../types';
import {
  createLedgerEntriesPropsFromQueryResults,
  extractLedgerEntriesFilters,
  LedgerEntriesParentProps, WithLedgerEntriesProps
} from './ledgerEntriesUtils';

const functionName = 'ledgerEntriesByUserData';

interface ParentProps extends LedgerEntriesParentProps {
  userOid: string;
}

type WithProps =
  NamedProps<{ledgerEntriesByUserData: GraphqlQueryControls & LedgerEntriesByUserQuery}, ParentProps>;

export const withLedgerEntriesByUser =
  graphql<
    ParentProps,
    LedgerEntriesByUserQuery,
    LedgerEntriesByUserQueryVariables,
    WithLedgerEntriesProps
  >(ledgerEntriesByUserQuery, {
    options: (props: ParentProps) => ({
      variables: {
        filters: extractLedgerEntriesFilters(props),
        userOid: props.userOid
      }
    }),
    name: functionName,
    props: ({ ledgerEntriesByUserData }: WithProps) => {
      return createLedgerEntriesPropsFromQueryResults('getLedgerEntriesByUser', ledgerEntriesByUserData);
    }
  });
