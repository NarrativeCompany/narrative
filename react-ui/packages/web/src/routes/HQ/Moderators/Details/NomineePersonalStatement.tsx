import * as React from 'react';
import { compose, withHandlers } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { Link } from '../../../../shared/components/Link';
import { Paragraph, ParagraphProps } from '../../../../shared/components/Paragraph';
import { withState, WithStateProps } from '@narrative/shared';
import { NominateCurrentUserForm } from './NominateCurrentUserForm';
import { ModeratorElectionDetailsMessages } from '../../../../shared/i18n/ModeratorElectionDetailsMessages';
import styled from '../../../../shared/styled';

const StyledParagraph = styled<ParagraphProps>(Paragraph)`
  word-break: break-word;
`;

interface State {
  isEditFormVisible: boolean;
}
const initialState: State = {
  isEditFormVisible: false
};

interface WithHandlers {
  handleToggleStatementEditForm: () => void;
}

interface ParentProps {
  electionOid: string;
  personalStatement?: string;
  isCurrentUser?: boolean;
}

type Props =
  ParentProps &
  WithHandlers &
  WithStateProps<State>;

export const NomineePersonalStatementComponent: React.SFC<Props> = (props) => {
  const { electionOid, personalStatement, state, handleToggleStatementEditForm } = props;
  const statement = getPersonalStatement(props);

  return (
    <React.Fragment>
      <StyledParagraph
        color={personalStatement ? 'default' : 'light'}
        textAlign="center"
      >
        {!state.isEditFormVisible && statement}
      </StyledParagraph>

      {state.isEditFormVisible &&
      <NominateCurrentUserForm
        isCardForm={true}
        electionOid={electionOid}
        dismiss={handleToggleStatementEditForm}
        personalStatement={personalStatement}
      />}
    </React.Fragment>
  );
};

function getPersonalStatement (props: Props) {
  const { personalStatement, isCurrentUser, handleToggleStatementEditForm } = props;

  if (!personalStatement) {
    return isCurrentUser ? (
      <Link.Anchor onClick={handleToggleStatementEditForm}>
        <FormattedMessage {...ModeratorElectionDetailsMessages.AddAPitch}/>
      </Link.Anchor>
    ) : (
      <FormattedMessage {...ModeratorElectionDetailsMessages.NoPitch}/>
    );
  }

  const statement = `"${personalStatement}"`;

  if (isCurrentUser) {
    return (
      <span style={{textAlign: 'center'}}>
        {statement}
        <Link.Anchor onClick={handleToggleStatementEditForm} style={{display: 'block'}}>
          <FormattedMessage {...ModeratorElectionDetailsMessages.EditPitch}/>
        </Link.Anchor>
      </span>
    );
  }

  return statement;
}

export const NomineePersonalStatement = compose(
  withState<State>(initialState),
  withHandlers({
    handleToggleStatementEditForm: (props: ParentProps & WithStateProps<State>) => () => {
      const { isCurrentUser, setState } = props;

      if (!isCurrentUser) {
        return;
      }

      setState(ss => ({ ...ss, isEditFormVisible: !ss.isEditFormVisible }));
    }
  })
)(NomineePersonalStatementComponent) as React.ComponentClass<ParentProps>;
