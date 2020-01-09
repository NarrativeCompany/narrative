import { graphql } from 'react-apollo';
import {
  declinePublicationPowerUserInviteMutation
} from '../graphql/publication/declinePublicationPowerUserInviteMutation';
import {
  PublicationInvitationResponseInput,
  DeclinePublicationPowerUserInviteMutation,
  DeclinePublicationPowerUserInviteMutation_declinePublicationPowerUserInvite,
  DeclinePublicationPowerUserInviteMutationVariables,
} from '../../types';
import { mutationResolver } from '../../utils';

const functionName = 'declinePublicationPowerUserInvite';

export interface WithDeclinePublicationPowerUserInviteProps {
  [functionName]: (input: PublicationInvitationResponseInput) =>
    Promise<DeclinePublicationPowerUserInviteMutation_declinePublicationPowerUserInvite>;
}

export const withDeclinePublicationPowerUserInvite =
  graphql<
    {},
    DeclinePublicationPowerUserInviteMutation,
    DeclinePublicationPowerUserInviteMutationVariables,
    WithDeclinePublicationPowerUserInviteProps
  >(declinePublicationPowerUserInviteMutation, {
    props: ({mutate}) => ({
      [functionName]: async (input: PublicationInvitationResponseInput) => {
        return await mutationResolver<DeclinePublicationPowerUserInviteMutation>(mutate, {
          variables: { input }
        }, functionName);
      }
    })
  });
