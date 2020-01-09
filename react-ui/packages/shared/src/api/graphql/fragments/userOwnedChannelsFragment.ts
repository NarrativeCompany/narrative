import gql from 'graphql-tag';

export const UserOwnedChannelsFragment = gql`
  fragment UserOwnedChannels on UserOwnedChannels {
    ownedNiches
    ownedPublications
  }
`;
