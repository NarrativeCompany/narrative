import gql from 'graphql-tag';

export const deletePostMutation = gql`
  mutation DeletePostMutation ($postOid: String!) {
    deletePost (postOid: $postOid)@rest(type: "VoidResult", path: "/posts/{args.postOid}", method: "DELETE") {
      success
    }
  }
`;
