import * as React from 'react';
import { ButtonProps, Button } from '../../../shared/components/Button';
import { generatePath } from 'react-router';
import { FormattedMessage } from 'react-intl';
import { WebRoute } from '../../../shared/constants/routes';
import { ModeratorElectionsMessages } from '../../../shared/i18n/ModeratorElectionsMessages';

interface ParentProps {
  buttonProps?: ButtonProps;
  electionOid: string;
}

export const ViewElectionButton: React.SFC<ParentProps> = (props) => {
  const { electionOid, buttonProps } = props;
  const href = generatePath(WebRoute.ModeratorElectionDetails, {electionOid});

  return (
    <Button type="ghost" href={href} {...buttonProps}>
      <FormattedMessage {...ModeratorElectionsMessages.ViewElectionBtnText}/>
    </Button>
  );
};
