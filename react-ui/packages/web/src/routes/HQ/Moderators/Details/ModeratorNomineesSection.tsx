import * as React from 'react';
import { compose, withHandlers, withProps } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { SectionHeader } from '../../../../shared/components/SectionHeader';
import { CardListLoading } from '../../../../shared/components/Loading';
import { ModeratorNomineesList } from './ModerationNomineesList';
import { ModeratorElectionDetailsMessages } from '../../../../shared/i18n/ModeratorElectionDetailsMessages';
import {
  withElectionNominees,
  WithElectionNomineesProps,
  ElectionNominee,
  ElectionNomineesQueryInput,
  ElectionNomineesQueryVariables,
  fetchMoreNominees,
  withState,
  WithStateProps
} from '@narrative/shared';

interface State {
  isFetchingMore: boolean;
}
const initialState: State = {
  isFetchingMore: false,
};

interface WithHandlers {
  handleFetchMore: () => void;
}

interface ParentProps {
  electionOid: string;
  currentUserNominee: ElectionNominee | null;
  totalNominees: number;
}

type Props =
  ParentProps &
  WithStateProps<State> &
  WithElectionNomineesProps &
  WithHandlers;

export const ModeratorNomineesSectionComponent: React.SFC<Props> = (props) => {
  const {
    state,
    loading,
    electionOid,
    electionNominees,
    totalNominees,
    currentUserNominee,
    hasMoreItems,
    handleFetchMore
  } = props;

  return (
    <React.Fragment>
      <SectionHeader
        title={<FormattedMessage {...ModeratorElectionDetailsMessages.NomineesSectionTitle}
        values={{totalNominees}}/>}
      />

      {loading &&
      <CardListLoading
        grid={{gutter: 32, lg: 3, md: 2, sm: 2, xl: 4, xs: 1}}
        listLength={4}
      />}

      {!loading &&
      <ModeratorNomineesList
        electionOid={electionOid}
        currentUserNominee={currentUserNominee}
        electionNominees={electionNominees}
        hasMoreItems={hasMoreItems}
        onFetchMore={handleFetchMore}
        isFetchingMore={state.isFetchingMore}
      />}
    </React.Fragment>
  );
};

export const ModeratorNomineesSection = compose(
  withState<State>(initialState),
  withProps(() => ({
    count: 11
  })),
  withElectionNominees,
  withHandlers({
    handleFetchMore: (props: WithElectionNomineesProps & WithStateProps<State>) => async () => {
      const { fetchMore, lastItemDatetime: confirmedBefore, electionOid, setState } = props;

      const input: ElectionNomineesQueryInput = { confirmedBefore, count: 12 };
      const variables: ElectionNomineesQueryVariables = { input, electionOid };

      setState(ss => ({ ...ss, isFetchingMore: true }));
      await fetchMoreNominees(variables, fetchMore);
      setState(ss => ({ ...ss, isFetchingMore: false }));
    }
  }),
)(ModeratorNomineesSectionComponent) as React.ComponentClass<ParentProps>;
