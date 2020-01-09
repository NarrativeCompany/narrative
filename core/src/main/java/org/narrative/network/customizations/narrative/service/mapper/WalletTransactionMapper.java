package org.narrative.network.customizations.narrative.service.mapper;

import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.customizations.narrative.service.api.model.UserRewardTransactionDTO;
import org.narrative.network.customizations.narrative.service.mapper.util.ServiceMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Date: 2019-06-03
 * Time: 15:56
 *
 * @author brian
 */
@Mapper(config = ServiceMapperConfig.class, uses = {NicheMapper.class, UserMapper.class, PostMapper.class, NrveValueMapper.class})
public interface WalletTransactionMapper {
    /**
     * Map from {@link WalletTransaction} entity to {@link UserRewardTransactionDTO}.
     *
     * @param transaction The transaction to map
     * @return The mapped {@link UserRewardTransactionDTO}
     */
    UserRewardTransactionDTO mapWalletTransactionEntityToUserRewardTransactionDTO(WalletTransaction transaction);

    List<UserRewardTransactionDTO> mapWalletTransactionEntityListToUserRewardTransactionDTOList(List<WalletTransaction> transactions);
}
