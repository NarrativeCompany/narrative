import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { SectionHeader } from '../../../shared/components/SectionHeader';
import { ReputationScoreCard } from './ReputationScoreCard';
import { MemberReputationMessages } from '../../../shared/i18n/MemberReputationMessages';
import { WithUserReputationProps } from './MemberReputation';

export const ReputationScore: React.SFC<WithUserReputationProps> = (props) => {
  const { isForCurrentUser } = props;

  const titleMessage = isForCurrentUser ?
    MemberReputationMessages.ReputationScoreSectionTitleForCurrentUser :
    MemberReputationMessages.ReputationScoreSectionTitle;

  return (
    <React.Fragment>
      <SectionHeader title={<FormattedMessage {...titleMessage}/>}/>

      <ReputationScoreCard {...props}/>
    </React.Fragment>
  );
};
