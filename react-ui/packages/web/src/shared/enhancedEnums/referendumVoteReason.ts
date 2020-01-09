import { ReferendumMessages } from '../i18n/ReferendumMessages';
import { ReferendumVoteReason } from '@narrative/shared';
import { EnumEnhancer } from '../utils/enhancedEnumUtils';
import { FormattedMessage } from 'react-intl';

// jw: first, we need to define the helper object for ReferendumVoteReason
class ReferendumVoteReasonHelper {
  descriptionMessage: FormattedMessage.MessageDescriptor;
  radioMessage: FormattedMessage.MessageDescriptor;
  assertionMessage: FormattedMessage.MessageDescriptor;
  reason: ReferendumVoteReason;

  constructor(
    reason: ReferendumVoteReason,
    descriptionMessage: FormattedMessage.MessageDescriptor,
    radioMessage: FormattedMessage.MessageDescriptor,
    assertionMessage: FormattedMessage.MessageDescriptor
  ) {
    this.reason = reason;
    this.descriptionMessage = descriptionMessage;
    this.radioMessage = radioMessage;
    this.assertionMessage = assertionMessage;
  }
}

// jw: next: lets create the lookup of ReferendumVoteReason to helper object
const referendumVoteReasonHelpers: {[key: number]: ReferendumVoteReasonHelper} = [];
referendumVoteReasonHelpers[ReferendumVoteReason.REDUNDANT] = new ReferendumVoteReasonHelper(
  ReferendumVoteReason.REDUNDANT,
  ReferendumMessages.RedundantReasonDescription,
  ReferendumMessages.RedundantReasonRadio,
  ReferendumMessages.UniqueAssertion
);
referendumVoteReasonHelpers[ReferendumVoteReason.UNCLEAR_NAME_OR_DESCRIPTION] = new ReferendumVoteReasonHelper(
  ReferendumVoteReason.UNCLEAR_NAME_OR_DESCRIPTION,
  ReferendumMessages.UnclearNameOrDescriptionReasonDescription,
  ReferendumMessages.UnclearNameOrDescriptionReasonRadio,
  ReferendumMessages.ClearlyDefinedAssertion
);
referendumVoteReasonHelpers[ReferendumVoteReason.WRONG_LANGUAGE] = new ReferendumVoteReasonHelper(
  ReferendumVoteReason.WRONG_LANGUAGE,
  ReferendumMessages.WrongLanguageReasonDescription,
  ReferendumMessages.WrongLanguageReasonRadio,
  ReferendumMessages.LanguageAssertion
);
referendumVoteReasonHelpers[ReferendumVoteReason.SPELLING_ISSUE_IN_NAME] = new ReferendumVoteReasonHelper(
  ReferendumVoteReason.SPELLING_ISSUE_IN_NAME,
  ReferendumMessages.SpellingIssueInNameReasonDescription,
  ReferendumMessages.SpellingIssueInNameReasonRadio,
  ReferendumMessages.SpelledProperlyAssertion
);
referendumVoteReasonHelpers[ReferendumVoteReason.CONTAINS_PROFANITY] = new ReferendumVoteReasonHelper(
  ReferendumVoteReason.CONTAINS_PROFANITY,
  ReferendumMessages.ContainsProfanityReasonDescription,
  ReferendumMessages.ContainsProfanityReasonRadio,
  ReferendumMessages.ContainsNoProfanityAssertion
);
referendumVoteReasonHelpers[ReferendumVoteReason.VIOLATES_TOS] = new ReferendumVoteReasonHelper(
  ReferendumVoteReason.VIOLATES_TOS,
  ReferendumMessages.ViolatesTosReasonDescription,
  ReferendumMessages.ViolatesTosReasonRadio,
  ReferendumMessages.CompliesWithTosAssertion
);

// jw: finally, let's create the enhancer, which will allow us to lookup helpers by enum instance.
export const EnhancedReferendumVoteReason = new EnumEnhancer<ReferendumVoteReason, ReferendumVoteReasonHelper>(
  referendumVoteReasonHelpers
);
