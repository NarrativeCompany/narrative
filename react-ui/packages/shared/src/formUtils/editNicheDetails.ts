import { nicheDetailsFormUtil, NicheDetailsFormValues, nicheDetailsInitialValues } from './shared';

export const editNicheDetailsFormUtil = {
  ...nicheDetailsFormUtil,
  mapPropsToValues: (defaultValues: NicheDetailsFormValues) => ({...nicheDetailsInitialValues, ...defaultValues})
};
