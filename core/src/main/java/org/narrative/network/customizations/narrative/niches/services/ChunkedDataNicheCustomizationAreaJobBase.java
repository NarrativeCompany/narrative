package org.narrative.network.customizations.narrative.niches.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.SubListIterator;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;

import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/21/18
 * Time: 8:05 AM
 */
public abstract class ChunkedDataNicheCustomizationAreaJobBase extends NicheCustomizationAreaJobBase {
    protected abstract NetworkLogger getLogger();

    protected abstract String getLoggingEntityName();

    protected abstract List<OID> getData();

    // jw: this method will be called from within the context of a root area task, isolated to this chunk of data
    protected abstract void processChunk(List<OID> chunk);

    protected boolean processDataIndividually() {
        return false;
    }

    @Override
    protected void executeForNicheArea() {
        List<OID> data = getData();

        if (isEmptyOrNull(data)) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Shorting out because there is nothing to do for " + getLoggingEntityName() + "!");
            }
            return;
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Processing " + data.size() + " " + getLoggingEntityName());
        }

        int processed = 0;

        // jw: let's process the data in chunks of 100!
        SubListIterator<OID> chunks = new SubListIterator<OID>(data, processDataIndividually() ? 1 : SubListIterator.CHUNK_SMALL);
        while (chunks.hasNext()) {
            List<OID> chunk = chunks.next();

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Processing " + processed + "-" + (processed + chunk.size()) + " out of " + data.size() + " " + getLoggingEntityName());
            }

            TaskRunner.doRootAreaTask(getAreaContext().getArea().getOid(), new AreaTaskImpl<Object>() {
                @Override
                protected Object doMonitoredTask() {

                    processChunk(chunk);

                    return null;
                }
            });

            processed += chunk.size();
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Finished processing " + data.size() + " " + getLoggingEntityName() + "!");
        }
    }
}
