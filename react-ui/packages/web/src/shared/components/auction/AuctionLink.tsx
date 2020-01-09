import * as React from 'react';
import { NicheAuction } from '@narrative/shared';
import { LinkStyleProps } from '../Link';
import { Link } from '../Link';
import { WebRoute } from '../../constants/routes';
import { generatePath } from 'react-router';

interface ParentProps {
  auction: NicheAuction;
}

type Props =
  ParentProps &
  LinkStyleProps;

export const AuctionLink: React.SFC<Props> = (props) => {
  const { auction, ...linkProps } = props;
  const auctionOid = auction.oid;

  return (
    <Link
      {...linkProps}
      to={generatePath(WebRoute.AuctionDetails, {auctionOid})}
    />
  );
};
