import gql from 'graphql-tag';

export const FollowScrollParamsFragment = gql`
  fragment FollowScrollParams on FollowScrollParams {
    lastItemName
    lastItemOid
  }
`;
