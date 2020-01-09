import { EnumEnhancer } from '../utils/enhancedEnumUtils';
import { FormattedMessage, InjectedIntl } from 'react-intl';
import { ReferendumMessages } from '../i18n/ReferendumMessages';
import { Referendum, ReferendumType, ReferendumVote } from '@narrative/shared';
import { getApprovalPercentage, wasReferendumPassed } from '../utils/referendumUtils';

// jw: first, we need to define the helper object for ReferendumType
class ReferendumTypeHelper {
  type: ReferendumType;

  constructor(type: ReferendumType) {
    this.type = type;
  }

  public isRequiresRejectionReason(): boolean {
    switch (this.type) {
      case ReferendumType.APPROVE_SUGGESTED_NICHE:
      case ReferendumType.RATIFY_NICHE:
        return true;
      default:
        return false;
    }
  }

  public isTribunalType(): boolean {
    switch (this.type) {
      case ReferendumType.TRIBUNAL_APPROVE_NICHE_DETAIL_CHANGE:
      case ReferendumType.TRIBUNAL_RATIFY_NICHE:
      case ReferendumType.TRIBUNAL_RATIFY_PUBLICATION:
      case ReferendumType.TRIBUNAL_APPROVE_REJECTED_NICHE:
        return true;
      default:
        return false;
    }
  }

  public isNicheApproval(): boolean {
    return this.type === ReferendumType.APPROVE_SUGGESTED_NICHE;
  }

  public getVotedMessage(): FormattedMessage.MessageDescriptor {
    switch (this.type) {
      case ReferendumType.TRIBUNAL_APPROVE_NICHE_DETAIL_CHANGE:
        return ReferendumMessages.YouVotedToMessageForEdit;
      case ReferendumType.TRIBUNAL_RATIFY_PUBLICATION:
        return ReferendumMessages.YouVotedToPublicationMessage;
      default:
        return ReferendumMessages.YouVotedToNicheMessage;
    }
  }

  public getVoteActionMessage(vote: ReferendumVote): FormattedMessage.MessageDescriptor {
    if (!vote || !vote.votedFor) {
      return ReferendumMessages.Reject;
    }

    switch (this.type) {
      case ReferendumType.RATIFY_NICHE:
      case ReferendumType.TRIBUNAL_RATIFY_NICHE:
      case ReferendumType.TRIBUNAL_RATIFY_PUBLICATION:
        return ReferendumMessages.Keep;
      default:
        return ReferendumMessages.Approve;
    }
  }

  public getTypeMessage(): FormattedMessage.MessageDescriptor {
    switch (this.type) {
      case ReferendumType.APPROVE_SUGGESTED_NICHE:
        return ReferendumMessages.ApproveSuggestedNiche;
      case ReferendumType.RATIFY_NICHE:
        return ReferendumMessages.RatifyNiche;
      case ReferendumType.APPROVE_REJECTED_NICHE:
        return ReferendumMessages.ApproveRejectedNiche;
      case ReferendumType.TRIBUNAL_APPROVE_NICHE_DETAIL_CHANGE:
        return ReferendumMessages.TribunalApproveNicheDetailChange;
      case ReferendumType.TRIBUNAL_APPROVE_REJECTED_NICHE:
        return ReferendumMessages.TribunalApproveRejectedNiche;
      case ReferendumType.TRIBUNAL_RATIFY_PUBLICATION:
        return ReferendumMessages.TribunalRatifyPublication;
      case ReferendumType.TRIBUNAL_RATIFY_NICHE:
      default:
        // todo:error-handling: We need to log with the server if the type is not TRIBUNAL_RATIFY_NICHE
        return ReferendumMessages.TribunalRatifyNiche;
    }
  }

  public getResultTitleMessage(referendum: Referendum): FormattedMessage.MessageDescriptor {
    const approved = wasReferendumPassed(referendum);

    switch (this.type) {
      case ReferendumType.TRIBUNAL_APPROVE_NICHE_DETAIL_CHANGE:
        return approved
          ? ReferendumMessages.NicheDetailsApproved
          : ReferendumMessages.NicheDetailsRejected;
      case ReferendumType.APPROVE_SUGGESTED_NICHE:
        return approved
          ? ReferendumMessages.NicheApproved
          : ReferendumMessages.NicheRejected;
      case ReferendumType.APPROVE_REJECTED_NICHE:
      case ReferendumType.TRIBUNAL_APPROVE_REJECTED_NICHE:
        return approved
          ? ReferendumMessages.NicheApproved
          : ReferendumMessages.NicheStatusUnchanged;
      case ReferendumType.TRIBUNAL_RATIFY_PUBLICATION:
        return approved
          ? ReferendumMessages.PublicationStatusUnchanged
          : ReferendumMessages.PublicationRejected;
      default:
        // todo:error-handling: We need to log with the server if the type is not TRIBUNAL_RATIFY_NICHE or RATIFY_NICHE
        return approved
          ? ReferendumMessages.NicheStatusUnchanged
          : ReferendumMessages.NicheRejected;
    }
  }
  public getResultDescription(intl: InjectedIntl, referendum: Referendum): string {
    const approved = wasReferendumPassed(referendum);

    // jw: this deserves explanation: We want the highest percentage, so we will base this calculation off of the
    //     winning side of the debate. Hence why we will be passing the values reversed for rejected referendums.
    const rawPercentage = approved
      ? getApprovalPercentage(referendum.votePointsFor, referendum.votePointsAgainst)
      : getApprovalPercentage(referendum.votePointsAgainst, referendum.votePointsFor);
    const percentage = `${rawPercentage}%`;

    let message;
    switch (this.type) {
      case ReferendumType.TRIBUNAL_APPROVE_NICHE_DETAIL_CHANGE:
        message = approved
          ? ReferendumMessages.NicheDetailsChangePassed
          : ReferendumMessages.NicheDetailsChangeNotPassed;
        break;
      case ReferendumType.APPROVE_SUGGESTED_NICHE:
        message = approved
          ? ReferendumMessages.SuggestedNichePassed
          : ReferendumMessages.SuggestedNicheNotPassed;
        break;
      case ReferendumType.APPROVE_REJECTED_NICHE:
      case ReferendumType.TRIBUNAL_APPROVE_REJECTED_NICHE:
        message = approved
          ? ReferendumMessages.ApproveRejectedNichePassed
          : ReferendumMessages.ApproveRejectedNicheNotPassed;
        break;
      case ReferendumType.TRIBUNAL_RATIFY_PUBLICATION:
        message = approved
          ? ReferendumMessages.PublicationRatificationPassed
          : ReferendumMessages.PublicationRatificationNotPassed;
        break;
      default:
        // todo:error-handling: We need to log with the server if the type is not TRIBUNAL_RATIFY_NICHE or RATIFY_NICHE
        message = approved
          ? ReferendumMessages.NicheRatificationPassed
          : ReferendumMessages.NicheRatificationNotPassed;
    }

    return intl.formatMessage(message, { percentage });
  }
}

// jw: next: lets create the lookup of ReferendumType to helper object
const ReferendumTypeHelpers: {[key: number]: ReferendumTypeHelper} = [];

// jw: gonna use foreach for this since we need to control the array keys.
Object.values(ReferendumType).forEach((referendumType) => {
  ReferendumTypeHelpers[referendumType] = new ReferendumTypeHelper(referendumType);
});

// jw: finally, let's create the enhancer, which will allow us to lookup helpers by enum instance.
export const EnhancedReferendumType = new EnumEnhancer<ReferendumType, ReferendumTypeHelper>(
  ReferendumTypeHelpers
);
