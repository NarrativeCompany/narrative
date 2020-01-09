import gql from 'graphql-tag';
import { UserFragment } from './userFragment';

export const DeletedChannelFragment = gql`
  fragment DeletedChannel on DeletedChannel {
    oid
    type
    name

    owner @type(name: "User") {
      ...User
    }
  }
  ${UserFragment}
`;
