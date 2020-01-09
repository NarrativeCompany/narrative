import * as React from 'react';
import { compose, withHandlers } from 'recompose';
import { Icon } from 'antd';
import { FormattedMessage } from 'react-intl';
import { Card, CardProps } from '../../../../shared/components/Card';
import { Heading } from '../../../../shared/components/Heading';
import { FlexContainer } from '../../../../shared/styled/shared/containers';
import { NominateCurrentUserForm } from './NominateCurrentUserForm';
import { ElectionModal } from './ElectionModal';
import { ModeratorElectionDetailsMessages } from '../../../../shared/i18n/ModeratorElectionDetailsMessages';
import { RevokeReasonMessages } from '../../../../shared/i18n/RevokeReasonMessages';
import { withState, WithStateProps } from '@narrative/shared';
import {
  WithPermissionsModalControllerProps,
  withPermissionsModalController
} from '../../../../shared/containers/withPermissionsModalController';
import styled from '../../../../shared/styled';
import { PermissionErrorModal } from '../../../../shared/components/PermissionErrorModal';

const CardButton = styled<CardProps>(Card)`
  border: 1px solid ${props => props.theme.primaryBlue} !important;
  cursor: pointer !important;
  
  .ant-card-body {
    height: 100%
  }
  
  i, h4 {
    color: ${props => props.theme.primaryBlue}; 
    transition: all .15s ease-in-out;
  }
  
  i {
    font-size: 80px;
    margin-bottom: 10px;
  }
  
  &:hover {
    border-color: ${props => props.theme.secondaryBlue};
    transition: all .15s ease-in-out;
    
    i, h4 {
      color: ${props => props.theme.secondaryBlue};
      transition: all .15s ease-in-out; 
    }
  }
`;

interface State {
  isCurrentUserNominationModalVisible: boolean;
}

const initialState: State = {
  isCurrentUserNominationModalVisible: false,
};

interface WithHandlers {
  handleToggleModal: (isVisible: boolean) => void;
}

interface ParentProps {
  electionOid: string;
}

type Props =
  ParentProps &
  WithStateProps<State> &
  WithHandlers &
  WithPermissionsModalControllerProps;

const NominateCurrentUserButtonComponent: React.SFC<Props> = (props) => {
  const { electionOid, handleToggleModal, state, permissionErrorModalProps } = props;

  return (
    <React.Fragment>
      <CardButton height={275} noBoxShadow={true} hoverable={true} onClick={() => handleToggleModal(true)}>
        <FlexContainer centerAll={true} column={true} style={{ height: '100%' }}>
          <Icon type="plus"/>

          <Heading size={4} uppercase={true}>
            <FormattedMessage {...ModeratorElectionDetailsMessages.NominateMyselfBtn}/>
          </Heading>
        </FlexContainer>
      </CardButton>

      {permissionErrorModalProps && <PermissionErrorModal {...permissionErrorModalProps} />}

      <ElectionModal
        dismiss={() => handleToggleModal(false)}
        visible={state.isCurrentUserNominationModalVisible}
        title={<FormattedMessage {...ModeratorElectionDetailsMessages.NominateCurrentUserModalTitle}/>}
      >
        <NominateCurrentUserForm
          electionOid={electionOid}
          dismiss={() => handleToggleModal(false)}
        />
      </ElectionModal>
    </React.Fragment>
  );
};

export const NominateCurrentUserButton = compose(
  withState<State>(initialState),
  withPermissionsModalController(
    'nominateForModeratorElection',
    RevokeReasonMessages.NominateForModeratorElection
  ),
  withHandlers({
    handleToggleModal: (props: Props) => (isVisible: boolean) => {
      const { setState, granted, handleShowPermissionsModal } = props;

      if (!granted) {
        handleShowPermissionsModal();
        return;
      }

      setState(ss => ({ ...ss, isCurrentUserNominationModalVisible: isVisible }));
    },
  })
)(NominateCurrentUserButtonComponent) as React.ComponentClass<ParentProps>;
