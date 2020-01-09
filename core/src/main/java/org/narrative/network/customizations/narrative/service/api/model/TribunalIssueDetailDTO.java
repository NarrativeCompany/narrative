package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Date: 9/13/18
 * Time: 11:46 AM
 *
 * @author brian
 */
@JsonValueObject
@JsonTypeName("TribunalIssueDetail")
@Value
@Builder(toBuilder = true)
public class TribunalIssueDetailDTO {
    private final TribunalIssueDTO tribunalIssue;
    private final List<TribunalIssueReportDTO> tribunalIssueReports;
}
