import { UserRedemptionStatus } from '@narrative/shared';
import { EnumEnhancer } from '../utils/enhancedEnumUtils';

/**
 * jw: This enum is closely tied to the java version of this enum, particularly since it duplicates the walletUpdatable
 *     and supportsRedemption flags. It's vital that this is kept in sync with the Java version.
 */

// jw: let's define the QualityFilterHelper that will provide all the extra helper logic for QualityFilters
export class UserRedemptionStatusHelper {
  status: UserRedemptionStatus;
  walletUpdatable: boolean;
  supportsRedemption: boolean;

  constructor(
    status: UserRedemptionStatus,
    walletUpdatable: boolean,
    supportsRedemption: boolean
  ) {
    this.status = status;
    this.walletUpdatable = walletUpdatable;
    this.supportsRedemption = supportsRedemption;
  }

  public isWalletUnspecified(): boolean {
    return this.status === UserRedemptionStatus.WALLET_UNSPECIFIED;
  }

  public isWalletInWaitingPeriod(): boolean {
    return this.status === UserRedemptionStatus.WALLET_IN_WAITING_PERIOD;
  }

  public isHasPendingRedemption(): boolean {
    return this.status === UserRedemptionStatus.HAS_PENDING_REDEMPTION;
  }

  public isRedemptionAvailable(): boolean {
    return this.status === UserRedemptionStatus.REDEMPTION_AVAILABLE;
  }
}

// jw: next: lets create the lookup of QualityFilter to helper object

const helpers: {[key: number]: UserRedemptionStatusHelper} = [];
helpers[UserRedemptionStatus.WALLET_UNSPECIFIED] = new UserRedemptionStatusHelper(
  UserRedemptionStatus.WALLET_UNSPECIFIED,
  true,
  false
);
helpers[UserRedemptionStatus.WALLET_IN_WAITING_PERIOD] = new UserRedemptionStatusHelper(
  UserRedemptionStatus.WALLET_IN_WAITING_PERIOD,
  true,
  false
);
helpers[UserRedemptionStatus.HAS_PENDING_REDEMPTION] = new UserRedemptionStatusHelper(
  UserRedemptionStatus.HAS_PENDING_REDEMPTION,
  false,
  false
);
helpers[UserRedemptionStatus.REDEMPTION_AVAILABLE] = new UserRedemptionStatusHelper(
  UserRedemptionStatus.REDEMPTION_AVAILABLE,
  true,
  true
);

// jw: finally, let's create the enhancer, which will allow us to lookup helpers by enum instance.
export const EnhancedUserRedemptionStatus = new EnumEnhancer<UserRedemptionStatus, UserRedemptionStatusHelper>(
  helpers
);
