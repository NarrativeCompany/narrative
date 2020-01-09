import * as React from 'react';
import { Link, LinkStyleProps } from '../Link';
import { truncateStringInMiddle } from '@narrative/shared';

interface ParentProps {
  scriptHash: string;
  showFull?: boolean;
}

type Props =
  ParentProps &
  LinkStyleProps;

export const NeoAssetLink: React.SFC<Props> = (props) => {
  const { scriptHash, showFull, ...linkProps } = props;

  const addressUrl = 'https://neoscan.io/asset/' + scriptHash;

  // bl: if a body is specified, then use that in place of the address in the link
  return (
    <Link.Anchor
      {...linkProps}
      href={addressUrl}
      target="_blank"
    >
      {props.children ? props.children : showFull ? scriptHash : truncateStringInMiddle(scriptHash, 10, 10)}
    </Link.Anchor>
  );
};
