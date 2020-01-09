import * as React from 'react';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import { compose } from 'recompose';
import { MemberReferralDetailsIntro } from './MemberReferralDetailsIntro';
import { MemberReferralDetailsStats } from './MemberReferralDetailsStats';
import { MemberProfileConnect, WithMemberProfileProps } from '../../../shared/context/MemberProfileContext';
import { MemberProfileHeaderText } from '../MemberProfile';
import { SEO } from '../../../shared/components/SEO';
import { MemberReferralDetailsMessages } from '../../../shared/i18n/MemberReferralDetailsMessages';

type Props =
  WithMemberProfileProps &
  InjectedIntlProps;

const MemberReferralDetailsComponent: React.SFC<Props> = (props) => {
  const {
    detailsForProfile,
    detailsForProfile: {user: {displayName}},
    isForCurrentUser,
    intl: {formatMessage}
  } = props;

  return (
    <React.Fragment>
      <SEO title={formatMessage(MemberReferralDetailsMessages.Title, {displayName})} />

      {isForCurrentUser &&
        <MemberProfileHeaderText>
          <MemberReferralDetailsIntro />
        </MemberProfileHeaderText>
      }

      <MemberReferralDetailsStats userDetails={detailsForProfile} />
    </React.Fragment>
  );
};

const MemberReferralDetails = compose(
  injectIntl,
  MemberProfileConnect,
)(MemberReferralDetailsComponent) as React.ComponentClass<{}>;

export default MemberReferralDetails;
