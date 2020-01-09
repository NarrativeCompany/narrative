import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { ledgerEntriesByChannelQuery } from '../graphql/ledgerEntry/ledgerEntriesByChannelQuery';
import { LedgerEntriesByChannelQuery, LedgerEntriesByChannelQueryVariables } from '../../types';
import {
  createLedgerEntriesPropsFromQueryResults, extractLedgerEntriesFilters,
  LedgerEntriesParentProps,
  WithLedgerEntriesProps
} from './ledgerEntriesUtils';

const functionName = 'ledgerEntriesByChannelData';

interface ParentProps extends LedgerEntriesParentProps {
  channelOid: string;
}

type WithProps =
  NamedProps<{[functionName]: GraphqlQueryControls & LedgerEntriesByChannelQuery}, ParentProps>;

export const withLedgerEntriesByChannel =
  graphql<
    ParentProps,
    LedgerEntriesByChannelQuery,
    LedgerEntriesByChannelQueryVariables,
    WithLedgerEntriesProps
  >(ledgerEntriesByChannelQuery, {
    options: (props: ParentProps) => ({
      variables: {
        filters: extractLedgerEntriesFilters(props),
        channelOid: props.channelOid
      }
    }),
    name: functionName,
    props: ({ ledgerEntriesByChannelData }: WithProps) => {
      return createLedgerEntriesPropsFromQueryResults('getLedgerEntriesByChannel', ledgerEntriesByChannelData);
    }
  });
