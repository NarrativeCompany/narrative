package org.narrative.network.customizations.narrative.service.impl.election;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.controller.ElectionController;
import org.narrative.network.customizations.narrative.elections.Election;
import org.narrative.network.customizations.narrative.elections.ElectionNominee;
import org.narrative.network.customizations.narrative.elections.services.ConfirmElectionNomineeTask;
import org.narrative.network.customizations.narrative.elections.services.WithdrawFromElectionTask;
import org.narrative.network.customizations.narrative.permissions.NarrativePermissionType;
import org.narrative.network.customizations.narrative.service.api.AreaTaskExecutor;
import org.narrative.network.customizations.narrative.service.api.ElectionService;
import org.narrative.network.customizations.narrative.service.api.model.ElectionDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.ElectionNomineeDTO;
import org.narrative.network.customizations.narrative.service.api.model.ElectionNomineesDTO;
import org.narrative.network.customizations.narrative.service.mapper.ElectionMapper;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Date: 11/13/18
 * Time: 3:57 PM
 *
 * @author jonmark
 */
@Service
public class ElectionServiceImpl implements ElectionService {
    private final AreaTaskExecutor areaTaskExecutor;
    private final ElectionMapper electionMapper;

    public ElectionServiceImpl(AreaTaskExecutor areaTaskExecutor, ElectionMapper electionMapper) {
        this.areaTaskExecutor = areaTaskExecutor;
        this.electionMapper = electionMapper;
    }

    @Override
    public ElectionNomineesDTO findElectionNominees(OID electionOid, Instant confirmedBefore, int count) {
        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<ElectionNomineesDTO>(false) {
            @Override
            protected ElectionNomineesDTO doMonitoredTask() {
                Election election = Election.dao().getForApiParam(electionOid, ElectionController.ELECTION_OID_PARAM);

                // bl: exclude the current user from the list of nominees since the current user will always be
                // handled separately in ElectionDetailDTO.currentUserNominee
                List<ElectionNominee> nominees = ElectionNominee.dao().getConfirmedForElection(election, getNetworkContext().getUser(), confirmedBefore, count);
                List<ElectionNomineeDTO> nomineeDTOs = electionMapper.mapElectionNomineesToDtoList(nominees);

                boolean hasMoreItems = nominees.size() == count;
                Instant lastItemConfirmationDatetime = hasMoreItems
                        ? nominees.get(nominees.size() - 1).getNominationConfirmedDatetime()
                        : null;

                return ElectionNomineesDTO.builder()
                        .items(nomineeDTOs)
                        .hasMoreItems(hasMoreItems)
                        .lastItemConfirmationDatetime(lastItemConfirmationDatetime)
                        .build();
            }
        });
    }

    @Override
    public ElectionDetailDTO nominateCurrentUser(OID electionOid, String personalStatement) {
        ElectionNominee nominee = areaTaskExecutor.executeAreaTask(new AreaTaskImpl<ElectionNominee>() {
            @Override
            protected ElectionNominee doMonitoredTask() {
                getAreaContext().getAreaRole().checkNarrativeRight(NarrativePermissionType.NOMINATE_FOR_MODERATOR_ELECTIONS);
                Election election = Election.dao().getForApiParam(electionOid, ElectionController.ELECTION_OID_PARAM);

                return getAreaContext().doAreaTask(new ConfirmElectionNomineeTask(
                        election,
                        getNetworkContext().getUser(),
                        personalStatement
                ));
            }
        });

        return getElectionDetailDTOForCurrentUserNominee(nominee);
    }

    @Override
    public ElectionDetailDTO withdrawNominationForCurrentUser(OID electionOid) {
        ElectionNominee nominee = areaTaskExecutor.executeAreaTask(new AreaTaskImpl<ElectionNominee>() {
            @Override
            protected ElectionNominee doMonitoredTask() {
                getNetworkContext().getPrimaryRole().checkRegisteredUser();
                Election election = Election.dao().getForApiParam(electionOid, ElectionController.ELECTION_OID_PARAM);

                return getAreaContext().doAreaTask(new WithdrawFromElectionTask(
                        election,
                        getNetworkContext().getUser()
                ));
            }
        });

        return getElectionDetailDTOForCurrentUserNominee(nominee);
    }

    private ElectionDetailDTO getElectionDetailDTOForCurrentUserNominee(ElectionNominee currentUserNominee) {
        Election election = currentUserNominee.getElection();
        election.setCurrentUserNominee(currentUserNominee);
        return electionMapper.mapElectionToDetailDto(election);
    }
}
