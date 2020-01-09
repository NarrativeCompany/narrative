package org.narrative.network.shared.tasktypes;

import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.shared.context.AreaContext;
import org.narrative.network.shared.context.AreaContextHolder;
import org.narrative.network.shared.processes.AreaTaskProcess;

import java.util.function.Consumer;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 16, 2005
 * Time: 8:38:25 PM
 */
public abstract class AreaTaskImpl<T> extends NetworkTaskImpl<T> implements AreaContextHolder {

    private AreaContext areaContext;

    protected AreaTaskImpl() {}

    protected AreaTaskImpl(boolean isForceWritable) {
        super(isForceWritable);
    }

    public AreaTaskImpl(boolean isForceWritable, Consumer<ValidationContext> validationFunction) {
        super(isForceWritable, validationFunction);
    }

    public AreaContext getAreaContext() {
        return areaContext;
    }

    /**
     * bl: not using a standard bean setter so that this value will never be able to be set
     * via Struts (as would be the case when an AreaTaskImpl is exposed with SubPropertySettable.
     *
     * @param areaContext the areaContext to use for this AreaTaskImpl
     */
    public void doSetAreaContext(AreaContext areaContext) {
        this.areaContext = areaContext;
    }

    protected AreaTaskProcess createProcess() {
        return new AreaTaskProcess(areaContext, getNetworkContext(), getMonitoredClassName());
    }
}
