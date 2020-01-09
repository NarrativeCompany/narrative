import { EmailAddressVerificationStep } from '@narrative/shared';
import { EnumEnhancer } from '../utils/enhancedEnumUtils';

// jw: let's define the EmailAddressVerificationStepHelper that will provide all the extra helper logic for 
//     EmailAddressVerificationSteps
export class EmailAddressVerificationStepHelper {
  type: EmailAddressVerificationStep;

  constructor(type: EmailAddressVerificationStep) {
    this.type = type;
  }

  isVerifyPending(): boolean {
    return this.type === EmailAddressVerificationStep.VERIFY_PENDING;
  }

  getOtherEmailAddress(emailAddress: string, pendingEmailAddress: string): string {
    if (this.isVerifyPending()) {
      return emailAddress;
    }

    // todo:error-handling: assert that this.isVerifyPrimary since that is the only other case we have right now.
    return pendingEmailAddress;
  }
}

// jw: next: lets create the lookup of EmailAddressVerificationStep to helper object

const verifiedEmailAddressTypeHelpers: {[key: number]: EmailAddressVerificationStepHelper} = [];
// jw: make sure to register these in the order you want them to display.
verifiedEmailAddressTypeHelpers[EmailAddressVerificationStep.VERIFY_PRIMARY] = new EmailAddressVerificationStepHelper(
  EmailAddressVerificationStep.VERIFY_PRIMARY
);
verifiedEmailAddressTypeHelpers[EmailAddressVerificationStep.VERIFY_PENDING] = new EmailAddressVerificationStepHelper(
  EmailAddressVerificationStep.VERIFY_PENDING
);

// jw: finally, let's create the enhancer, which will allow us to lookup helpers by enum instance.
export const EnhancedEmailAddressVerificationStep = new EnumEnhancer<
  // jw: really not sure how best to structure this so it makes sense. up on the line above is too long, so it needs to
  //     be broken up, but nothing feels "perfect".
  EmailAddressVerificationStep,
  EmailAddressVerificationStepHelper
>(
  verifiedEmailAddressTypeHelpers
);
