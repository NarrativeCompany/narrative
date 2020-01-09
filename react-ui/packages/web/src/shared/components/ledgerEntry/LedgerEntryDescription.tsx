import * as React from 'react';
import { LedgerEntryProps } from './LedgerEntryListItem';
import { LocalizedTime } from '../LocalizedTime';

export const LedgerEntryDescription: React.SFC<LedgerEntryProps> = (props) => {
  const { ledgerEntry } = props;

  return <LocalizedTime time={ledgerEntry.eventDatetime}/>;
};
