import { graphql } from 'react-apollo';
import {
  acceptPublicationPowerUserInviteMutation
} from '../graphql/publication/acceptPublicationPowerUserInviteMutation';
import {
  PublicationInvitationResponseInput,
  AcceptPublicationPowerUserInviteMutation,
  AcceptPublicationPowerUserInviteMutation_acceptPublicationPowerUserInvite,
  AcceptPublicationPowerUserInviteMutationVariables,
} from '../../types';
import { mutationResolver } from '../../utils';

const functionName = 'acceptPublicationPowerUserInvite';

export interface WithAcceptPublicationPowerUserInviteProps {
  [functionName]: (input: PublicationInvitationResponseInput) =>
    Promise<AcceptPublicationPowerUserInviteMutation_acceptPublicationPowerUserInvite>;
}

export const withAcceptPublicationPowerUserInvite =
  graphql<
    {},
    AcceptPublicationPowerUserInviteMutation,
    AcceptPublicationPowerUserInviteMutationVariables,
    WithAcceptPublicationPowerUserInviteProps
  >(acceptPublicationPowerUserInviteMutation, {
    props: ({mutate}) => ({
      [functionName]: async (input: PublicationInvitationResponseInput) => {
        return await mutationResolver<AcceptPublicationPowerUserInviteMutation>(mutate, {
          variables: { input }
        }, functionName);
      }
    })
  });
