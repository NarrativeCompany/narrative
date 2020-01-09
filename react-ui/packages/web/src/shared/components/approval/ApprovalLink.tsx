import * as React from 'react';
import { Referendum } from '@narrative/shared';
import { Link, LinkStyleProps } from '../Link';
import { WebRoute } from '../../constants/routes';
import { generatePath } from 'react-router';

interface ParentProps {
  referendum: Referendum;
}

type Props =
  ParentProps &
  LinkStyleProps;

export const ApprovalLink: React.SFC<Props> = (props) => {
  const { referendum, ...linkStyleProps } = props;
  const referendumOid = referendum.oid;

  return (
    <Link
      {...linkStyleProps}
      to={generatePath(WebRoute.ApprovalDetails, {referendumOid})}
    />
  );
};
