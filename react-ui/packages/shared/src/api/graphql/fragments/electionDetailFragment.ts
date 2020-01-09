import gql from 'graphql-tag';
import { ElectionFragment } from './electionFragment';
import { ElectionNomineeFragment } from './electionNomineeFragment';

export const ElectionDetailFragment = gql`
  fragment ElectionDetail on ElectionDetail {
    oid
    election @type(name: "Election") {
      ...Election
    }
    currentUserNominee @type(name: "ElectionNominee") {
      ...ElectionNominee
    } 
  }
  ${ElectionFragment}
  ${ElectionNomineeFragment}
`;
