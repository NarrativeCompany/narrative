import * as React from 'react';
import { compose, withProps } from 'recompose';
import { Publication, withLedgerEntriesByChannel } from '@narrative/shared';
import {
  LedgerEntryList,
  withLedgerEntryListPropsFromQuery,
  WithLedgerEntryListPropsFromQueryProps
} from '../../../../../../shared/components/ledgerEntry/LedgerEntryList';

interface ParentProps {
  publication: Publication;
}

const PublicationLedgerEntriesComponent: React.SFC<WithLedgerEntryListPropsFromQueryProps> = (props) => {
  const { ledgerEntryListProps } = props;

  return <LedgerEntryList {...ledgerEntryListProps}/>;
};

export const PublicationLedgerEntries = compose(
  withProps((props: ParentProps) => {
    const { publication: { oid } } = props;
    return { channelOid: oid };
  }),
  withLedgerEntriesByChannel,
  withLedgerEntryListPropsFromQuery
)(PublicationLedgerEntriesComponent) as React.ComponentClass<ParentProps>;
