package org.narrative.network.core.cluster.actions;

import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.security.area.community.advanced.AreaCircle;
import org.narrative.network.customizations.narrative.niches.NarrativeCircleType;

/**
 * Date: 2020-01-03
 * Time: 12:14
 *
 * @author jonmark
 */
public abstract class ManageCircleMembersActionBase extends ClusterAction {

    public static final String CIRCLE_PARAM = "circle";

    private NarrativeCircleType circle;

    @Override
    public void checkRightAfterParams() {
        if(circle==null) {
            throw UnexpectedError.getRuntimeException("circle is required!");
        }
        if(!circle.isMembershipManageable()) {
            throw UnexpectedError.getRuntimeException("Can't manage circles that aren't manageable!");
        }
    }

    protected AreaCircle getAreaCircle() {
        Area area = Area.dao().getNarrativePlatformArea();

        return circle.getCircle(area.getAuthZone());
    }

    public NarrativeCircleType getCircle() {
        return circle;
    }

    public void setCircle(NarrativeCircleType circle) {
        this.circle = circle;
    }
}
