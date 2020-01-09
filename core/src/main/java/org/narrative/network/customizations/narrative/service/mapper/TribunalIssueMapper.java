package org.narrative.network.customizations.narrative.service.mapper;

import org.narrative.network.customizations.narrative.niches.referendum.metadata.NicheDetailChangeReferendumMetadata;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssue;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssueReport;
import org.narrative.network.customizations.narrative.service.api.model.NicheEditDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.TribunalIssueDTO;
import org.narrative.network.customizations.narrative.service.api.model.TribunalIssueDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.TribunalIssueReportDTO;
import org.narrative.network.customizations.narrative.service.mapper.util.ServiceMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = ServiceMapperConfig.class, uses = {ReferendumMapper.class, UserMapper.class})
public interface TribunalIssueMapper {
    List<TribunalIssueDTO> mapTribunalIssueListToTribunalIssueDTOList(List<TribunalIssue> tribunalIssues);

    TribunalIssueDTO mapTribunalIssueToTribunalIssueDTO(TribunalIssue tribunalIssue);

    @Mapping(source = "tribunalIssue", target = "tribunalIssue")
    TribunalIssueDetailDTO mapTribunalIssueToTribunalIssueDetailDTO(TribunalIssue tribunalIssue);

    TribunalIssueReportDTO mapTribunalIssueReportToTribunalIssueReportDTO(TribunalIssueReport tribunalIssueReport);

    List<TribunalIssueReportDTO> mapTribunalIssueReportListToTribunalIssueReportDTOList(List<TribunalIssueReport> tribunalIssueReports);

    /**
     * Map from {@link NicheDetailChangeReferendumMetadata} to {@link NicheEditDetailDTO}
     *
     * @param metadata associated with a Tribunal Appeal to edit Niche name or description
     * @return mapped NicheEditDetailDTO
     */
    NicheEditDetailDTO mapNicheDetailChangeReferendumMetadataToNicheEditDetailDTO(NicheDetailChangeReferendumMetadata metadata);
}
