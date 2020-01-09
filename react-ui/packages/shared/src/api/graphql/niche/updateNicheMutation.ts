import gql from 'graphql-tag';
import { TribunalIssueDetailFragment } from '../fragments/tribunalIssueDetailFragment';

export const updateNicheMutation = gql`
  mutation UpdateNicheMutation ($input: UpdateNicheInput!, $nicheOid: String!) {
    updateNiche (input: $input, nicheOid: $nicheOid) 
    @rest(type: "Referendum", path: "/niches/{args.nicheOid}", method: "PUT") {
      ...TribunalIssueDetail
    }
  }
  ${TribunalIssueDetailFragment}
`;
