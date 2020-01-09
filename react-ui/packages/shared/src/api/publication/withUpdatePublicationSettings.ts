import { graphql } from 'react-apollo';
import { updatePublicationSettingsMutation } from '../graphql/publication/updatePublicationSettingsMutation';
import {
  PublicationSettings,
  PublicationSettingsInput,
  UpdatePublicationSettingsMutation,
  UpdatePublicationSettingsMutationVariables,
} from '../../types';
import { mutationResolver } from '../../utils';

const functionName = 'updatePublicationSettings';

export interface WithUpdatePublicationSettingsProps {
  [functionName]: (input: PublicationSettingsInput, publicationOid: string) => Promise<PublicationSettings>;
}

export const withUpdatePublicationSettings =
  graphql<
    {},
    UpdatePublicationSettingsMutation,
    UpdatePublicationSettingsMutationVariables,
    WithUpdatePublicationSettingsProps
  >(updatePublicationSettingsMutation, {
    props: ({mutate}) => ({
      [functionName]: async (input: PublicationSettingsInput, publicationOid: string) => {
        return await mutationResolver<UpdatePublicationSettingsMutation>(mutate, {
          variables: { input, publicationOid }
        }, functionName);
      }
    })
  });
