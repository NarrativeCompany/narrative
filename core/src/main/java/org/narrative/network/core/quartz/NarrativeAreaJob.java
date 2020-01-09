package org.narrative.network.core.quartz;

import org.narrative.network.core.area.base.Area;
import org.quartz.JobExecutionContext;

/**
 * Date: 2019-04-19
 * Time: 13:06
 *
 * @author brian
 */
public abstract class NarrativeAreaJob extends AreaJob {
    @Override
    protected Area getArea(JobExecutionContext context) {
        return Area.dao().getNarrativePlatformArea();
    }
}
