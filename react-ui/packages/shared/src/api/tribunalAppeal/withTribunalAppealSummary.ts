import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { tribunalAppealSummaryQuery } from '../graphql/tribunalAppeal/tribunalAppealSummaryQuery';
import { TribunalAppealSummaryQuery, TribunalAppealSummaryQueryVariables } from '../../types';

export interface TribunalIssueOidProps {
  tribunalIssueOid: string;
}

export type WithTribunalAppealSummaryProps =
  NamedProps<{tribunalAppealSummaryData: GraphqlQueryControls & TribunalAppealSummaryQuery}, TribunalIssueOidProps>;

export const withTribunalAppealSummary =
  graphql<
    TribunalIssueOidProps,
    TribunalAppealSummaryQuery,
    TribunalAppealSummaryQueryVariables
  >(tribunalAppealSummaryQuery, {
    skip: ({tribunalIssueOid}) => !tribunalIssueOid,
    options: ({tribunalIssueOid}) => ({
      variables: {
        input: {tribunalIssueOid}
      }
    }),
    name: 'tribunalAppealSummaryData'
  });
