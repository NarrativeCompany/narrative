package org.narrative.reputation.service;

import org.narrative.shared.event.reputation.KYCVerificationEvent;

public interface KYCVerificationService {
    KYCVerificationEvent updateCurrentReputationWithKYCVerificationEvent (KYCVerificationEvent kycVerificationEvent);

}
