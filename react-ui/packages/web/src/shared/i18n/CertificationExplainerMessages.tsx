import { defineMessages } from 'react-intl';

export const CertificationExplainerMessages = defineMessages({
  SeoTitle: {
    id: 'CertificationExplainer.seoTitle',
    defaultMessage: 'What is Certification?'
  },
  SeoDescription: {
    id: 'CertificationExplainer.seoDescription',
    defaultMessage: 'Narrative is intended to be a bustling hive of creative humans, not a home for wayward bots.'
  },
  PageHeaderTitle: {
    id: 'CertificationExplainer.pageHeaderTitle',
    defaultMessage: 'Certification'
  },
  PageHeaderDescription: {
    id: 'CertificationExplainer.pageHeaderDescription',
    defaultMessage: 'aka Congrats, You’re a Unique Human!'
  },
  Description: {
    id: 'CertificationExplainerDetails.pageHeaderDescription',
    defaultMessage: 'Narrative is intended to be a bustling hive of creative humans, not a home for wayward bots.' +
      ' So we’ve come up with a way to encourage members to prove their humanity (no blood sample required).' +
      ' Certification is an optional process that establishes your individual identity (behind the scenes) and' +
      ' opens up additional benefits in Narrative. It does not force you to expose your real name on Narrative.' +
      ' No worries, you can still be @{username}.'
  },
  UsernamePlaceholder: {
    id: 'CertificationExplainerDetails.usernamePlaceholder',
    defaultMessage: '[username]'
  },
  IsCertificationRequired: {
    id: 'CertificationExplainerDetails.isCertificationRequired',
    defaultMessage: 'Do I have to get {titleHighlight}?'
  },
  IsCertificationRequiredDescription: {
    id: 'CertificationExplainerDetails.isCertificationRequiredDescription',
    defaultMessage: 'Definitely not! There’s a lot you can do in Narrative without Certification (including' +
      ' read, write, vote).'
  },
  WhyGetCertified: {
    id: 'CertificationExplainerDetails.whyGetCertified',
    defaultMessage: 'Why would I want to get {titleHighlight}?'
  },
  WhyGetCertifiedDescription: {
    id: 'CertificationExplainerDetails.whyGetCertifiedDescription',
    defaultMessage: 'If you go through the Certification process (it’s painless, we promise), you can:'
  },
  WhyGetCertifiedPoint1: {
    id: 'CertificationExplainerDetails.whyGetCertifiedPoint1',
    defaultMessage: 'Boost your reputation score (Certification is 30% of your total score). The very highest rep' +
      ' members get an activity rewards bonus!'
  },
  WhyGetCertifiedPoint2: {
    id: 'CertificationExplainerDetails.whyGetCertifiedPoint2',
    defaultMessage: 'View age-restricted content if you are 18+'
  },
  WhyGetCertifiedPoint3: {
    id: 'CertificationExplainerDetails.whyGetCertifiedPoint3',
    defaultMessage: 'Get a “Certified” status box on your profile'
  },
  TaxReportingExplainer: {
    id: 'CertificationExplainerDetails.taxReportingExplainer',
    defaultMessage: 'You will also need to be Certified if you request a redemption of your Reward Points that ' +
      'exceeds the equivalent value of US$600. This is to comply with tax laws, so we can determine if you are a ' +
      'United States citizen, for whom tax reporting may be required.'
  },
  HowMuchDoesCertificationCost: {
    id: 'CertificationExplainerDetails.howMuchDoesCertificationCost',
    defaultMessage: 'How much does Certification cost?'
  },
  HowMuchDoesCertificationCostDescription: {
    id: 'CertificationExplainerDetails.howMuchDoesCertificationCostDescription',
    defaultMessage: 'The cost is {initialPrice}. Because we’re working with a third-party vendor and handling' +
      ' the processing fees, we have to charge for this service, and the Certification fees will go to' +
      ' Narrative Company to defray those expenses.'
  },
  HowMuchDoesCertificationCostDescriptionWithPromo: {
    id: 'CertificationExplainerDetails.howMuchDoesCertificationCostDescriptionWithPromo',
    defaultMessage: 'The cost is {kycPromoPrice} for a limited time (until the public Beta launch, when it goes up' +
      ' to {initialPrice}). Because we’re working with a third-party vendor and handling the processing fees,' +
      ' we have to charge for this service, and the Certification fees will go to Narrative Company to defray' +
      ' those expenses.'
  },
  WhatInformationIsNeeded: {
    id: 'CertificationExplainerDetails.whatInformationIsNeeded',
    defaultMessage: 'What information do I need to provide?'
  },
  WhatInformationIsNeededDescription: {
    id: 'CertificationExplainerDetails.whatInformationIsNeededDescription',
    defaultMessage: 'You’ll need to submit a selfie photograph, that includes a piece of paper with the date and' +
      ' the words “Narrative Certification” on it. You’ll also need to submit a valid form of identification.'
  },
  HowAreaYouProcessing: {
    id: 'CertificationExplainerDetails.howAreaYouProcessing',
    defaultMessage: 'How are you processing and securing my information?'
  },
  HowAreaYouProcessingDescription: {
    id: 'CertificationExplainerDetails.howAreaYouProcessingDescription',
    defaultMessage: 'Our whole mantra is about trust and control over your own data. That’s why we’re only' +
      ' collecting what we need in order to ensure you’re a unique human being. So while you’ll be providing' +
      ' a selfie and your ID information, we’re only retaining your birth month/year, your country, and' +
      ' creating a unique ID number for you in our system (called a “hash”). We will not store your real name' +
      ' or your ID information at all in our servers, and we are only retaining the birth year in order to age-gate' +
      ' restricted content.'
  },
  GeeksOnlyDescription: {
    id: 'CertificationExplainerDetails.geeksOnlyDescription',
    defaultMessage: '{geeksOnly}: This type of ID verification is sometimes called “KYC” or “Know Your Customer”' +
      ' in banking circles. We are not performing the other half of that process, called “AML”' +
      ' or “Anti-Money Laundering.”'
  },
  CanIReapply: {
    id: 'CertificationExplainerDetails.canIReapply',
    defaultMessage: 'Can I re-apply if I’m rejected?'
  },
  CanIReapplyDescription: {
    id: 'CertificationExplainerDetails.canIReapplyDescription',
    defaultMessage: 'Yes, you can; however you will need to pay a {retryPrice} re-submission fee, since we’re' +
      ' incurring costs for every application submitted.'
  },
  MoreThanOneAccount: {
    id: 'CertificationExplainerDetails.moreThanOneAccount',
    defaultMessage: 'I have more than one Narrative account; can I certify more than one?'
  },
  MoreThanOneAccountDescription: {
    id: 'CertificationExplainerDetails.moreThanOneAccountDescription',
    defaultMessage: 'Yes, but you must use a unique document each time you get Certified on a new account.'
  },
  GetCertifiedNow: {
    id: 'getCertifiedCTA.getCertifiedNow',
    defaultMessage: 'Get Certified Now'
  },
});
