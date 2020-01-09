import { graphql } from 'react-apollo';
import { approvePublicationPostMutation } from '../graphql/post/approvePublicationPostMutation';
import {
  PostDetail,
  ApprovePublicationPostMutation,
  ApprovePublicationPostMutationVariables
} from '../../types';
import { mutationResolver } from '../../utils';

const functionName = 'approvePublicationPost';

export interface WithApprovePublicationPostProps {
  [functionName]: (postOid: string) => Promise<PostDetail>;
}

export const withApprovePublicationPost =
  graphql<
    {},
    ApprovePublicationPostMutation,
    ApprovePublicationPostMutationVariables,
    WithApprovePublicationPostProps
  >(approvePublicationPostMutation, {
    props: ({mutate}) => ({
      [functionName]: async (postOid: string) => {
        return await mutationResolver<ApprovePublicationPostMutation>(mutate, {
          variables: { input: {postOid} }
        }, functionName);
      }
    })
  });
