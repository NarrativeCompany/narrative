package org.narrative.network.customizations.narrative.service.mapper;

import com.google.common.collect.Lists;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.core.user.UserKyc;
import org.narrative.network.core.user.UserKycEvent;
import org.narrative.network.core.user.UserKycEventType;
import org.narrative.network.customizations.narrative.UsdValue;
import org.narrative.network.customizations.narrative.service.api.model.KycPricingDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserKycDTO;
import org.narrative.network.customizations.narrative.service.api.model.kyc.UserKycStatus;
import org.narrative.network.customizations.narrative.service.mapper.util.ServiceMapperConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Mapper(config = ServiceMapperConfig.class)
public abstract class UserKycMapper {

    @Autowired
    private NarrativeProperties narrativeProperties;
    @Autowired
    private InvoiceMapper invoiceMapper;

    /**
     * Map from {@link UserKyc} entity to {@link UserKycDTO}.
     *
     * @param userKyc The incoming user KYC entity to map
     * @return The mapped user KYC DTO
     */
    public UserKycDTO mapUserKycEntityToUserKycDTO(UserKyc userKyc) {
        UserKycDTO.UserKycDTOBuilder userKycDTOBuilder = UserKycDTO.builder()
                .oid(userKyc.getOid())
                .kycStatus(userKyc.getKycStatus());

        // If the KYC status is rejected, findi the most recent rejected event
        boolean rejectionEventNotFound = false;
        if (UserKycStatus.REJECTED.equals(userKyc.getKycStatus())) {
            if (CollectionUtils.isNotEmpty(userKyc.getEvents())) {
                UserKycEventType lastRejectedEventType =
                        Lists.reverse(userKyc.getEvents()).stream()
                        .filter(event -> {
                            UserKycStatus status = event.getType().getSendEmailForStatus();
                            return status!=null && status.isRejected();
                        })
                        .findFirst()
                        .map(UserKycEvent::getType)
                        .orElse(null);
                if (lastRejectedEventType != null) {
                    userKycDTOBuilder.rejectedReasonEventType(lastRejectedEventType);
                } else {
                    rejectionEventNotFound = true;
                }
            } else {
                rejectionEventNotFound = true;
            }
        }

        if (userKyc.getKycStatus().isStartCheckEligible() && userKyc.getInvoiceType()!=null) {
            // jw: let's skip this if the context is null since that means we are running for a test.
            userKycDTOBuilder.payPalCheckoutDetails(invoiceMapper.mapPayPalCheckoutDetailToDTO(
                    userKyc.getInvoiceType().getImmediatePaymentPayPalCheckoutDetails(userKyc.getUser(), narrativeProperties)
            )).kycPricing(mapKycApiConfigToKycPricingDTO(narrativeProperties.getPayPal().getKycPayments()));
        }

        if (rejectionEventNotFound) {
            log.error("No UserKycEvent found for rejected UserKyc - this should never happen.  OID: " + userKyc.getOid());
        }

        return userKycDTOBuilder.build();
    }

    public KycPricingDTO mapKycApiConfigToKycPricingDTO(NarrativeProperties.PayPal.KycApiConfig cfg) {
        return KycPricingDTO.builder()
                .initialPrice(UsdValue.valueOf(cfg.getInitialPrice()))
                .retryPrice(UsdValue.valueOf(cfg.getRetryPrice()))
                .kycPromoPrice(UsdValue.valueOf(cfg.getKycPromoPrice()))
                .kycPromoMessage(cfg.getKycPromoMessage())
                .build();
    }
}
