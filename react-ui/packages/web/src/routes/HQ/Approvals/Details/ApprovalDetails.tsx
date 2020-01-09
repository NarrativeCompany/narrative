import * as React from 'react';
import { compose, withProps } from 'recompose';
import {
  CompositionConsumerType,
  Referendum,
  ReferendumOidProps,
  withReferendum,
  WithReferendumProps
} from '@narrative/shared';
import { viewWrapperPlaceholder, withLoadingPlaceholder } from '../../../../shared/utils/withLoadingPlaceholder';
import { RouteComponentProps, withRouter } from 'react-router-dom';
import { ReferendumVoteDetails } from '../../../../shared/components/referendum/ReferendumVoteDetails';
import { ApprovalActionCard } from './ApprovalActionCard';
import { SecondaryDetailsViewWrapper } from '../../../../shared/components/SecondaryDetailsViewWrapper';
import { ApprovalDetailsMessages } from '../../../../shared/i18n/ApprovalDetailsMessages';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { Link } from '../../../../shared/components/Link';
import { WebRoute } from '../../../../shared/constants/routes';
import { SimilarNichesSidebarCard } from '../../../../shared/components/sidebar/SimilarNichesSidebarCard';
import { SEO } from '../../../../shared/components/SEO';
import { SEOMessages } from '../../../../shared/i18n/SEOMessages';
import { generatePath } from 'react-router';
import { CommentsSection } from '../../../../shared/components/comment/CommentsSection';
import { ReferendumSummarySection } from '../../../../shared/components/referendum/ReferendumSummarySection';
import { DetailsGradientBox } from '../../../../shared/components/DetailsGradientBox';

export interface ReferendumProps {
  referendum: Referendum;
  commentsSectionKey?: string;
}

type Props =
  ReferendumProps &
  InjectedIntlProps ;

const ApprovalDetailsComponent: React.SFC<Props> = (props) => {
  const {intl, referendum, commentsSectionKey} = props;
  const referendumOid = referendum.oid;

  if (!referendum.niche) {
    // todo:error-handling log this
    throw new Error('No niche found on Referendum!');
  }

  return (
    <SecondaryDetailsViewWrapper
      iconType="review"
      channel={referendum.niche}
      title={<FormattedMessage {...ApprovalDetailsMessages.NicheApproval} />}
      listLink={<Link to={WebRoute.Approvals}><FormattedMessage {...ApprovalDetailsMessages.AllApprovals} /></Link>}
      status={referendum.open ? undefined : ApprovalDetailsMessages.VotingHasEnded}
      sidebarItems={<SimilarNichesSidebarCard niche={referendum.niche}/>}
    >
      <SEO
        title={intl.formatMessage(SEOMessages.ApprovalDetailsTitle) + ' - ' + referendum.niche.name}
        description={referendum.niche.description}

      />
      <ApprovalActionCard referendum={referendum}/>

      <ReferendumSummarySection referendum={referendum}/>

      <ReferendumVoteDetails referendum={referendum}/>

      <CommentsSection
        key={commentsSectionKey}
        consumerType={CompositionConsumerType.referendums}
        consumerOid={referendum.oid}
        toDetails={generatePath(WebRoute.ApprovalDetails, { referendumOid })}
        allowNewComments={referendum.open}
      />

    </SecondaryDetailsViewWrapper>
  );
};

const ApprovalDetails = compose(
  withRouter,
  withProps((props: RouteComponentProps<ReferendumOidProps>) => {
    const referendumOid = props.match.params.referendumOid;
    return {referendumOid};
  }),
  withReferendum,
  withProps((props: WithReferendumProps) => {
    const {loading, getReferendum} = props.referendumData;

    // zb: every time the user votes to reject and supplies a comment
    // we will set the commentsectionkey to the commentOid
    let commentsSectionKey: string | undefined;

    if (!loading
      && getReferendum
      && getReferendum.currentUserVote
      && getReferendum.currentUserVote.commentOid
    ) {
      commentsSectionKey = getReferendum.currentUserVote.commentOid;
    }

    return {
      loading,
      referendum: getReferendum,
      commentsSectionKey
    };
  }),
  withLoadingPlaceholder(viewWrapperPlaceholder(() => <DetailsGradientBox color="blue"/>))
)(ApprovalDetailsComponent) as React.ComponentClass<ReferendumOidProps>;

export default compose(
  injectIntl
)(ApprovalDetails);
