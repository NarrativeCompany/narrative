import * as React from 'react';
import { compose, withProps } from 'recompose';
import { CardProps } from '../../../shared/components/Card';
import { NicheCard } from '../components/NicheCard';
import { NicheCardUser } from '../components/NicheCardUser';
import { ChannelCardTitleAndDesc } from '../components/ChannelCardTitleAndDesc';
import { ApprovalCardVoteButtons } from './ApprovalCardVoteButtons';
import { ApprovalCardActions } from './ApprovalCardActions';
import { ApprovalSimilarNiches } from './ApprovalSimilarNiches';
import { NicheCardProgressBar } from '../components/NicheCardProgressBar';
import { Niche, Referendum, User } from '@narrative/shared';
import { getNicheFromReferendum, getSuggesterFromReferendum } from '../../../shared/utils/ballotBoxUtils';
import { themeColors } from '../../../shared/styled/theme';
import { generatePath } from 'react-router';
import { WebRoute } from '../../../shared/constants/routes';
import { getApprovalPercentage } from '../../../shared/utils/referendumUtils';

interface WithProps {
  niche: Niche;
  suggester: User;
  nicheApprovalPercentage: number;
  referendumHasVotes: boolean;
}

// tslint:disable no-any
interface ParentProps {
  referendum: Referendum;
  toggleCard: () => any;
}
// tslint:enable no-any

type Props =
  WithProps &
  ParentProps &
  CardProps &
  WithProps;

export const ApprovalCardFrontComponent: React.SFC<Props> = (props) => {
  const { toggleCard, referendum, niche, suggester, nicheApprovalPercentage, referendumHasVotes } = props;

  const referendumOid = referendum.oid;

  const ProgressBar = (
    <NicheCardProgressBar
      percent={nicheApprovalPercentage}
      strokeColor={themeColors.secondaryBlue}
      progressInnerBg={referendumHasVotes ? 'error' : undefined}
    />
  );

  return (
    <NicheCard
      cover={ProgressBar}
      height={400}
      actions={[<ApprovalSimilarNiches key="similar-niches" nicheId={niche.oid}/>]}
    >
      <NicheCardUser user={suggester} />

      <ChannelCardTitleAndDesc
        channel={niche}
        linkPath={generatePath(WebRoute.ApprovalDetails, {referendumOid})}
        center={true}
        forListCard={true}
      />

      <ApprovalCardVoteButtons
        showRejectReasonForm={toggleCard}
        referendum={referendum}
      />

      <ApprovalCardActions
        referendum={referendum}
        approvalRating={nicheApprovalPercentage}
      />
    </NicheCard>
  );
};

export const ApprovalCardFront = compose(
  withProps((props: Props) => {
    const { referendum } = props;

    const niche = getNicheFromReferendum(referendum);
    const suggester = getSuggesterFromReferendum(referendum);
    const nicheApprovalPercentage = getApprovalPercentage(referendum.votePointsFor, referendum.votePointsAgainst);
    const referendumHasVotes = (parseFloat(referendum.votePointsAgainst) + parseFloat(referendum.votePointsFor)) > 0;

    return { niche, suggester, nicheApprovalPercentage, referendumHasVotes };
  })
)(ApprovalCardFrontComponent) as React.ComponentClass<ParentProps>;
