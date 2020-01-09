import { NicheStatus } from '@narrative/shared';
import { EnumEnhancer } from '../utils/enhancedEnumUtils';
import { FormattedMessage } from 'react-intl';
import { TagColor } from '../components/Tag';
import { NicheGeneralMessages } from '../i18n/NicheGeneralMessages';

// jw: let's define the NicheStatusHelper that will provide all the extra helper logic for NicheStatuss
export class NicheStatusHelper {
  status: NicheStatus;
  message: FormattedMessage.MessageDescriptor;
  tagColor: TagColor;

  constructor(status: NicheStatus, message: FormattedMessage.MessageDescriptor, tagColor: TagColor) {
    this.status = status;
    this.message = message;
    this.tagColor = tagColor;
  }

  isActive() {
    return this.status === NicheStatus.ACTIVE;
  }

  isRejected() {
    return this.status === NicheStatus.REJECTED;
  }
}

// jw: next: lets create the lookup of NicheStatus to helper object

const statusHelpers: {[key: number]: NicheStatusHelper} = [];
// jw: make sure to register these in the order you want them to display. (though right now we never iterate over them)
statusHelpers[NicheStatus.ACTIVE] = new NicheStatusHelper(
  NicheStatus.ACTIVE,
  NicheGeneralMessages.Active,
  'green'
);
statusHelpers[NicheStatus.SUGGESTED] = new NicheStatusHelper(
  NicheStatus.SUGGESTED,
  NicheGeneralMessages.Suggested,
  'default'
);
statusHelpers[NicheStatus.FOR_SALE] = new NicheStatusHelper(
  NicheStatus.FOR_SALE,
  NicheGeneralMessages.UpForAuction,
  'beige'

);
statusHelpers[NicheStatus.REJECTED] = new NicheStatusHelper(
  NicheStatus.REJECTED,
  NicheGeneralMessages.Rejected,
  'red'
);
statusHelpers[NicheStatus.PENDING_PAYMENT] = new NicheStatusHelper(
  NicheStatus.PENDING_PAYMENT,
  NicheGeneralMessages.PendingPayment,
  'beige'
);

// jw: finally, let's create the enhancer, which will allow us to lookup helpers by enum instance.
export const EnhancedNicheStatus = new EnumEnhancer<NicheStatus, NicheStatusHelper>(
  statusHelpers
);
