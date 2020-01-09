import gql from 'graphql-tag';

export const PublicationPlanDetailFragment = gql`
  fragment PublicationPlanDetail on PublicationPlanDetail {
    oid

    plan
    withinTrialPeriod
    withinRenewalPeriod
    eligibleForDiscount
    endDatetime
    deletionDatetime
    admins
    editors
    writers
    renewalPlans
    upgradePlans
  }
`;
