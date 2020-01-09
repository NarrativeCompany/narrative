import { defineMessages } from 'react-intl';

export const MemberCertificationMessages = defineMessages({
  PageHeaderDescription: {
    id: 'memberCertification.pageHeaderDescription',
    defaultMessage: 'While you never have to reveal your true name or identity on Narrative, proving that you' +
      ' are a unique person is very important for our self-governing system. {learnMore}.'
  },
  PageHeaderDescriptionLink: {
    id: 'memberCertification.pageHeaderDescriptionLink',
    defaultMessage: 'Read all about the benefits of Certification here'
  },
  CertificationStepsSectionHeaderTitle: {
    id: 'memberCertification.certificationStepsSectionTitle',
    defaultMessage: 'There are two steps to getting Certified'
  },
  CertificationStepsSectionHeaderStepOne: {
    id: 'memberCertification.certificationStepsSectionHeaderStepOne',
    defaultMessage: 'Pay the application fee.'
  },
  CertificationStepsSectionHeaderStepTwo: {
    id: 'memberCertification.certificationStepsSectionHeaderStepTwo',
    defaultMessage: 'Submit documentation/images that prove you are a unique person in Narrative.' +
      ' You will need to submit a government-issued photo ID and a selfie. Narrative will not retain your' +
      ' documents any longer than necessary to process your application (see our {privacyPolicyLink}).' +
      ' The only information that will be retained from the submitted document is your month/year of birth and country.'
  },
  CertificationStepOneTitle: {
    id: 'memberCertification.certificationStepOneTitle',
    defaultMessage: 'Step 1: Pay Non-Refundable {price} {feeType} Fee'
  },
  ApplicationFee: {
    id: 'memberCertification.applicationFee',
    defaultMessage: 'Application'
  },
  ResubmissionFee: {
    id: 'memberCertification.resubmissionFee',
    defaultMessage: 'Resubmission'
  },
  CertificationStepOneRejectedDescription: {
    id: 'memberCertification.certificationStepOneRejectedDescription',
    defaultMessage: 'There is a $5 fee for re-submitting your Certification application.'
  },
  CertificationStepTwoTitle: {
    id: 'memberCertification.certificationStepTwoTitle',
    defaultMessage: 'Step 2: Submit Documentation'
  },
  CertificationStepTwoDescription: {
    id: 'memberCertification.certificationStepTwoDescription',
    defaultMessage: `You will need to provide government-issued photo ID (passport, driver's license, etc.).`
  },
  CertificationStepTwoMustHaveDateOfBirth: {
    id: 'memberCertification.certificationStepTwoMustHaveDateOfBirth',
    defaultMessage: `must have a date of birth`
  },
  CertificationStepTwoDateOfBirthRequired: {
    id: 'memberCertification.certificationStepTwoDateOfBirthRequired',
    defaultMessage: `Your government-issued photo ID {mustHaveDateOfBirth}.`
  },
  CertificationStepYearsOfAgeUS: {
    id: 'memberCertification.certificationStepYearsOfAgeUS',
    defaultMessage: `13 years of age`
  },
  CertificationStepTwoDescriptionExtraUS: {
    id: 'memberCertification.certificationStepTwoDescriptionExtraUS',
    defaultMessage: `If you live in the United States, you must be at least {yearsOfAge}.`
  },
  CertificationStepYearsOfAgeNonUS: {
    id: 'memberCertification.certificationStepYearsOfAgeNonUS',
    defaultMessage: `16 years of age`
  },
  CertificationStepTwoDescriptionExtraNonUS: {
    id: 'memberCertification.certificationStepTwoDescriptionExtraNonUS',
    defaultMessage: `If you live outside of the United States, you must be at least {yearsOfAge}.`
  },
  CertificationFAQLink: {
    id: 'memberCertification.certificationFAQLink',
    defaultMessage: `FAQ`
  },
  CertificationFAQDetails: {
    id: 'memberCertification.CertificationFAQDetails',
    defaultMessage: `Before submitting your documents, please review the Certification {faqLink}.`
  },
  ProceedToDocumentSubmission: {
    id: 'memberCertification.proceedToDocumentSubmission',
    defaultMessage: 'Proceed to Document Submission'
  },
  CertificationApprovedDescription: {
    id: 'memberCertification.certificationApprovedDescription',
    defaultMessage: 'Your Account Is Certified'
  },
  CertificationsDisabledDescription: {
    id: 'memberCertification.certificationsDisabledDescription',
    defaultMessage: 'Certifications Have Been Disabled'
  },
  CertificationsDisabledReason: {
    id: 'memberCertification.certificationsDisabledReason',
    defaultMessage: 'Narrative is no longer accepting new Certification requests.'
  },
  CertificationRejectedDescription: {
    id: 'memberCertification.certificationRejectedDescription',
    defaultMessage: 'Your Previous Certification Application Was Rejected'
  },
  CertificationRejectedReason: {
    id: 'memberCertification.certificationRejectedReason',
    defaultMessage: 'Your submission was rejected {reason}. '
  },
  CertificationRejectedInfo: {
    id: 'memberCertification.certificationRejectedInfo',
    defaultMessage: 'You may re-apply for Certification any time. A $5 fee' +
      ' will apply, when re-submitting an application.'
  },
  CertificationPendingDescription: {
    id: 'memberCertification.certificationPendingDescription',
    defaultMessage: 'Your Certification Application Is Under Review'
  },
  CertificationPendingInfo: {
    id: 'memberCertification.certificationPendingInfo',
    defaultMessage: 'You will be notified when your application has been processed.'
  },
  CertificationRevokedDescription: {
    id: 'memberCertification.certificationRevokedDescription',
    defaultMessage: 'You are not currently eligible for Certification'
  },
  CertificationRevokedInfo: {
    id: 'memberCertification.certificationRevokedInfo',
    defaultMessage: 'Because you previously requested and were issued a chargeback for a previous payment you' +
      ' made to Narrative, you are not currently permitted to pay by credit card, PayPal, or Venmo.' +
      ' The Certification process requires payment by one of those methods, and thus you are not currently' +
      ' permitted to become Certified.'
  },
  KycStatusNone: {
    id: 'memberCertification.kycStatusNone',
    defaultMessage: 'Uncertified'
  },
  KycStatusReadyForVerification: {
    id: 'memberCertification.readyForVerification',
    defaultMessage: 'Incomplete'
  },
  KycStatusPending: {
    id: 'memberCertification.pending',
    defaultMessage: 'Pending'
  },
  KycStatusApproved: {
    id: 'memberCertification.approved',
    defaultMessage: 'Certified'
  },
  KycStatusRejected: {
    id: 'memberCertification.rejected',
    defaultMessage: 'Rejected'
  },
  KycStatusRevoked: {
    id: 'memberCertification.revoked',
    defaultMessage: 'Revoked'
  },
  DueToUserInfoMissingFromDocument: {
    id: 'userKycEventType.dueToUserInfoMissingFromDocument',
    defaultMessage: 'due to incomplete or missing information in document'
  },
  DueToSelfieNotValid: {
    id: 'userKycEventType.dueToSelfieNotValid',
    defaultMessage: 'due to an invalid selfie'
  },
  DueToRejectedDuplicate: {
    id: 'userKycEventType.dueToRejectedDuplicate',
    defaultMessage: 'due to a document having been used before'
  },
  DueToDocumentInvalid: {
    id: 'userKycEventType.dueToDocumentInvalid',
    defaultMessage: 'due to an invalid document'
  },
  DueToDocumentSuspicious: {
    id: 'userKycEventType.dueToDocumentSuspicious',
    defaultMessage: 'due to a suspicious or inauthentic document'
  },
  DueToSelfiePaperMissing: {
    id: 'userKycEventType.dueToSelfiePaperMissing',
    defaultMessage: 'due to missing "Narrative Certification" paper in selfie photo'
  },
  DueToSelfieLowQuality: {
    id: 'userKycEventType.dueToSelfieLowQuality',
    defaultMessage: 'due to a low quality selfie'
  },
  DueToSelfieMismatch: {
    id: 'userKycEventType.dueToSelfieMismatch',
    defaultMessage: 'due to a photo mismatch between the selfie and the document'
  },
  DueToUserUnderage: {
    id: 'userKycEventType.dueToUserUnderage',
    defaultMessage: 'due to the individual not meeting the minimum age requirement'
  },
  CertificationPayment: {
    id: 'memberCertificationPayment.certificationPayment',
    defaultMessage: 'Narrative Certification'
  },
  CertificationRetryPayment: {
    id: 'memberCertificationPayment.certificationRetryPayment',
    defaultMessage: 'Narrative Certification Resubmission'
  },
});
