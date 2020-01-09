import { graphql } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import {
  UpdateUserNotificationSettingsMutation,
  UpdateUserNotificationSettingsMutation_updateUserNotificationSettings,
  UpdateUserNotificationSettingsMutationVariables
} from '../../types';
import { updateUserNotificationSettingsMutation } from '../graphql/user/updateUserNotificationSettingsMutation';
import { resolveExceptionFromApolloError } from '../../utils';

export interface WithUpdateUserNotificationSettingsProps {
  updateUserNotificationSettings: (input: UpdateUserNotificationSettingsMutationVariables)
    => Promise<UpdateUserNotificationSettingsMutation_updateUserNotificationSettings>;
}

export const withUpdateUserNotificationSettings = graphql(updateUserNotificationSettingsMutation, {
  props: ({mutate}) => ({
    updateUserNotificationSettings: async (input: UpdateUserNotificationSettingsMutationVariables) => {
      const variables: UpdateUserNotificationSettingsMutationVariables = input;
      const options = {
        variables,
        mutation: updateUserNotificationSettingsMutation
      };

      if (!mutate) {
        throw new Error ('withUpdateUserNotificationSettings: missing mutate');
      }

      return mutate(options)
        .then((response: FetchResult<UpdateUserNotificationSettingsMutation>) => {
          const updateUserNotificationSettings =
            response &&
            response.data &&
            response.data.updateUserNotificationSettings;

          if (!updateUserNotificationSettings) {
            throw new Error('withUpdateUserNotificationSettings: no return value from ' +
              'updateUserNotificationSettings mutation');
          }

          return updateUserNotificationSettings;
        }).catch((error) => {
          throw resolveExceptionFromApolloError(error);
        });
    }
  })
});
