import * as React from 'react';
import { compose } from 'recompose';
import { UserKyc, withCurrentUserKyc } from '@narrative/shared';
import { withLoadingPlaceholder } from '../../../shared/utils/withLoadingPlaceholder';
import { EnhancedUserKycStatus } from '../../../shared/enhancedEnums/userKycStatus';
import { MemberCertificationSteps } from './MemberCertificationSteps';
import { MemberCertificationDescription } from './MemberCertificationDescription';

export interface WithUserKycProps {
  userKyc: UserKyc;
}

const MemberCertificationDetailsComponent: React.SFC<WithUserKycProps> = (props) => {
  const { userKyc } = props;

  const status = EnhancedUserKycStatus.get(userKyc.kycStatus);
  const showCertificationSteps = (
    status.isReadyForVerification() ||
    (status.isSupportsPayments() && userKyc.payPalCheckoutDetails)
  );

  return (
    <React.Fragment>
      <MemberCertificationDescription {...props}/>
      {showCertificationSteps && <MemberCertificationSteps {...props}/>}
    </React.Fragment>
  );
};

export const MemberCertificationDetails = compose(
  withCurrentUserKyc,
  withLoadingPlaceholder()
)(MemberCertificationDetailsComponent) as React.ComponentClass<{}>;
