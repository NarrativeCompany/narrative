import * as React from 'react';
import { compose } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { Link } from '../../../shared/components/Link';
import { Button } from '../../../shared/components/Button';
import { generatePath } from 'react-router';
import { WebRoute } from '../../../shared/constants/routes';
import { BidCardButtonMessages } from '../../../shared/i18n/BidCardButtonMessages';
import styled from '../../../shared/styled';

const LinkWrapper = styled.div`
  margin-bottom: 20px;
`;

interface ParentProps {
  pendingPayment?: boolean;
  auctionOid: string;
}

type Props =
  ParentProps;

const BidCardButtonComponent: React.SFC<Props> = (props) => {
  const {
    pendingPayment,
    auctionOid
  } = props;

  // bl: this is an unsecured link now. we used to attempt permission checks on the link to give an error before
  // you get to the details page, but that's really unnecessary and inaccurate. we don't know if you can bid
  // on this specific niche at this point; the general/global permission is insufficient to determine that.
  const anchorProps = { href: generatePath(WebRoute.AuctionDetails, { auctionOid }) };

  if (pendingPayment) {
    return (
      <LinkWrapper>
        <Link.Anchor {...anchorProps}>
          <FormattedMessage {...BidCardButtonMessages.LinkText}/>
        </Link.Anchor>
      </LinkWrapper>
    );
  }

  return (
    <Button
      type="primary"
      size="large"
      block={true}
      style={{ marginBottom: 20, width: '90%' }}
      {...anchorProps}
    >
      <FormattedMessage {...BidCardButtonMessages.BtnText}/>
    </Button>
  );
};

export const AuctionCardButton = compose(
)(BidCardButtonComponent) as React.ComponentClass<ParentProps>;
