package org.narrative.network.customizations.narrative.niches.referendum.services;

import org.narrative.common.util.IPDateUtil;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;

import java.sql.Timestamp;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/27/18
 * Time: 7:41 AM
 */
public class RescheduleReferendumTask extends UpdateReferendumTask {
    public RescheduleReferendumTask(Referendum referendum) {
        super(referendum);
    }

    @Override
    protected void updateReferendum(Referendum referendum) {
        referendum.setEndDatetime(new Timestamp(System.currentTimeMillis() + referendum.getType().getVotingPeriodExtensionInHours() * IPDateUtil.HOUR_IN_MS));
    }
}
