package org.narrative.network.customizations.narrative.service.mapper;

import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.metadata.PublicationPaymentLedgerEntryMetadata;
import org.narrative.network.customizations.narrative.service.api.model.LedgerEntryDTO;
import org.narrative.network.customizations.narrative.service.mapper.util.ServiceMapperConfig;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(config = ServiceMapperConfig.class, uses = {
        UserMapper.class,
        NicheMapper.class,
        PublicationMapper.class,
        NicheAuctionMapper.class,
        ReferendumMapper.class,
        TribunalIssueMapper.class,
        ElectionMapper.class,
        PostMapper.class
})
public abstract class LedgerEntryMapper {
    /**
     * Map from {@link LedgerEntry} entity to {@link LedgerEntryDTO}.
     *
     * @param ledgerEntryEntity The incoming ledger entry entity to map
     * @return The mapped ledger entry
     */
    @Mapping(source = "actor.areaUser.user", target = LedgerEntryDTO.Fields.actor)
    // jw: I canot explain it, but for whatever reason, if we try to use the same "issue" and "issueReport" names as
    //     what is on LedgerEntry on the DTO, then the fields will not be mapped. Brian S and I were stumped on this
    //     and cannot come up with an explanation.
    @Mapping(source = "issue", target = LedgerEntryDTO.Fields.tribunalIssue)
    @Mapping(source = "issueReport", target = LedgerEntryDTO.Fields.tribunalIssueReport)
    @Mapping(source = "nicheResolved", target = LedgerEntryDTO.Fields.niche)
    @Mapping(source = "publicationResolved", target = LedgerEntryDTO.Fields.publication)
    @Mapping(source = "content", target = LedgerEntryDTO.Fields.post)
    @Mapping(source = "contentOid", target = LedgerEntryDTO.Fields.postOid)
    @Mapping(ignore = true, target = LedgerEntryDTO.Fields.publicationPaymentType)
    @Mapping(ignore = true, target = LedgerEntryDTO.Fields.publicationPlan)
    public abstract LedgerEntryDTO mapLedgerEntryEntityToLedgerEntry(LedgerEntry ledgerEntryEntity);

    @AfterMapping
    void map(LedgerEntry entry, @MappingTarget LedgerEntryDTO.LedgerEntryDTOBuilder builder) {
        if (entry.getType().isPublicationPayment()) {
            PublicationPaymentLedgerEntryMetadata metadata = entry.getMetadata();
            assert metadata != null : "We should always have metadata for publication payment events!";

            builder.publicationPlan(metadata.getPlan());
            builder.publicationPaymentType(metadata.getPaymentType());
        }
    }

    /**
     * Map a {@link List} of {@link LedgerEntry} to a {@link List} of {@link LedgerEntryDTO}.
     *
     * @param ledgerEntryList The incoming list of ledger entry entities
     * @return The resulting list of ledger entries
     */
    public abstract List<LedgerEntryDTO> mapLedgerEntryEntityListToLedgerEntryList(List<LedgerEntry> ledgerEntryList);
}
