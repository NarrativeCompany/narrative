import {
  UpdateNicheModeratorSlotsMutation,
  UpdateNicheModeratorSlotsMutation_updateNicheModeratorSlots,
  UpdateNicheModeratorSlotsMutationVariables
} from '../../types';
import { buildMutationImplFunction } from '../../utils';
import { updateNicheModeratorSlotsMutation } from '../graphql/niche/updateNicheModeratorSlotsMutation';

export interface WithUpdateNicheModeratorSlotsProps {
  updateNicheModeratorSlots: (input: UpdateNicheModeratorSlotsMutationVariables) =>
    Promise<UpdateNicheModeratorSlotsMutation_updateNicheModeratorSlots>;
}

export const withUpdateNicheModeratorSlots =
  buildMutationImplFunction<UpdateNicheModeratorSlotsMutationVariables, UpdateNicheModeratorSlotsMutation>(
    updateNicheModeratorSlotsMutation,
    'updateNicheModeratorSlots'
  );
