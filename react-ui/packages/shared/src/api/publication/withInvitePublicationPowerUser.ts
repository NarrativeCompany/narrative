import { graphql } from 'react-apollo';
import { invitePublicationPowerUserMutation } from '../graphql/publication/invitePublicationPowerUserMutation';
import {
  PublicationPowerUsers,
  InvitePublicationPowerUserInput,
  InvitePublicationPowerUserMutation,
  InvitePublicationPowerUserMutationVariables,
} from '../../types';
import { mutationResolver } from '../../utils';

const functionName = 'invitePublicationPowerUser';

export interface WithInvitePublicationPowerUserProps {
  [functionName]: (
    input: InvitePublicationPowerUserInput,
    publicationOid: string,
    userOid: string
  ) => Promise<PublicationPowerUsers>;
}

export const withInvitePublicationPowerUser =
  graphql<
    {},
    InvitePublicationPowerUserMutation,
    InvitePublicationPowerUserMutationVariables,
    WithInvitePublicationPowerUserProps
  >(invitePublicationPowerUserMutation, {
    props: ({mutate}) => ({
      [functionName]: async (input: InvitePublicationPowerUserInput, publicationOid: string, userOid: string) => {
        return await mutationResolver<InvitePublicationPowerUserMutation>(mutate, {
          variables: { input, publicationOid, userOid }
        }, functionName);
      }
    })
  });
