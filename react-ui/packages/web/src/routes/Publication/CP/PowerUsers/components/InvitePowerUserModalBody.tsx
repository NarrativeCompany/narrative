import * as React from 'react';
import { compose } from 'recompose';
import { InvitePowerUserButtonProps } from './InvitePowerUserButton';
import { User, withState, WithStateProps } from '@narrative/shared';
import { MemberSearch } from '../../../../../shared/components/user/MemberSearch';
import { InvitePowerUserModalSelectedUserBody } from './InvitePowerUserModalSelectedUserBody';

interface State {
  selectedUser?: User;
}

interface ParentProps extends InvitePowerUserButtonProps {
  close: () => void;
}

type Props = ParentProps &
  WithStateProps<State>;

const InvitePowerUserModalBodyComponent: React.SFC<Props> = (props) => {
  const { canInviteRoles, publicationDetail, close, setState, state: { selectedUser } } = props;

  if (!selectedUser) {
    return <MemberSearch onMemberSelected={user => setState(ss => ({...ss, selectedUser: user}))}/>;
  }

  return (
    <InvitePowerUserModalSelectedUserBody
      publicationDetail={publicationDetail}
      canInviteRoles={canInviteRoles}
      selectedUser={selectedUser}
      close={close}
      unselectUser={() => setState(ss => ({...ss, selectedUser: undefined}))}
    />
  );
};

export const InvitePowerUserModalBody = compose(
  withState<State>({})
)(InvitePowerUserModalBodyComponent) as React.ComponentClass<ParentProps>;
