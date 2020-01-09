import gql from 'graphql-tag';
import { TribunalIssueDetailFragment } from '../fragments/tribunalIssueDetailFragment';

export const createNicheTribunalIssueMutation = gql`
  mutation CreateNicheTribunalIssueMutation ($input: CreateNicheTribunalIssueInput!, $nicheOid: String!) {
    createNicheTribunalIssue (input: $input, nicheOid: $nicheOid) 
    @rest (type: "TribunalIssueDetail", path: "/tribunal/appeals/niches/{args.nicheOid}", method: "POST") {
      ...TribunalIssueDetail
    }
  }
  ${TribunalIssueDetailFragment}
`;
