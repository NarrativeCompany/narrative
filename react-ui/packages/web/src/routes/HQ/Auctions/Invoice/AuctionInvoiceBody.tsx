import * as React from 'react';
import { compose, withProps } from 'recompose';
import {
  withInvoice,
  WithInvoiceProps
} from '@narrative/shared';
import { InvoiceDetails } from './InvoiceDetails';
import { Heading } from '../../../../shared/components/Heading';
import { InvoiceMessages } from '../../../../shared/i18n/InvoiceMessages';
import { injectIntl, InjectedIntlProps } from 'react-intl';
import styled from '../../../../shared/styled';
import { fullPlaceholder, withLoadingPlaceholder } from '../../../../shared/utils/withLoadingPlaceholder';
import { InvoiceOidProps, InvoiceProps } from './AuctionInvoice';
import { NotFound } from '../../../../shared/components/NotFound';
import { SEO } from '../../../../shared/components/SEO';
import { FlexContainer } from '../../../../shared/styled/shared/containers';
import { InvoiceStatusTag } from './InvoiceStatusTag';

type WithProps = InvoiceProps &
  InjectedIntlProps;

const InvoiceHeading = styled(Heading)`
  margin-bottom: 20px;
`;

const AuctionInvoiceBodyComponent: React.SFC<WithProps> = (props) => {
  const { invoice, intl: { formatMessage } } = props;

  if (!invoice) {
    return <NotFound />;
  }

  let title;
  if (invoice.nicheAuctionInvoice) {
      title = formatMessage(
        InvoiceMessages.TitleForNiche,
        {nicheName: invoice.nicheAuctionInvoice.auction.niche.name}
      );
  } else {
    // todo:error-handling: it seems like we have a new Invoice type we are not properly supporting...
    return null;
  }

  return (
    <React.Fragment>
      <SEO title={title} />
      <FlexContainer justifyContent="space-between" alignItems="center">
        <InvoiceHeading size={1}>
          {title}
        </InvoiceHeading>
        <InvoiceStatusTag invoice={invoice} />
      </FlexContainer>
      <InvoiceDetails invoice={invoice}/>
    </React.Fragment>
  );
};

export const AuctionInvoiceBody = compose(
  withInvoice,
  withProps((props: WithInvoiceProps) => {
    const { invoiceData } = props;
    const { getInvoice, loading } = invoiceData;

    return { invoice: getInvoice, loading };
  }),
  withLoadingPlaceholder(fullPlaceholder),
  injectIntl
)(AuctionInvoiceBodyComponent) as React.ComponentClass<InvoiceOidProps>;
