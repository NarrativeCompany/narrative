import * as React from 'react';
import { compose, withHandlers } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { FlexContainer, FlexContainerProps } from '../../../../shared/styled/shared/containers';
import { Heading } from '../../../../shared/components/Heading';
import { ElectionModal } from './ElectionModal';
import { RemoveCurrentUserNominationForm } from './RemoveCurrentUserNominationForm';
import { withState, WithStateProps } from '@narrative/shared';
import { ModeratorElectionDetailsMessages } from '../../../../shared/i18n/ModeratorElectionDetailsMessages';
import styled, { css } from '../../../../shared/styled';

const NomineeCardActionWrapper = styled<FlexContainerProps & {isCurrentUser?: boolean}>(FlexContainer)`
  height: 100%;
  width: 100%;
  border-radius: 0 0 4px 4px;
  ${props => getActionWrapperStyles(props.isCurrentUser)};
`;

interface State {
  isRemoveNominationModalVisible: boolean;
}
const initialState: State = {
  isRemoveNominationModalVisible: false,
};

interface WithHandlers {
  handleToggleModal: (isVisible: boolean) => void;
}

interface ParentProps {
  isCurrentUser?: boolean;
  electionOid: string;
}

type Props =
  ParentProps &
  WithStateProps<State> &
  WithHandlers;

const NomineeCardActionComponent: React.SFC<Props> = (props) => {
  const { isCurrentUser, state, electionOid, handleToggleModal } = props;
  const actionText = getActionWrapperText(isCurrentUser);

  return (
    <React.Fragment>
      <NomineeCardActionWrapper
        centerAll={true}
        isCurrentUser={isCurrentUser}
        onClick={() => handleToggleModal(true)}
      >
        <Heading size={5} uppercase={true} weight={400} noMargin={true}>
          <FormattedMessage {...actionText}/>
        </Heading>
      </NomineeCardActionWrapper>

      <ElectionModal
        title={<FormattedMessage {...ModeratorElectionDetailsMessages.RevokeMyNominationModalTitle}/>}
        visible={state.isRemoveNominationModalVisible}
        dismiss={() => handleToggleModal(false)}
      >
        <RemoveCurrentUserNominationForm
          electionOid={electionOid}
          dismiss={() => handleToggleModal(false)}
        />
      </ElectionModal>
    </React.Fragment>
  );
};

function getActionWrapperStyles (isCurrentUser?: boolean) {
  if (isCurrentUser) {
    return css`
      background: ${props => props.theme.primaryRed};
      cursor: pointer;
      
      h5 {
        color: #fff;
      }
    `;
  }

  return css`
    background: ${props => props.theme.defaultTagBackgroundColorHover};
    
    h5 {
      color: ${props => props.theme.textColor};
    }
  `;
}

function getActionWrapperText (isCurrentUser?: boolean) {
  return isCurrentUser ?
    ModeratorElectionDetailsMessages.RevokeNominationBtnText :
    ModeratorElectionDetailsMessages.NomineeBtnText;
}

export const NomineeCardAction = compose(
  withState<State>(initialState),
  withHandlers({
    handleToggleModal: (props: ParentProps & WithStateProps<State>) => (isVisible: boolean) => {
      const { isCurrentUser, setState } = props;

      if (!isCurrentUser) {
        return;
      }

      setState(ss => ({ ...ss, isRemoveNominationModalVisible: isVisible }));
    }
  })
)(NomineeCardActionComponent) as React.ComponentClass<ParentProps>;
