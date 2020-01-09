import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { nicheModeratorElectionDetailsQuery } from '../graphql/election/nicheModeratorElectionDetailsQuery';
import { Election, ElectionNominee, Niche, NicheModeratorElectionDetailsQuery } from '../../types';

interface ParentProps {
  electionOid: string;
}

type WithProps = NamedProps<
  {nicheModeratorElectionDetailsData: GraphqlQueryControls & NicheModeratorElectionDetailsQuery},
  WithNicheModeratorElectionDetailsProps
>;

export type WithNicheModeratorElectionDetailsProps =
  ChildDataProps<ParentProps, NicheModeratorElectionDetailsQuery> & {
  loading: boolean;
  electionOid: string;
  election: Election;
  availableSlots: number;
  nomineeCount: number;
  currentUserNominee: ElectionNominee;
  niche: Niche;
};

export const withNicheModeratorElectionDetails =
  graphql<
    ParentProps,
    NicheModeratorElectionDetailsQuery,
    {},
    WithNicheModeratorElectionDetailsProps
  >(nicheModeratorElectionDetailsQuery, {
    skip: ({electionOid}) => !electionOid,
    options: (ownProps: ParentProps) => ({
      variables: {
        electionOid: ownProps.electionOid
      }
    }),
    name: 'nicheModeratorElectionDetailsData',
    props: ({ nicheModeratorElectionDetailsData, ownProps }: WithProps) => {
      const loading = nicheModeratorElectionDetailsData.loading;
      const result =
        nicheModeratorElectionDetailsData &&
        nicheModeratorElectionDetailsData.getNicheModeratorElectionDetail;
      const electionDetail =
        result &&
        result.election;
      const election =
        electionDetail &&
        electionDetail.election;
      const niche =
        result &&
        result.niche;
      const currentUserNominee =
        electionDetail &&
        electionDetail.currentUserNominee;
      const availableSlots =
        election &&
        election.availableSlots;
      const nomineeCount =
        election &&
        election.nomineeCount;

      return {
        loading,
        election,
        availableSlots,
        nomineeCount,
        currentUserNominee,
        niche,
        ...ownProps
      };
    }
  });
