import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { Election, ElectionStatus, Niche, NicheModeratorSlotsQuery } from '../../types';
import { nicheModeratorSlotsQuery } from '../graphql/niche/nicheModeratorSlotsQuery';

const queryName = 'nicheModeratorSlotsData';

interface ParentProps {
  nicheId: string;
}

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & NicheModeratorSlotsQuery},
  WithNicheModeratorSlotsProps
  >;

export type WithNicheModeratorSlotsProps =
  ChildDataProps<ParentProps, NicheModeratorSlotsQuery> & {
  nicheModeratorSlotsLoading: boolean;
  niche: Niche;
  moderatorSlots: number;
  activeModeratorElection: Election;
  electionIsLive: boolean;
};

export const withNicheModeratorSlots =
  graphql<
    ParentProps,
    NicheModeratorSlotsQuery,
    {},
    WithNicheModeratorSlotsProps
   >(nicheModeratorSlotsQuery, {
    options: ({nicheId}: ParentProps) => ({
      variables: {
        nicheId
      }
    }),
    name: queryName,
    props: ({ nicheModeratorSlotsData, ownProps }: WithProps) => {
      const { loading, getNicheModeratorSlots } = nicheModeratorSlotsData;

      const niche =
        getNicheModeratorSlots &&
        getNicheModeratorSlots.niche;

      const moderatorSlots =
        getNicheModeratorSlots &&
        getNicheModeratorSlots.moderatorSlots;

      const activeModeratorElection =
        getNicheModeratorSlots &&
        getNicheModeratorSlots.activeModeratorElection as Election;

      const electionIsLive =
        activeModeratorElection &&
        (activeModeratorElection.status === ElectionStatus.NOMINATING ||
         activeModeratorElection.status === ElectionStatus.VOTING) || false;

      return {
        ...ownProps,
        nicheModeratorSlotsLoading: loading,
        niche,
        moderatorSlots,
        activeModeratorElection,
        electionIsLive
      };
    }
  });
