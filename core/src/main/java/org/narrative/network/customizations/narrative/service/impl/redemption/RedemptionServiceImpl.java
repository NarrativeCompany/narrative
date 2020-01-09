package org.narrative.network.customizations.narrative.service.impl.redemption;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.narrative.wallet.Wallet;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.service.api.AreaTaskExecutor;
import org.narrative.network.customizations.narrative.service.api.RedemptionService;
import org.narrative.network.customizations.narrative.service.api.model.UserNeoWalletDTO;
import org.narrative.network.customizations.narrative.service.api.model.input.RequestRedemptionInput;
import org.narrative.network.customizations.narrative.service.api.model.input.UpdateProfileAccountConfirmationInputBase;
import org.narrative.network.customizations.narrative.service.api.model.input.UpdateUserNeoWalletInput;
import org.narrative.network.customizations.narrative.service.mapper.UserMapper;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.springframework.stereotype.Service;

/**
 * Date: 2019-06-26
 * Time: 13:13
 *
 * @author jonmark
 */
@Service
public class RedemptionServiceImpl implements RedemptionService {
    private final AreaTaskExecutor taskExecutor;
    private final UserMapper userMapper;

    public RedemptionServiceImpl(AreaTaskExecutor taskExecutor, UserMapper userMapper) {
        this.taskExecutor = taskExecutor;
        this.userMapper = userMapper;
    }

    @Override
    public UserNeoWalletDTO updateWalletNeoAddressForCurrentUser(UpdateUserNeoWalletInput input) {
        return taskExecutor.executeAreaTask(new AreaTaskImpl<UserNeoWalletDTO>() {
            @Override
            protected UserNeoWalletDTO doMonitoredTask() {
                getNetworkContext().getPrimaryRole().checkRegisteredUser();

                User user = getNetworkContext().getUser();
                getAreaContext().doAreaTask(new UpdateUserNeoWalletTask(user, input));

                return userMapper.mapUserToUserNeoWalletDTO(user);
            }
        });
    }

    @Override
    public UserNeoWalletDTO deleteWalletNeoAddressForCurrentUser(UpdateProfileAccountConfirmationInputBase input) {
        return taskExecutor.executeAreaTask(new AreaTaskImpl<UserNeoWalletDTO>() {
            @Override
            protected UserNeoWalletDTO doMonitoredTask() {
                getNetworkContext().getPrimaryRole().checkRegisteredUser();

                User user = getNetworkContext().getUser();
                getAreaContext().doAreaTask(new DeleteUserNeoWalletTask(user, input));

                return userMapper.mapUserToUserNeoWalletDTO(user);
            }
        });
    }

    @Override
    public UserNeoWalletDTO getWalletNeoAddressForCurrentUser() {
        return taskExecutor.executeAreaTask(new AreaTaskImpl<UserNeoWalletDTO>(false) {
            @Override
            protected UserNeoWalletDTO doMonitoredTask() {
                getNetworkContext().getPrimaryRole().checkRegisteredUser();

                return userMapper.mapUserToUserNeoWalletDTO(getNetworkContext().getUser());
            }
        });
    }

    @Override
    public void requestNeoWalletRedemptionForCurrentUser(RequestRedemptionInput input) {
        taskExecutor.executeAreaTask(new AreaTaskImpl<Object>(true) {
            @Override
            protected Object doMonitoredTask() {
                getNetworkContext().getPrimaryRole().checkRegisteredUser();

                getAreaContext().doAreaTask(new RequestRedemptionTask(
                        getNetworkContext().getUser(),
                        input
                ));

                return null;
            }
        });
    }

    @Override
    public void deleteNeoWalletRedemptionForCurrentUser(OID redemptionOid) {
        taskExecutor.executeAreaTask(new AreaTaskImpl<Object>(true) {
            @Override
            protected Object doMonitoredTask() {
                getNetworkContext().getPrimaryRole().checkRegisteredUser();

                Wallet wallet = getNetworkContext().getUser().getWallet();

                return getAreaContext().doAreaTask(new CancelRedemptionTask(wallet, redemptionOid));
            }
        });
    }
}
