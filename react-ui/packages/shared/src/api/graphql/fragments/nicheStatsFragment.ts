import gql from 'graphql-tag';

export const NicheStatsFragment = gql`
  fragment NicheStats on NicheStats {
    nichesForSale
    nichesAwaitingApproval
  }
`;
