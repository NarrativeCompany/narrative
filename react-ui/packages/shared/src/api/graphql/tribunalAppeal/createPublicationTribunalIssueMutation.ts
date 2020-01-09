import gql from 'graphql-tag';
import { TribunalIssueDetailFragment } from '../fragments/tribunalIssueDetailFragment';

export const createPublicationTribunalIssueMutation = gql`
  mutation CreatePublicationTribunalIssueMutation
  ($input: CreatePublicationTribunalIssueInput!, $publicationOid: String!) {
    createPublicationTribunalIssue (input: $input, publicationOid: $publicationOid) 
    @rest (type: "TribunalIssueDetail", path: "/tribunal/appeals/publications/{args.publicationOid}", method: "POST") {
      ...TribunalIssueDetail
    }
  }
  ${TribunalIssueDetailFragment}
`;
