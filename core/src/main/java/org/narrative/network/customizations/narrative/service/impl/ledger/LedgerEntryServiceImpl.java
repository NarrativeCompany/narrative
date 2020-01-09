package org.narrative.network.customizations.narrative.service.impl.ledger;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.CoreUtils;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.channels.ChannelConsumer;
import org.narrative.network.customizations.narrative.channels.FollowedChannel;
import org.narrative.network.customizations.narrative.controller.LedgerEntryController;
import org.narrative.network.customizations.narrative.elections.Election;
import org.narrative.network.customizations.narrative.elections.ElectionNominee;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.service.api.AreaTaskExecutor;
import org.narrative.network.customizations.narrative.service.api.LedgerEntryService;
import org.narrative.network.customizations.narrative.service.api.model.LedgerEntriesDTO;
import org.narrative.network.customizations.narrative.service.api.model.LedgerEntryDTO;
import org.narrative.network.customizations.narrative.service.api.model.LedgerEntryScrollParamsDTO;
import org.narrative.network.customizations.narrative.service.impl.StaticMethodWrapper;
import org.narrative.network.customizations.narrative.service.mapper.LedgerEntryMapper;
import org.narrative.network.customizations.narrative.util.LedgerEntryScrollable;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 8/10/18
 * Time: 10:57 AM
 *
 * @author brian
 */

@Service
@Transactional
public class LedgerEntryServiceImpl implements LedgerEntryService {
    private final AreaTaskExecutor areaTaskExecutor;
    private final LedgerEntryMapper ledgerEntryMapper;
    private final StaticMethodWrapper staticMethodWrapper;
    private final NarrativeProperties narrativeProperties;

    public LedgerEntryServiceImpl(AreaTaskExecutor areaTaskExecutor, LedgerEntryMapper ledgerEntryMapper, StaticMethodWrapper staticMethodWrapper, NarrativeProperties narrativeProperties) {
        this.areaTaskExecutor = areaTaskExecutor;
        this.ledgerEntryMapper = ledgerEntryMapper;
        this.staticMethodWrapper = staticMethodWrapper;
        this.narrativeProperties = narrativeProperties;
    }

    /**
     * Find a niche ledger entry by its OID.
     *
     * @param ledgerEntryOid {@link OID} specifying the ledger entry OID for the search
     * @return {@link LedgerEntryDTO} found, null otherwise
     */
    @Override
    public LedgerEntryDTO findLedgerEntryByOid(OID ledgerEntryOid) {
        LedgerEntry ledgerEntry = LedgerEntry.dao().get(ledgerEntryOid);
        if (exists(ledgerEntry)) {
            return ledgerEntryMapper.mapLedgerEntryEntityToLedgerEntry(ledgerEntry);
        }
        return null;
    }

    @Override
    public LedgerEntriesDTO findLedgerEntriesForChannel(OID channelOid, LedgerEntryScrollable scrollable) {
        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<LedgerEntriesDTO>(false) {
            @Override
            protected LedgerEntriesDTO doMonitoredTask() {
                // jw: let's ensure that we have a Niche by the specified ID
                Channel channel = Channel.dao().getForApiParam(channelOid, LedgerEntryController.CHANNEL_OID_PARAM);

                int count = scrollable.getResolvedCount(narrativeProperties);
                List<LedgerEntry> entryList = LedgerEntry.dao().getEntriesForChannelBefore(
                        channel,
                        channel.getType().getLedgerEntryTypes(),
                        getBefore(scrollable),
                        count
                );

                return getLedgerEntriesDtoFromResults(entryList, count);
            }
        });
    }

    @Override
    public LedgerEntriesDTO findLedgerEntriesForUser(OID userOid, Set<LedgerEntryType> ledgerEntryTypes, LedgerEntryScrollable scrollable) {
        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<LedgerEntriesDTO>(false) {
            @Override
            protected LedgerEntriesDTO doMonitoredTask() {
                AreaUser areaUser = AreaUser.dao().getAreaUserFromUserAndArea(userOid, getAreaContext().getArea().getOid());

                if (!exists(areaUser)) {
                    return LedgerEntriesDTO.builder()
                            .items(Collections.emptyList())
                            .hasMoreItems(false)
                            .scrollParams(null)
                            .build();
                }

                int count = scrollable.getResolvedCount(narrativeProperties);
                List<LedgerEntry> entryList = LedgerEntry.dao().getEntriesForAreaUserRlmByOid(
                        areaUser.getOid(),
                        ledgerEntryTypes,
                        getBefore(scrollable),
                        count
                );
                return getLedgerEntriesDtoFromResults(entryList, count);
            }
        });
    }

    private static Instant getBefore(LedgerEntryScrollable scrollable) {
        if (scrollable.getScrollParams()==null) {
            return null;
        }

        return scrollable.getScrollParams().getLastItemDatetime();
    }

    private LedgerEntriesDTO getLedgerEntriesDtoFromResults(List<LedgerEntry> entryList, int count) {
        // pre-populate the watched channel data
        Set<Niche> niches = entryList.stream()
                .filter(entry -> entry.getNicheResolved() != null)
                .map(LedgerEntry::getNicheResolved)
                .collect(Collectors.toSet());
        Set<Publication> publications = entryList.stream()
                .filter(entry -> entry.getPublicationResolved() != null)
                .map(LedgerEntry::getPublicationResolved)
                .collect(Collectors.toSet());
        Set<ChannelConsumer> postChannelConsumers = entryList.stream()
                .filter(entry -> entry.getContent() != null)
                .map(entry -> entry.getContent().getPublishedToChannels())
                .flatMap(Collection::stream)
                .map(c -> (ChannelConsumer)c.getConsumer())
                .collect(Collectors.toSet());

        Set<ChannelConsumer> allConsumers = new HashSet<>();
        allConsumers.addAll(niches);
        allConsumers.addAll(publications);
        allConsumers.addAll(postChannelConsumers);

        FollowedChannel.dao().populateChannelConsumersFollowedByCurrentUserField(staticMethodWrapper.networkContext().getUser(), allConsumers);

        // pre-populate election nominee counts
        List<Election> elections = entryList.stream().map(LedgerEntry::getElection).filter(CoreUtils::exists).collect(Collectors.toList());
        ElectionNominee.dao().populateElectionNomineeCounts(elections);

        // Map the entity results into niche DTOs
        List<LedgerEntryDTO> entries = ledgerEntryMapper.mapLedgerEntryEntityListToLedgerEntryList(entryList);

        boolean hasMoreItems = entries.size() == count;

        Instant lastItemDatetime = hasMoreItems
                ? entryList.get(entryList.size()-1).getEventDatetime()
                : null;

        LedgerEntryScrollParamsDTO scrollParams = lastItemDatetime != null
                ? LedgerEntryScrollParamsDTO.builder().lastItemDatetime(lastItemDatetime).build()
                : null;

        return LedgerEntriesDTO.builder()
                .items(entries)
                .hasMoreItems(hasMoreItems)
                .scrollParams(scrollParams)
                .build();
    }
}
