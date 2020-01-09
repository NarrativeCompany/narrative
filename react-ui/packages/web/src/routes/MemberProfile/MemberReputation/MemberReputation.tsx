import * as React from 'react';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { ReputationScore } from './ReputationScore';
import { ReputationBreakdown } from './ReputationBreakdown';
import { compose, withProps } from 'recompose';
import { MemberProfileConnect, WithMemberProfileProps } from '../../../shared/context/MemberProfileContext';
import { UserDetail, UserReputation } from '@narrative/shared';
import { MemberReputationMessages } from '../../../shared/i18n/MemberReputationMessages';
import { MemberProfileHeaderText } from '../MemberProfile';
import { SEO } from '../../../shared/components/SEO';

export interface WithUserReputationProps {
  // jw: this comes from MemberProfileContext
  detailsForProfile: UserDetail;
  isForCurrentUser: boolean;
  userReputation: UserReputation;
}

type Props =
  WithUserReputationProps &
  InjectedIntlProps;

const MemberReputation: React.SFC<Props> = (props) => {
  const {
    detailsForProfile: {user: {displayName}},
    intl: { formatMessage }
  } = props;

  return (
    <React.Fragment>
      <SEO title={formatMessage(MemberReputationMessages.PageHeaderTitle, {displayName})} />

      <MemberProfileHeaderText>
        <FormattedMessage {...MemberReputationMessages.PageHeaderDescription}/>
      </MemberProfileHeaderText>

      <ReputationScore {...props}/>
      <ReputationBreakdown {...props}/>
    </React.Fragment>
  );
};

export default compose(
  injectIntl,
  MemberProfileConnect,
  withProps((props: WithMemberProfileProps) => {
    const { detailsForProfile: { user } } = props;

    return { userReputation: user.reputation };
  })
)(MemberReputation);
