import * as React from 'react';
import { UserEmailAddressDetail } from '@narrative/shared';
import { LocalizedTime } from '../../../../shared/components/LocalizedTime';
import { FlexContainer } from '../../../../shared/styled/shared/containers';
import { Label } from '../../settingsStyles';
import { FormattedMessage } from 'react-intl';
import { MemberAccountSettingsMessages } from '../../../../shared/i18n/MemberAccountSettingsMessages';
import { EnhancedEmailAddressVerificationStep } from '../../../../shared/enhancedEnums/emailAddressVerificationStep';
import { Block } from '../../../../shared/components/Block';

interface Props {
  emailAddressDetail: UserEmailAddressDetail;
}

export const PendingEmailAddressField: React.SFC<Props> = (props) => {
  const {
    emailAddress,
    pendingEmailAddress,
    incompleteVerificationSteps,
    pendingEmailAddressExpirationDatetime
  } = props.emailAddressDetail;

  // jw: if there is no pending email address then no need to include anything here.
  if (!pendingEmailAddress || !pendingEmailAddressExpirationDatetime || !incompleteVerificationSteps) {
    return null;
  }

  const expirationDatetime = <LocalizedTime time={pendingEmailAddressExpirationDatetime} />;

  let description: React.ReactNode;
  if (incompleteVerificationSteps.length === 1) {
    const stepToVerify = EnhancedEmailAddressVerificationStep.get(incompleteVerificationSteps[0]);
    const otherEmailAddress = stepToVerify.getOtherEmailAddress(emailAddress, pendingEmailAddress);

    description = (
      <FormattedMessage
        {...MemberAccountSettingsMessages.YouHaveUntilXToVerifyOneStep}
        values={{ expirationDatetime, otherEmailAddress }}
      />
    );

  } else {
    // todo:error-handling: we should assert here that we only have 2 steps, which is currently the max

    description = (
      <FormattedMessage
        {...MemberAccountSettingsMessages.YouHaveUntilXToVerifyTwoSteps}
        values={{ expirationDatetime, emailAddress, pendingEmailAddress }}
      />
    );
  }

  return (
    <React.Fragment>
      <FlexContainer alignItems="flex-start">
        <Label uppercase={true} size={6}>
          <FormattedMessage {...MemberAccountSettingsMessages.PendingEmailAddress}/>
        </Label>

        <Block>
          <Block>
            {pendingEmailAddress}
          </Block>
          <Block size="small" color="light">
            {description}
          </Block>
        </Block>
      </FlexContainer>
    </React.Fragment>
  );
};
