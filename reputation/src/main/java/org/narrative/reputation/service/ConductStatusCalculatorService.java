package org.narrative.reputation.service;

import org.narrative.shared.event.reputation.ConductStatusEvent;

public interface ConductStatusCalculatorService {
    ConductStatusEvent calculateNegativeConductExpirationDate (ConductStatusEvent conductStatusEvent);
}
