package org.narrative.network.shared.tasktypes;

import org.narrative.common.util.ValidationHandler;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.shared.context.NetworkContext;
import org.narrative.network.shared.context.NetworkContextHolder;

import java.util.function.Consumer;

/**
 * NetworkTaskImpl is a utility base class that can be used to execute
 * network-specific actions.  NetworkTaskImpl implements both Task for task
 * execution and Invocation so that it can handle NetworkInterceptor lists.
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 16, 2005
 * Time: 9:08:51 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class NetworkTaskImpl<T> extends PartitionTask<T> implements NetworkContextHolder {

    private NetworkContext networkContext;

    protected NetworkTaskImpl() {
        this(true);
    }

    protected NetworkTaskImpl(boolean isForceWritable, Consumer<ValidationContext> validationFunction) {
        super(isForceWritable, validationFunction);
    }

    protected NetworkTaskImpl(ValidationHandler validationHandler) {
        super(validationHandler);
    }

    protected NetworkTaskImpl(boolean isForceWritable) {
        super(isForceWritable);
    }

    public final NetworkTaskImpl<T> getAction() {
        return this;
    }

    /**
     * bl: not using a standard bean setter so that this value will never be able to be set
     * via Struts (as would be the case when a NetworkTaskImpl is exposed with SubPropertySettable.
     *
     * @param networkContext the networkContext to use for this NetworkTaskImpl
     */
    public void doSetNetworkContext(NetworkContext networkContext) {
        this.networkContext = networkContext;
    }

    public NetworkContext getNetworkContext() {
        return networkContext;
    }

}
