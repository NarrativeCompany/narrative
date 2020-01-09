package org.narrative.network.customizations.narrative.niches.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.security.area.community.advanced.AreaCircle;
import org.narrative.network.core.security.area.community.advanced.services.GlobalSecurable;
import org.narrative.network.core.settings.area.community.advanced.SandboxedCommunitySettings;
import org.narrative.network.core.settings.global.GlobalSettings;
import org.narrative.network.core.settings.global.services.GlobalSettingsUtil;
import org.narrative.network.customizations.narrative.niches.NarrativeCircleType;
import org.narrative.network.customizations.narrative.niches.nicheauction.services.ProcessPaidInvoicesJob;
import org.narrative.network.customizations.narrative.niches.referendum.services.ProcessEndedReferendumsJob;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import java.util.HashMap;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/9/18
 * Time: 8:41 AM
 */
public class EnableNichesCustomization extends AreaTaskImpl<Object> {
    @Override
    protected Object doMonitoredTask() {
        SandboxedCommunitySettings settings = getAreaContext().getAreaRlm().getSandboxedCommunitySettings();

        // jw: before we get into the custom profile fields, lets ensure that we setup the core settings.

        // jw: setup any jobs that we need to run for niche specific processing
        ProcessEndedReferendumsJob.registerForArea(getAreaContext().getArea());
        ProcessPaidInvoicesJob.registerForArea(getAreaContext().getArea());

        // jw: if the ne current minimum bid is not set, then let's set that up.
        GlobalSettings globalSettings = GlobalSettingsUtil.getGlobalSettingsForWrite();
        if (globalSettings.getNrveUsdPrice()==null) {
            globalSettings.refreshCurrentNrveCachedValues();
        }

        // jw: if this is the first time we have run, lets disable all AreaModuleTypes since narrative should not be using them by default.
        if (!settings.isHasSetupNarrativeCustomizationBefore()) {
            // jw: now that we are done, let's make sure we never do this base setup again!
            settings.setHasSetupNarrativeCustomizationBefore(true);
        }

        // jw: let's setup the Niche special area circles
        {
            Map<NarrativeCircleType, OID> typeToCircleOid = new HashMap<>();
            Map<NarrativeCircleType, AreaCircle> typeToCircle = settings.getCirclesByNarrativeCircleType();
            for (NarrativeCircleType circleType : NarrativeCircleType.values()) {
                AreaCircle circle = typeToCircle.get(circleType);
                if (!exists(circle)) {
                    circle = new AreaCircle(getAreaContext().getArea());
                    circle.setName(circleType.getName());
                    circle.setLabel(circleType.getLabel());
                    circle.setViewableByAdminsOnly(circleType.isViewableByAdminsOnly());
                    AreaCircle.dao().save(circle);

                    // jw: Lets ensure that we add any default permissions to the circle
                    for (GlobalSecurable defaultSecurable : circleType.getDefaultSecurables()) {
                        circle.addSecurable(getAreaContext().getArea().getAreaResource(), defaultSecurable);
                    }
                }

                typeToCircleOid.put(circleType, circle.getOid());
            }

            settings.setNarrativeCircleAssociations(typeToCircleOid);
        }

        return null;
    }

}
