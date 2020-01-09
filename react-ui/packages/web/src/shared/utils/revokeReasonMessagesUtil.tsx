import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { RevokeReasonMessages } from '../i18n/RevokeReasonMessages';
import { SharedComponentMessages } from '../i18n/SharedComponentMessages';
import {
  BidOnNichesRevokeReason,
  StandardRevokeReason,
  SubmitTribunalAppealsRevokeReason,
  SuggestNichesRevokeReason,
  User
} from '@narrative/shared';
import { Link, LinkStyleProps } from '../components/Link';
import { WebRoute } from '../constants/routes';
import { CountDown } from '../components/CountDown';
import { PermissionProps } from '../containers/withPermissionsModalController';
import { ModalActionProps, ModalName } from '../stores/ModalStore';

// This type can be extended and combine multiple revoke reason enums
// or maybe we just have a single RevokeReason enum that encompasses
// all permission types (suggest niche, bid, and tribunal action
// tslint:disable-next-line interface-over-type-literal
export type RevokeReasonType =
  SuggestNichesRevokeReason |
  StandardRevokeReason |
  BidOnNichesRevokeReason |
  SubmitTribunalAppealsRevokeReason;

function getRevokeReasonMessage(
  revokeReason: RevokeReasonType,
  attemptedActionMessage: FormattedMessage.MessageDescriptor
) {
  let errorMessage;
  let attemptedAction;

  switch (revokeReason) {
    case SuggestNichesRevokeReason.SUGGESTED_IN_LAST_24_HOURS:
      errorMessage = RevokeReasonMessages.SuggestNiche24Hours;
      break;
    case BidOnNichesRevokeReason.NICHE_SLOTS_FULL:
      errorMessage = RevokeReasonMessages.NicheSlotsFull;
      break;
    case BidOnNichesRevokeReason.SECURITY_DEPOSIT_REQUIRED:
      errorMessage = RevokeReasonMessages.SecurityDepositRequired;
      break;
    case SubmitTribunalAppealsRevokeReason.REPORTED_IN_LAST_24_HOURS:
      errorMessage = RevokeReasonMessages.AppealedInLast24Hours;
      break;
    case StandardRevokeReason.CONDUCT_NEGATIVE:
    case BidOnNichesRevokeReason.CONDUCT_NEGATIVE:
    case SuggestNichesRevokeReason.CONDUCT_NEGATIVE:
    case SubmitTribunalAppealsRevokeReason.CONDUCT_NEGATIVE:
      errorMessage = RevokeReasonMessages.ConductRevoke;
      attemptedAction = attemptedActionMessage || RevokeReasonMessages.DefaultAction;
      break;
    case StandardRevokeReason.NOT_CERTIFIED:
      errorMessage = RevokeReasonMessages.NotCertified;
      break;
    case BidOnNichesRevokeReason.LOW_REPUTATION:
    case SuggestNichesRevokeReason.LOW_REPUTATION:
    case SubmitTribunalAppealsRevokeReason.LOW_REPUTATION:
      errorMessage = RevokeReasonMessages.LowReputation;
      break;
    default:
      // todo:error-handling: We need to report this error to the server, if we are not already.
      throw new Error('getPermissionsErrorMessage: permissions error type required');
  }

  if (attemptedAction) {
    attemptedAction = <FormattedMessage {...attemptedAction}/>;
  }

  return <FormattedMessage {...errorMessage} values={{ attemptedAction }}/>;
}

interface RevokeReasonCertificationMessage {
  link: FormattedMessage.MessageDescriptor;
  extra?: FormattedMessage.MessageDescriptor;
}

function getRevokeReasonCertificationLinkMessage (
  revokeReason: RevokeReasonType,
  currentUser?: User
): RevokeReasonCertificationMessage | undefined {
  if (!currentUser) {
    return;
  }

  // bl: if the user is certified, then we shouldn't include a Certification link. ever.
  const isCurrentUserCertified = currentUser.reputation && currentUser.reputation.kycVerifiedScore;
  if (isCurrentUserCertified) {
    return;
  }
  const isConductNegative = revokeReason === StandardRevokeReason.CONDUCT_NEGATIVE;
  const isLowRep =
    revokeReason === BidOnNichesRevokeReason.LOW_REPUTATION ||
    // jw: let's consider the Security Deposit Required as a low rep event, since getting out of low rep resolves it
    revokeReason === BidOnNichesRevokeReason.SECURITY_DEPOSIT_REQUIRED ||
    revokeReason === SuggestNichesRevokeReason.LOW_REPUTATION ||
    revokeReason === SubmitTribunalAppealsRevokeReason.LOW_REPUTATION;
  const shouldReturnCertificationLink =
    revokeReason === StandardRevokeReason.NOT_CERTIFIED ||
    isConductNegative ||
    isLowRep;

  if (!shouldReturnCertificationLink) {
    return;
  }

  const certificationLinkMessages: RevokeReasonCertificationMessage = {
    link: SharedComponentMessages.LearnAboutCertificationLink
  };

  if (isConductNegative) {
    certificationLinkMessages.extra = RevokeReasonMessages.ConductNegativeGetCertified;
  } else if (isLowRep) {
    certificationLinkMessages.extra = RevokeReasonMessages.LowReputationGetCertified;
  }

  return certificationLinkMessages;
}

export interface RevokeReasonProps {
  granted: boolean;
  errorMessage?: React.ReactNode;
  restorationMessage?: React.ReactNode;
}

export function getRevokeReasonProps(
  attemptedAction: FormattedMessage.MessageDescriptor,
  permission?: PermissionProps,
  currentUser?: User,
  timeoutMessage?: FormattedMessage.MessageDescriptor,
  certificationOnClickHandler?: () => void,
  certificationAnchorStyleProps?: LinkStyleProps
): RevokeReasonProps {
  // jw: not all permissions are guaranteed to be set, many of them are optional, so we will need to deconstruct
  //     each of these individually.
  const granted = !!(permission && permission.granted);
  const revokeReason = permission && permission.revokeReason;

  let errorMessage;
  let restorationMessage;

  if (!granted) {
    // jw: resolve the error message from the revokeReason and the attempted action.
    const revokeReasonMessage =
      revokeReason &&
      getRevokeReasonMessage(revokeReason, attemptedAction);
    const certificationLinkMessages =
      revokeReason &&
      getRevokeReasonCertificationLinkMessage(revokeReason, currentUser);

    // jw: let's reduce boilerplate by establishing the properties for the PermissionErrorModal here!
    if (timeoutMessage) {
      const restorationDatetime = permission && permission.restorationDatetime;
      if (restorationDatetime) {
        const restorationCountdown = <CountDown endTime={restorationDatetime} timeOnly={true}/>;
        restorationMessage = <FormattedMessage {...timeoutMessage} values={{ restorationCountdown }}/>;
      }
    }

    errorMessage = (
      <React.Fragment>
        {revokeReasonMessage}

        {!!certificationLinkMessages &&
        <React.Fragment>
          {certificationLinkMessages.extra &&
          <span>
            &nbsp;<FormattedMessage {...certificationLinkMessages.extra}/>
          </span>}

          &nbsp;
          <Link
            size="inherit"
            to={WebRoute.CertificationExplainer}
            onClick={certificationOnClickHandler}
            {...certificationAnchorStyleProps}
          >
            <FormattedMessage {...certificationLinkMessages.link}/>
          </Link>
        </React.Fragment>}
      </React.Fragment>
    );
  }

  return { granted, errorMessage, restorationMessage };
}

export function getSignInMessage(
  attemptedAction: FormattedMessage.MessageDescriptor,
  modalStoreActions: ModalActionProps
): React.ReactNode {
  const signIn = (
      <Link.Anchor onClick={() => modalStoreActions.updateModalVisibility(ModalName.login)}>
        <FormattedMessage {...RevokeReasonMessages.SignIn} />
      </Link.Anchor>
    );

  const performAction = <FormattedMessage {...attemptedAction} />;

  return <FormattedMessage {...RevokeReasonMessages.MustSignInToPerformAction} values={{signIn, performAction}} />;
}

/**
 * This function will centralize the process of creating a intro sentence that includes the score with an optional
 * followup sentence that prompts them to get certified if they are not already.
 *
 * @param baseMessage The leading message that should include the 'totalScore' variable
 * @param certMessage The followup message if the user is not certified that should include the `certLink` variable
 *                    that corresponds to the {certLinkType} specified
 * @param certLinkType The type of `Link.About` to use for the `certLink` in {certMessage}.
 * @param currentUser The current user, no message will be returned if this does not exist, or has no rep information
 */
export function getCertificationMessage(
  baseMessage: FormattedMessage.MessageDescriptor,
  certMessage: FormattedMessage.MessageDescriptor,
  certLinkType: 'certified' | 'certification',
  currentUser?: User
): React.ReactNode | null {
  if (!currentUser || !currentUser.reputation) {
    // jw:todo:error-handling: Should always be provided a current user with reputation!
    return null;
  }

  const { kycVerifiedScore, totalScore } = currentUser.reputation;

  let extraMessage;
  // jw: if the user does not have a verified score that means that they are not verified...
  if (!kycVerifiedScore) {
    const certLink = <Link.About type={certLinkType} />;

    extraMessage = (
      <React.Fragment>
        {' '}
        <FormattedMessage {...certMessage} values={{certLink}} />
      </React.Fragment>
    );
  }

  return (
    <React.Fragment>
      <FormattedMessage {...baseMessage} values={{totalScore}} />
      {extraMessage}
    </React.Fragment>
  );
}
