import * as React from 'react';
import { compose, withProps } from 'recompose';

import {
  withInvoice,
  WithInvoiceProps
} from '@narrative/shared';
import { LoadingProps } from '../../utils/withLoadingPlaceholder';
import { ContainedLoading } from '../Loading';
import { Link } from '../Link';
import { generatePath } from 'react-router';
import { WebRoute } from '../../constants/routes';
import { FormattedMessage } from 'react-intl';
import { AuctionStatusCard } from './AuctionStatusCard';
import { AuctionDetailsMessages } from '../../i18n/AuctionDetailsMessages';
import { LocalizedTime } from '../LocalizedTime';
import { InvoiceOidProps, InvoiceProps } from '../../../routes/HQ/Auctions/Invoice/AuctionInvoice';

type Props =
  InvoiceProps &
  LoadingProps;

const ActiveInvoiceStatusComponent: React.SFC<Props> = (props) => {
  const { loading, invoice } = props;

  if (loading) {
    return (
      <AuctionStatusCard
        color="primaryGreen"
        title={AuctionDetailsMessages.InvoiceDue}
        message={<ContainedLoading />}
      />
    );
  }

  if (!invoice) {
    // todo:error-handling: Log with the server that we failed to load the invoice from the provided OID.
    return null;
  }

  const invoiceOid = invoice.oid;
  const invoiceLink = (
    <Link to={generatePath(WebRoute.AuctionInvoice, { invoiceOid })}>
      <FormattedMessage {...AuctionDetailsMessages.Invoice} />
    </Link>
  );
  const expirationDatetime = <LocalizedTime time={invoice.paymentDueDatetime} />;

  return (
    <AuctionStatusCard
      color="primaryGreen"
      title={AuctionDetailsMessages.InvoiceDue}
      message={<FormattedMessage {...AuctionDetailsMessages.YouHaveAnExistingInvoice} values={{ invoiceLink }} />}
      info={<FormattedMessage {...AuctionDetailsMessages.ExistingInvoiceInfo} values={{ expirationDatetime }} />}
    />
  );
};

export const ActiveInvoiceStatusCard = compose(
  withInvoice,
  withProps((props: WithInvoiceProps) => {
    const { invoiceData } = props;
    const { getInvoice, loading } = invoiceData;

    return { invoice: getInvoice, loading };
  }),
)(ActiveInvoiceStatusComponent) as React.ComponentClass<InvoiceOidProps>;
