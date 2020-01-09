import gql from 'graphql-tag';
import { UserFragment } from './userFragment';
import { NicheFragment } from './nicheFragment';
import { PublicationFragment } from './publicationFragment';
import { NicheAuctionFragment } from './nicheAuctionFragment';
import { NicheAuctionBidFragment } from './nicheAuctionBidFragment';
import { TribunalIssueFragment } from './tribunalIssueFragment';
import { TribunalIssueReportFragment } from './tribunalIssueReportFragment';
import { ElectionFragment } from './electionFragment';
import { InvoiceFragment } from './invoiceFragment';
import { PostFragment } from './postFragment';

export const LedgerEntryFragment = gql`
  fragment LedgerEntry on LedgerEntry {
    oid
    actor @type(name: "User") {
      ...User
    }
    type
    eventDatetime
    niche @type(name: "Niche") {
      ...Niche
    }
    publication @type(name: "Publication") {
      ...Publication
    }
    auction @type(name: "NicheAuction") {
      ...NicheAuction
    }
    auctionBid @type(name: "NicheAuctionBid") {
      ...NicheAuctionBid
    }
    tribunalIssue @type(name: "TribunalIssue") {
      ...TribunalIssue
    }
    tribunalIssueReport @type(name: "TribunalIssueReport") {
      ...TribunalIssueReport
    }
    referendum @type(name: "Referendum") {
      ...Referendum
    }
    election @type(name: "Election") {
      ...Election
    }
    invoice @type(name: "Invoice") {
      ...Invoice
    }
    wasReferendumVotedFor
    postOid
    post @type(name: "Post") {
      ...Post
    }
    commentOid
    author @type(name: "User") {
      ...User
    }
    securityDepositValue
    publicationPlan
    publicationPaymentType
  }
  ${UserFragment}
  ${NicheFragment}
  ${PublicationFragment}
  ${NicheAuctionFragment}
  ${NicheAuctionBidFragment}
  ${TribunalIssueFragment}
  ${TribunalIssueReportFragment}
  ${ElectionFragment}
  ${InvoiceFragment}
  ${PostFragment}
`;
