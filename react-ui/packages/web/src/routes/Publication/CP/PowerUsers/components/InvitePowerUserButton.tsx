import * as React from 'react';
import { compose } from 'recompose';
import { PublicationRole, PublicationDetail, withState, WithStateProps } from '@narrative/shared';
import { Button } from '../../../../../shared/components/Button';
import { PublicationDetailsMessages } from '../../../../../shared/i18n/PublicationDetailsMessages';
import { FormattedMessage } from 'react-intl';
import { InvitePowerUserModal } from './InvitePowerUserModal';
import { PublicationRoleBooleans } from '../../../../../shared/utils/publicationRoleUtils';

export interface InvitePowerUserButtonProps {
  publicationDetail: PublicationDetail;
  canInviteRoles: PublicationRole[];
  currentUserRoles: PublicationRoleBooleans;
}

interface State {
  showInvitePowerUserModal?: boolean;
}

type Props = InvitePowerUserButtonProps &
  WithStateProps<State>;

const InvitePowerUserButtonComponent: React.SFC<Props> = (props) => {
  const { canInviteRoles, publicationDetail, currentUserRoles, setState, state: { showInvitePowerUserModal } } = props;

  return (
    <React.Fragment>
      <Button type="primary" onClick={() => setState(ss => ({...ss, showInvitePowerUserModal: true}))}>
        <FormattedMessage {...PublicationDetailsMessages.AddPowerUser} />
      </Button>
      <InvitePowerUserModal
        canInviteRoles={canInviteRoles}
        publicationDetail={publicationDetail}
        currentUserRoles={currentUserRoles}
        visible={showInvitePowerUserModal}
        close={() => setState(ss => ({...ss, showInvitePowerUserModal: undefined}))}
      />
    </React.Fragment>
  );
};

export const InvitePowerUserButton = compose(
  withState<State>({})
)(InvitePowerUserButtonComponent) as React.ComponentClass<InvitePowerUserButtonProps>;
