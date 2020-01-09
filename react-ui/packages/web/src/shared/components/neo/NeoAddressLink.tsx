import * as React from 'react';
import { Link, LinkStyleProps } from '../Link';
import { truncateStringInMiddle } from '@narrative/shared';

interface ParentProps {
  address: string;
  showFull?: boolean;
}

type Props =
  ParentProps &
  LinkStyleProps;

export const NeoAddressLink: React.SFC<Props> = (props) => {
  const { address, showFull, ...linkProps } = props;

  const addressUrl = 'https://neoscan.io/address/' + address;

  // bl: if a body is specified, then use that in place of the address in the link
  return (
    <Link.Anchor
      {...linkProps}
      href={addressUrl}
      target="_blank"
    >
      {props.children ? props.children : showFull ? address : truncateStringInMiddle(address, 10, 10)}
    </Link.Anchor>
  );
};
