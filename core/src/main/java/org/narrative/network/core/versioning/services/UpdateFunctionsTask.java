package org.narrative.network.core.versioning.services;

import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.cluster.partition.NamedSql;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 7/6/17
 * Time: 9:52 AM
 */
public class UpdateFunctionsTask extends GlobalTaskImpl<Object> {
    private final Properties data;

    public UpdateFunctionsTask(Properties data) {
        super(true);

        this.data = data;
    }

    @Override
    protected Object doMonitoredTask() {
        //apply functions to each partition
        for (Partition part : Partition.dao().getAll(true)) {
            StringBuilder functionString = new StringBuilder();
            List<NamedSql> functions = part.getPartitionType().getFunctions();
            if (!functions.isEmpty()) {

                // jw: this task is ran in two cases, first, for patches, in which case we should always run it, and second when encryption changes. We will know we are running for
                //     patches if we have a "data" Properties object, so we will determine if we need to update this function in two different ways.
                boolean updateFunctions = false;
                String functionMD5;
                // jw: first, if we are running for a patch, we will run if the MD5 of the functions have changed.
                if (data != null) {
                    //see if the functions have changed
                    for (NamedSql function : functions) {
                        functionString.append(function.getName());
                        functionString.append(function.getSql());
                    }
                    functionMD5 = IPStringUtil.getMD5DigestFromString(functionString.toString());
                    String oldMD5 = data.getProperty(part.getOid().toString());
                    updateFunctions = !functionMD5.equals(oldMD5);

                    // if we are running for encryption update, then see if any of the SQL statements has a "ENCRYPTION='" directive in it.
                } else {
                    functionMD5 = null;
                    for (NamedSql function : functions) {
                        if (function.getSql().contains(" ENCRYPTION='")) {
                            updateFunctions = true;
                            break;
                        }
                    }
                }

                // jw: functions are different so re-run
                if (updateFunctions) {
                    for (NamedSql function : functions) {
                        try {
                            part.getDatabaseResources().executeStatement(function.getSql(), function.getName());
                        } catch (SQLException e) {
                            throw UnexpectedError.getRuntimeException("Unable to apply function: " + function.getName() + " to partition " + part.getDisplayName(), e);
                        }
                    }
                    if (data != null) {
                        data.setProperty(part.getOid().toString(), functionMD5);
                    }
                }
            }

        }
        return null;
    }
}
