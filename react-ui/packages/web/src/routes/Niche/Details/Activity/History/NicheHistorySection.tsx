import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { NicheDetailsMessages } from '../../../../../shared/i18n/NicheDetailsMessages';
import { SectionHeader } from '../../../../../shared/components/SectionHeader';
import { WithNicheDetailsContextProps } from '../../components/NicheDetailsContext';
import { compose, withProps } from 'recompose';
import { withLedgerEntriesByChannel } from '@narrative/shared';
import {
  LedgerEntryList,
  withLedgerEntryListPropsFromQuery,
  WithLedgerEntryListPropsFromQueryProps
} from '../../../../../shared/components/ledgerEntry/LedgerEntryList';

const NicheHistorySectionComponent: React.SFC<WithLedgerEntryListPropsFromQueryProps> = (props) => {
  const { ledgerEntryListProps } = props;

  return (
    <React.Fragment>
      <SectionHeader title={<FormattedMessage {...NicheDetailsMessages.History} />}/>

      <LedgerEntryList {...ledgerEntryListProps}/>
    </React.Fragment>
  );
};

export const NicheHistorySection = compose(
  withProps((props: WithNicheDetailsContextProps) => {
    // jw: we never would have gotten here if we didn't have a resolved niche!
    return { channelOid: props.nicheDetail.niche.oid };
  }),
  withLedgerEntriesByChannel,
  withLedgerEntryListPropsFromQuery
)(NicheHistorySectionComponent) as React.ComponentClass<WithNicheDetailsContextProps>;
