package org.narrative.network.customizations.narrative.controller;

import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.customizations.narrative.service.api.model.KycPricingDTO;
import org.narrative.network.customizations.narrative.service.mapper.UserKycMapper;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * Date: 2019-02-11
 * Time: 10:43
 *
 * @author jonmark
 */
@RestController
@RequestMapping("/kyc")
@Validated
public class KycController {
    private final NarrativeProperties narrativeProperties;
    private final UserKycMapper userKycMapper;

    public KycController(NarrativeProperties narrativeProperties, UserKycMapper userKycMapper) {
        this.narrativeProperties = narrativeProperties;
        this.userKycMapper = userKycMapper;
    }

    @GetMapping("/pricing")
    public KycPricingDTO getKycPricing(HttpServletRequest request) {

        return userKycMapper.mapKycApiConfigToKycPricingDTO(
                narrativeProperties.getPayPal().getKycPayments()
        );
    }
}
