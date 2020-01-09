import gql from 'graphql-tag';
import { ReferendumFragment } from '../fragments/referendumFragment';
import { PageInfoFragment } from '../fragments/pageInfoFragment';

export const allBallotBoxReferendumsQuery = gql`
  query AllBallotBoxReferendumsQuery ($size: Int, $page: Int) {
    getAllBallotBoxReferendums (size: $size, page: $page) 
    @rest(type: "BallotBoxReferendumsPayload", path: "/referendums/ballot-box?{args}") {
      items @type(name: "Referendum") {
        ...Referendum
      }
      info @type(name: "PageInfo") {
        ...PageInfo
      }
    }
  }
  ${ReferendumFragment}
  ${PageInfoFragment}
`;
