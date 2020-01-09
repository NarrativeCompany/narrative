import * as React from 'react';
import { compose, withProps } from 'recompose';
import { Row } from 'antd';
import { MemberReferralDetailsMessages } from '../../../shared/i18n/MemberReferralDetailsMessages';
import { FormattedMessage } from 'react-intl';
import {
  UserReferralDetails,
  UserDetail,
  withUserReferralDetails,
  WithUserReferralDetailsProps
} from '@narrative/shared';
import styled from '../../../shared/styled';
import { LocalizedNumber } from '../../../shared/components/LocalizedNumber';
import { MemberReferralDetailCol } from './MemberReferralDetailCol';
import { RowProps } from 'antd/lib/grid';
import { withLoadingPlaceholder, WithLoadingPlaceholderProps } from '../../../shared/utils/withLoadingPlaceholder';

interface WithProps {
  referralDetails: UserReferralDetails;
}

interface ParentProps {
  userDetails: UserDetail;
}

type Props =
  WithUserReferralDetailsProps &
  ParentProps &
  WithLoadingPlaceholderProps &
  WithProps;

const DetailsRow = styled<RowProps>((props) => <Row {...props} />)`
  margin-bottom: 15px;
`;

const MemberReferralDetailsStatsComponent: React.SFC<Props> = (props) => {
  const { referralDetails } = props;

  let rank;
  if (referralDetails.rank == null) {
    rank = <FormattedMessage {...MemberReferralDetailsMessages.ReferralDetailsUnranked} />;
  } else {
    rank = (
      <FormattedMessage
        {...MemberReferralDetailsMessages.ReferralDetailsRankValue}
        values={{rank: referralDetails.rank}}
      />
    );
  }

  return (
    <DetailsRow>
      <MemberReferralDetailCol label={<FormattedMessage {...MemberReferralDetailsMessages.ReferralDetailsRank} />}>
        {rank}
      </MemberReferralDetailCol>

      <MemberReferralDetailCol
        label={<FormattedMessage {...MemberReferralDetailsMessages.ReferralDetailsFriendsJoined} />}>
        <LocalizedNumber value={referralDetails.friendsJoined} />
      </MemberReferralDetailCol>

      <MemberReferralDetailCol
        label={<FormattedMessage {...MemberReferralDetailsMessages.ReferralDetailsNrveEarned} />}>
        {referralDetails.nrveEarned}
      </MemberReferralDetailCol>
    </DetailsRow>
  );
};

export const MemberReferralDetailsStats = compose(
  // jw: first, let's extract the users OID out of the userDetails and make it available for the apollo query input.
  withProps((props: Props) => {
    const userOid = props.userDetails &&
      props.userDetails.user &&
      props.userDetails.user.oid;

    return { userOid };
  }),
  withUserReferralDetails,
  // jw: next, let's pull the referral details and loading flag off of the hql query results and expose that.
  withProps((props: Props) => {
    const { userReferralDetailsData } = props;
    const { getUserReferralDetails, loading } = userReferralDetailsData;

    return { referralDetails: getUserReferralDetails, loading };
  }),
  withLoadingPlaceholder()
)(MemberReferralDetailsStatsComponent) as React.ComponentClass<ParentProps>;
