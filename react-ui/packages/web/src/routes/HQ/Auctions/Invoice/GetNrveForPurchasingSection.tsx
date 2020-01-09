import * as React from 'react';
import { Link } from '../../../../shared/components/Link';
import { Heading } from '../../../../shared/components/Heading';
import { FormattedMessage } from 'react-intl';
import { InvoiceMessages } from '../../../../shared/i18n/InvoiceMessages';
import styled from '../../../../shared/styled';
import { InvoiceType } from '@narrative/shared';
import { InvoiceProps } from './AuctionInvoice';

// jw: this Component may get used in different places, which is the only reason I am splitting it out from the
//     InvoicePayWithNrve.tsx. If we do end up needing it somewhere else, we should probably move it into shared.

const Container = styled.div`
  margin-bottom: 10px;
`;

export type GetNrveForPurchasingSectionParentProps = InvoiceProps;

export const GetNrveForPurchasingSection: React.SFC<GetNrveForPurchasingSectionParentProps> = (props) => {
  const { invoice } = props;

  const latokenLink = (
    <Link.Anchor href="https://latoken.com/" target="_blank">
      <FormattedMessage {...InvoiceMessages.VisitExchangeLATOKEN} />
    </Link.Anchor>
  );

  const switcheoLink = (
    <Link.Anchor href="https://switcheo.exchange/" target="_blank">
      <FormattedMessage {...InvoiceMessages.VisitExchangeSwitcheo} />
    </Link.Anchor>
  );

  const bilaxyLink = (
    <Link.Anchor href="https://bilaxy.com/" target="_blank">
      <FormattedMessage {...InvoiceMessages.VisitExchangeBilaxy} />
    </Link.Anchor>
  );

  const howToBuyLink = (
    <Link.About type="nrve">
      <FormattedMessage {...InvoiceMessages.HowToBuyNrve} />
    </Link.About>
  );

  const nrveLink = <Link.About type="nrve"/>;

  return (
    <Container>
      <Heading size={4}><FormattedMessage {...(
        invoice.type === InvoiceType.NICHE_AUCTION
        ? InvoiceMessages.HowDoIPayForMyNiche
        : invoice.type === InvoiceType.PUBLICATION_ANNUAL_FEE
          ? InvoiceMessages.HowDoIPayForMyPublication
          : InvoiceMessages.HowDoIPay
      )} /></Heading>
      <ul>
        <li>
          <FormattedMessage
            {...InvoiceMessages.VisitExchange}
            values={{nrveLink, latokenLink, switcheoLink, bilaxyLink}}
          />
        </li>
        <li><FormattedMessage {...InvoiceMessages.NewToCrypto} values={{howToBuyLink}}/></li>
        <li><FormattedMessage {...InvoiceMessages.DontSendFromExchange} values={{nrveLink}}/></li>
      </ul>
    </Container>
  );
};
