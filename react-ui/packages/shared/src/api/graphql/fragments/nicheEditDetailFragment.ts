import gql from 'graphql-tag';

export const NicheEditDetailFragment = gql`
  fragment NicheEditDetail on NicheEditDetail {
    newName
    newDescription
    originalName
    originalDescription
  }
`;
