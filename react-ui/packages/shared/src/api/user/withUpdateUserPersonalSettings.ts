import { graphql } from 'react-apollo';
import {
  UpdateUserPersonalSettingsMutation,
  UpdateUserPersonalSettingsMutation_updateUserPersonalSettings,
  UserPersonalSettingsInput
} from '../../types';
import { updateUserPersonalSettingsMutation } from '../graphql/user/updateUserPersonalSettingsMutation';
import { mutationResolver } from '../../utils';

const functionName = 'updateUserPersonalSettings';

export interface WithUpdateUserPersonalSettingsProps {
  [functionName]: (input: UserPersonalSettingsInput)
    => Promise<UpdateUserPersonalSettingsMutation_updateUserPersonalSettings>;
}

export const withUpdateUserPersonalSettings =
  graphql<
    {},
    UpdateUserPersonalSettingsMutation,
    UserPersonalSettingsInput,
    WithUpdateUserPersonalSettingsProps
    >( updateUserPersonalSettingsMutation, {
    props: ({mutate}) => ({
      [functionName]: async (input: UserPersonalSettingsInput) => {
        return await mutationResolver<UpdateUserPersonalSettingsMutation>(mutate, {
          variables: { input }
        }, functionName);
      }
    })
  });
