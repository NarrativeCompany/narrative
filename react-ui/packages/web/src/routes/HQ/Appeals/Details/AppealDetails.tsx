import * as React from 'react';
import {
  CompositionConsumerType,
  TribunalIssueDetail,
  TribunalIssueType,
  withTribunalAppealSummary,
  WithTribunalAppealSummaryProps
} from '@narrative/shared';
import { compose, withProps } from 'recompose';
import { generatePath, RouteComponentProps } from 'react-router';
import { NotFound } from '../../../../shared/components/NotFound';
import { SecondaryDetailsViewWrapper } from '../../../../shared/components/SecondaryDetailsViewWrapper';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { AppealDetailsMessages } from '../../../../shared/i18n/AppealDetailsMessages';
import { Link } from '../../../../shared/components/Link';
import { WebRoute } from '../../../../shared/constants/routes';
import { viewWrapperPlaceholder, withLoadingPlaceholder } from '../../../../shared/utils/withLoadingPlaceholder';
import { ReferendumVoteDetails } from '../../../../shared/components/referendum/ReferendumVoteDetails';
import { SimilarNichesSidebarCard } from '../../../../shared/components/sidebar/SimilarNichesSidebarCard';
import { AppealActionCard } from './AppealActionCard';
import { TribunalMembersSidebarCard } from './TribunalMembersSidebarCard';
import { SEO } from '../../../../shared/components/SEO';
import { SEOMessages } from '../../../../shared/i18n/SEOMessages';
import { CommentsSection } from '../../../../shared/components/comment/CommentsSection';
import { ReferendumSummarySection } from '../../../../shared/components/referendum/ReferendumSummarySection';
import { DetailsGradientBox } from '../../../../shared/components/DetailsGradientBox';

interface RouteProps {
  tribunalIssueOid: string;
}

interface WithProps {
  tribunalIssueDetail: TribunalIssueDetail;
}

type Props =
  WithProps &
  InjectedIntlProps;

const TribunalAppealDetailsComponent: React.SFC<Props> = (props) => {
  const { intl, tribunalIssueDetail } = props;

  if (!tribunalIssueDetail) {
    return <NotFound/>;
  }

  const { tribunalIssue } = tribunalIssueDetail;
  const tribunalIssueOid = tribunalIssue.oid;
  const { referendum, type } = tribunalIssue;

  const isPublication = type === TribunalIssueType.RATIFY_PUBLICATION;
  const isNiche = !!referendum.niche;
  const channel = referendum.niche || referendum.publication || undefined;
  const deletedChannel = referendum.deletedChannel || undefined;

  if (!channel && !deletedChannel) {
    // todo:error-handling log this
    throw new Error('Failed identifying channel for TribunalIssue');
  }

  const forNicheEdit = isNiche && tribunalIssue.type === TribunalIssueType.APPROVE_NICHE_DETAIL_CHANGE;

  const titleMessage = forNicheEdit
    ? AppealDetailsMessages.EditRequest
    : isPublication ? AppealDetailsMessages.PublicationAppeal : AppealDetailsMessages.NicheAppeal;

  const seoTitleMessage = isPublication
    ? SEOMessages.PublicationAppealDetailTitle
    : SEOMessages.NicheAppealDetailTitle;

  const channelName = channel
    ? channel.name
    : deletedChannel
      ? deletedChannel.name
      : '';

  return (
    <SecondaryDetailsViewWrapper
      channel={channel}
      deletedChannel={deletedChannel}
      iconType={forNicheEdit ? 'edit' : 'appeals'}
      title={<FormattedMessage {...titleMessage} />}
      listLink={<Link to={WebRoute.Appeals}><FormattedMessage {...AppealDetailsMessages.AllAppeals} /></Link>}
      status={referendum.open ? undefined : AppealDetailsMessages.VotingHasEnded}
      sidebarItems={
        <React.Fragment>
          <TribunalMembersSidebarCard />
          {referendum.niche && <SimilarNichesSidebarCard niche={referendum.niche} />}
        </React.Fragment>
      }
    >

      <SEO
        title={intl.formatMessage(seoTitleMessage) + ' - ' + channelName}
        description={channel ? channel.description : undefined}
      />

      {/* jw: the first piece of meaningful data is the action card */}
      <AppealActionCard issueDetails={tribunalIssueDetail} />

      {/* jw: the second piece of data is the summary... Give them summary! */}
      <ReferendumSummarySection referendum={referendum}/>

      {/* jw: the third piece of data is the vote details! */}
      <ReferendumVoteDetails referendum={referendum} />

      {/* jw: finally, let's include the comments for this appeal */}
      <CommentsSection
        consumerType={CompositionConsumerType.referendums}
        consumerOid={referendum.oid}
        toDetails={generatePath(WebRoute.AppealDetails, { tribunalIssueOid })}
        typeSpecificPostingPermission="participateInTribunalActions"
        allowNewComments={referendum.open}
        disableCommentRating={true}
      />

    </SecondaryDetailsViewWrapper>
  );
};

export default compose(
  injectIntl,
  withProps((props: RouteComponentProps<RouteProps>) => {
    return { tribunalIssueOid: props.match.params.tribunalIssueOid };
  }),
  withTribunalAppealSummary,
  withProps((props: WithTribunalAppealSummaryProps) => {
    const { tribunalAppealSummaryData } = props;
    const { loading, getTribunalAppealSummary } = tribunalAppealSummaryData;

    return { loading, tribunalIssueDetail: getTribunalAppealSummary };
  }),
  withLoadingPlaceholder(viewWrapperPlaceholder(() => <DetailsGradientBox color="blue"/>))
)(TribunalAppealDetailsComponent) as React.ComponentClass<{}>;
