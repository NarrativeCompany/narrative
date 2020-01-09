import { PublicationPlanType } from '@narrative/shared';
import { EnumEnhancer } from '../utils/enhancedEnumUtils';
import { FormattedMessage } from 'react-intl';
import { PublicationDetailsMessages } from '../i18n/PublicationDetailsMessages';

// jw: let's define the PublicationPlanTypeHelper that will provide all the extra helper logic for PublicationPlanTypes
export class PublicationPlanTypeHelper {
  plan: PublicationPlanType;
  name: FormattedMessage.MessageDescriptor;
  maxEditors: number;
  maxWriters: number;
  supportsCustomDomain: boolean;
  annualFee: number;

  constructor(
    plan: PublicationPlanType,
    name: FormattedMessage.MessageDescriptor,
    maxEditors: number,
    maxWriters: number,
    supportsCustomDomain: boolean,
    annualFee: number
  ) {
    this.plan = plan;
    this.name = name;
    this.maxEditors = maxEditors;
    this.maxWriters = maxWriters;
    this.supportsCustomDomain = supportsCustomDomain;
    this.annualFee = annualFee;
  }

  isBasicPlan(): boolean {
    return this.plan === PublicationPlanType.BASIC;
  }

  isBusinessPlan(): boolean {
    return this.plan === PublicationPlanType.BUSINESS;
  }
}

// jw: next: lets create the lookup of PublicationPlanType to helper object

const helpers: {[key: number]: PublicationPlanTypeHelper} = [];
// jw: make sure to register these in the order you want them to display.
// zb: we are now iterating over these values, so order matters
helpers[PublicationPlanType.BASIC] = new PublicationPlanTypeHelper(
  PublicationPlanType.BASIC,
  PublicationDetailsMessages.BasicPlanName,
  2,
  5,
  false,
  125
);

helpers[PublicationPlanType.BUSINESS] = new PublicationPlanTypeHelper(
  PublicationPlanType.BUSINESS,
  PublicationDetailsMessages.BusinessPlanName,
  10,
  30,
  true,
  299
);

// jw: finally, let's create the enhancer, which will allow us to lookup helpers by enum instance.
export const EnhancedPublicationPlanType = new EnumEnhancer<PublicationPlanType, PublicationPlanTypeHelper>(
  helpers
);
