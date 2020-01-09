import { graphql } from 'react-apollo';
import { changePublicationOwnerMutation } from '../graphql/publication/changePublicationOwnerMutation';
import {
  PublicationPowerUsers,
  ChangePublicationOwnerInput,
  ChangePublicationOwnerMutation,
  ChangePublicationOwnerMutationVariables,
} from '../../types';
import { mutationResolver } from '../../utils';

const functionName = 'changePublicationOwner';

export interface WithChangePublicationOwnerProps {
  [functionName]: (input: ChangePublicationOwnerInput, publicationOid: string) => Promise<PublicationPowerUsers>;
}

export const withChangePublicationOwner =
  graphql<
    {},
    ChangePublicationOwnerMutation,
    ChangePublicationOwnerMutationVariables,
    WithChangePublicationOwnerProps
  >(changePublicationOwnerMutation, {
    props: ({mutate}) => ({
      [functionName]: async (input: ChangePublicationOwnerInput, publicationOid: string) => {
        return await mutationResolver<ChangePublicationOwnerMutation>(mutate, {
          variables: { input, publicationOid }
        }, functionName);
      }
    })
  });
