package org.narrative.network.core.cluster.actions;

import lombok.experimental.FieldNameConstants;
import org.narrative.common.core.services.interceptors.SubPropertySettable;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.web.Birthday;
import org.narrative.common.web.HttpRequestType;
import org.narrative.common.web.struts.MethodDetails;
import org.narrative.config.StaticConfig;
import org.narrative.network.core.locations.Country;
import org.narrative.network.core.user.UserKyc;
import org.narrative.network.customizations.narrative.service.api.KycService;
import org.narrative.network.customizations.narrative.service.impl.kyc.DocCheckUserProps;
import org.narrative.network.shared.authentication.ClusterUserSession;
import org.narrative.network.shared.struts.NetworkResponses;

import static org.narrative.common.util.CoreUtils.*;

@FieldNameConstants
public class UpdateUserKycMetadataAction extends ClusterAction {
    public static final String ACTION_NAME = "update-user-kyc-metadata";
    public static final String FULL_ACTION_PATH = "/" + ACTION_NAME;

    private UserKyc userKyc;
    private String firstName;
    private String lastName;
    private Country country;
    private String documentNumber;
    private Birthday dateOfBirth;

    @Override
    public void validate() {
        UserKyc userKyc = getUserKyc();
        if (!exists(userKyc)) {
            throw UnexpectedError.getRuntimeException("UserKyc should never be null!");
        }
        if (!userKyc.getKycStatus().isAwaitingMetadata()) {
            throw UnexpectedError.getRuntimeException("UserKyc is not eligible for a metadata update " + userKyc.getOid());
        }
        if(isEmpty(firstName)) {
            addRequiredFieldError("firstName", "First Name");
        }
        if(isEmpty(lastName)) {
            addRequiredFieldError("lastName", "Last Name");
        }
        if(country==null) {
            addRequiredFieldError("country", "Country");
        }
        if(isEmpty(documentNumber)) {
            addRequiredFieldError("documentNumber", "Document Number");
        }
        if(dateOfBirth==null) {
            addRequiredFieldError("dateOfBirth", "Date of Birth");
        } else if (!dateOfBirth.isValid()) {
            addInvalidFieldError("dateOfBirth", "Date of Birth");
        }
    }

    @Override
    @MethodDetails(requestType = HttpRequestType.AJAX)
    public String execute() throws Exception {
        KycService kycService = StaticConfig.getBean(KycService.class);

        DocCheckUserProps docCheckUserProps = DocCheckUserProps.builder()
                .firstName(firstName)
                .lastName(lastName)
                .country(country)
                .documentNumber(documentNumber)
                .birthDate(dateOfBirth.toLocalDate())
                .build();

        kycService.updateKycData(
                userKyc.getUser(),
                docCheckUserProps,
                ClusterUserSession.getClusterUserSession().getClusterRole().getDisplayNameResolved()
        );

        return NetworkResponses.redirectResponse();
    }

    public UserKyc getUserKyc() {
        return userKyc;
    }

    public void setUserKyc(UserKyc userKyc) {
        this.userKyc = userKyc;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    @SubPropertySettable
    public Birthday getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Birthday dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
