import * as React from 'react';
import { WithUserKycProps } from './MemberCertificationDetails';
import { MemberCertificationStepsDescription } from './MemberCertificationStepsDescription';
import { MemberCertificationStepOne } from './MemberCertificationStepOne';
import { MemberCertificationStepTwo } from './MemberCertificationStepTwo';

export const MemberCertificationSteps: React.SFC<WithUserKycProps> = (props) => {
  return (
    <React.Fragment>
      <MemberCertificationStepsDescription {...props}/>
      <MemberCertificationStepOne {...props}/>
      <MemberCertificationStepTwo {...props}/>
    </React.Fragment>
  );
};
