package org.narrative.network.shared.tasktypes;

import org.narrative.common.util.ValidationHandler;
import org.narrative.network.shared.processes.GlobalTaskProcess;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 16, 2005
 * Time: 8:38:25 PM
 */
public abstract class GlobalTaskImpl<T> extends NetworkTaskImpl<T> {

    protected GlobalTaskImpl() {}

    protected GlobalTaskImpl(boolean isForceWritable) {
        super(isForceWritable);
    }

    protected GlobalTaskImpl(ValidationHandler validationHandler) {
        super(validationHandler);
    }

    protected GlobalTaskProcess createProcess() {
        return new GlobalTaskProcess(getNetworkContext(), getMonitoredClassName());
    }

}
