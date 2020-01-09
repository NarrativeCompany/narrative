import { PublicationStatus } from '@narrative/shared';
import { EnumEnhancer } from '../utils/enhancedEnumUtils';

// jw: let's define the PublicationStatusHelper that will provide all the extra helper logic for PublicationStatuss
export class PublicationStatusHelper {
  status: PublicationStatus;

  constructor(
    status: PublicationStatus,
  ) {
    this.status = status;
  }

  isActive(): boolean {
    return this.status === PublicationStatus.ACTIVE;
  }

  isExpired(): boolean {
    return this.status === PublicationStatus.EXPIRED;
  }
}

// jw: next: lets create the lookup of PublicationStatus to helper object

const statusHelpers: {[key: number]: PublicationStatusHelper} = [];
// jw: make sure to register these in the order you want them to display.
statusHelpers[PublicationStatus.ACTIVE] = new PublicationStatusHelper(
  PublicationStatus.ACTIVE
);
statusHelpers[PublicationStatus.EXPIRED] = new PublicationStatusHelper(
  PublicationStatus.EXPIRED
);

// jw: finally, let's create the enhancer, which will allow us to lookup helpers by enum instance.
export const EnhancedPublicationStatus = new EnumEnhancer<PublicationStatus, PublicationStatusHelper>(
  statusHelpers
);
