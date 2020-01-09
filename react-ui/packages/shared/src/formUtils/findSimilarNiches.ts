import { nicheDetailsFormUtil, nicheDetailsInitialValues } from './shared';

export const findSimilarNichesFormUtil = {
  ...nicheDetailsFormUtil,
  mapPropsToValues: () => (nicheDetailsInitialValues)
};
