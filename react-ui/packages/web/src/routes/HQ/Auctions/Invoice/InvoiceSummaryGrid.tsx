import * as React from 'react';
import { InvoiceMessages } from '../../../../shared/i18n/InvoiceMessages';
import { FormattedMessage } from 'react-intl';
import { SummaryGridRow } from '../../../../shared/components/SummaryGridRow';
import { SummaryGrid } from '../../../../shared/components/SummaryGrid';
import { InvoiceSummaryGridActivityDatetimeRow } from './InvoiceSummaryGridActivityDatetimeRow';
import { LocalizedTime } from '../../../../shared/components/LocalizedTime';
import { NicheLink } from '../../../../shared/components/niche/NicheLink';
import { InvoiceProps } from './AuctionInvoice';
import { USD } from '../../../../shared/components/USD';
import { NRVE } from '../../../../shared/components/NRVE';

export const InvoiceSummaryGrid: React.SFC<InvoiceProps> = (props) => {
  const { invoice } = props;

  return (
    <SummaryGrid title={<FormattedMessage {...InvoiceMessages.Summary}/>}>
      <SummaryGridRow title={<FormattedMessage {...InvoiceMessages.InvoiceId}/>}>
        {invoice.oid}
      </SummaryGridRow>

      <SummaryGridRow title={<FormattedMessage {...InvoiceMessages.Invoiced}/>}>
        <LocalizedTime time={invoice.invoiceDatetime}/>
      </SummaryGridRow>

      {invoice.nicheAuctionInvoice &&
        <SummaryGridRow title={<FormattedMessage {...InvoiceMessages.ForNiche}/>}>
          <NicheLink niche={invoice.nicheAuctionInvoice.auction.niche} color="default"/>
        </SummaryGridRow>
      }

      <SummaryGridRow title={<FormattedMessage {...InvoiceMessages.Amount}/>}>
        {invoice.nrveAmount && <NRVE amount={invoice.nrveAmount} />}

        {invoice.fiatPayment &&
        <FormattedMessage
          {...InvoiceMessages.FiatAmount}
          values={{fiatPayment: <USD value={invoice.fiatPayment.totalUsdAmount} />}}
        />}
      </SummaryGridRow>

      <InvoiceSummaryGridActivityDatetimeRow invoice={invoice} />
    </SummaryGrid>
  );
};
