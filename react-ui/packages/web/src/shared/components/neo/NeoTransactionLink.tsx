import * as React from 'react';
import { Link, LinkStyleProps } from '../Link';
import { truncateStringInMiddle } from '@narrative/shared';

interface ParentProps {
  transactionId: string;
}

type Props =
  ParentProps &
  LinkStyleProps;

export const NeoTransactionLink: React.SFC<Props> = (props) => {
  const { transactionId, ...linkProps } = props;

  const transactionUrl = 'https://neoscan.io/transaction/' + transactionId;

  // bl: if a body is specified, then use that in place of the transaction in the link
  return (
    <Link.Anchor
      {...linkProps}
      href={transactionUrl}
      target="_blank"
    >
      {props.children ? props.children : truncateStringInMiddle(transactionId, 10, 10)}
    </Link.Anchor>
  );
};
