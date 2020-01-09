import * as React from 'react';
import { compose } from 'recompose';
import {
  withLedgerEntriesByUser
} from '@narrative/shared';
import {
  LedgerEntryList,
  withLedgerEntryListPropsFromQuery, WithLedgerEntryListPropsFromQueryProps
} from '../../../shared/components/ledgerEntry/LedgerEntryList';
import { WithMemberProfileProps } from '../../../shared/context/MemberProfileContext';
import {
  withExtractedUserOidFromMemberProfileProps
} from '../../../shared/containers/withExtractedUserOidFromMemberProfileProps';

const MemberActivityLedgerEntryListComponent: React.SFC<WithLedgerEntryListPropsFromQueryProps> = (props) => {
  const { ledgerEntryListProps } = props;

  return <LedgerEntryList {...ledgerEntryListProps} forProfilePage={true} />;
};

export const MemberActivityLedgerEntryList = compose(
  withExtractedUserOidFromMemberProfileProps,
  withLedgerEntriesByUser,
  withLedgerEntryListPropsFromQuery
)(MemberActivityLedgerEntryListComponent) as React.ComponentClass<WithMemberProfileProps>;
