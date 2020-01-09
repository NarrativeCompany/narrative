package org.narrative.network.core.moderation;

import org.narrative.common.persistence.DAO;
import org.narrative.common.persistence.OID;
import org.narrative.common.util.enums.EnumRegistry;
import org.narrative.network.core.composition.base.CompositionCache;
import org.narrative.network.core.composition.base.CompositionConsumer;
import org.narrative.network.core.composition.base.CompositionType;
import org.narrative.network.core.quartz.NetworkJob;
import org.narrative.network.shared.services.AuthorProvider;
import org.quartz.JobBuilder;
import org.quartz.JobExecutionContext;

import java.sql.Timestamp;

/**
 * Date: 9/29/11
 * Time: 11:17 AM
 *
 * @author brian
 */
public interface Moderatable<T extends CompositionCache, S extends ModeratableStats, D extends DAO> extends CompositionConsumer<T, S, D>, AuthorProvider {

    public String getFullText(boolean includeFileContents);

    public String getGuestNameResolved();

    public ModerationStatus getModerationStatus();

    public void setModerationStatus(ModerationStatus moderationStatus);

    public void updateLiveDatetime(Timestamp liveDatetime);

    public static final String MODERATABLE_OID = "moderatableOid";
    public static final String COMPOSITION_TYPE = "compositionType";

    // todo: remove this when we remove all of the moderation rules stuff.
    public static final String MODERATABLE_TYPE = "moderatableType";

    public default void storeJobData(JobBuilder jobBuilder) {
        jobBuilder.usingJobData(MODERATABLE_OID, getOid().getValue());
        jobBuilder.usingJobData(COMPOSITION_TYPE, getCompositionType().getId());
    }

    public static Moderatable fetchFromJobData(JobExecutionContext context) {
        CompositionType compositionType = NetworkJob.getIntegerEnumFromJobDataMap(context.getMergedJobDataMap(), CompositionType.class, COMPOSITION_TYPE);

        // jw: todo: remove this when we remove the Moderation Rules.
        if (compositionType == null) {
            ModeratableType moderatableType = EnumRegistry.getForId(ModeratableType.class, context.getMergedJobDataMap().getString(MODERATABLE_TYPE), false);

            if (moderatableType != null) {
                compositionType = moderatableType.getCompositionType();
            }
        }

        if (compositionType == null) {
            return null;
        }
        OID moderatableOid = NetworkJob.getOidFromContext(context, MODERATABLE_OID);
        return (Moderatable) compositionType.getDAO().get(moderatableOid);
    }
}
