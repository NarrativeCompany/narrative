import * as React from 'react';
import { TribunalIssue } from '@narrative/shared';
import { Link, LinkStyleProps } from '../Link';
import { WebRoute } from '../../constants/routes';
import { generatePath } from 'react-router';

interface ParentProps {
  issue: TribunalIssue;
}

type Props =
  ParentProps &
  LinkStyleProps;

export const TribunalIssueLink: React.SFC<Props> = (props) => {
  const { issue, ...linkStyleProps } = props;

  const tribunalIssueOid = issue && issue.oid;

  return (
    <Link {...linkStyleProps} to={generatePath(WebRoute.AppealDetails, {tribunalIssueOid})}/>
  );
};
