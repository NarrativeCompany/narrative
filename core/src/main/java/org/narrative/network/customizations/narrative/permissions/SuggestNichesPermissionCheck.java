package org.narrative.network.customizations.narrative.permissions;

import org.narrative.common.util.IPDateUtil;
import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.service.api.model.permissions.SuggestNichesRevokeReason;
import org.narrative.network.shared.util.NetworkDateUtils;

import java.sql.Timestamp;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2019-02-10
 * Time: 16:21
 *
 * @author brian
 */
public class SuggestNichesPermissionCheck implements NarrativePermissionCheck {
    @Override
    public void checkRight(AreaRole areaRole) {
        // check to make sure the user hasn't suggested a niche within the past 24 hours
        Niche niche = Niche.dao().getMostRecentNicheSuggestedByUser(areaRole.getAreaUserRlm());
        if (exists(niche)) {
            Timestamp nicheSuggestionAllowedDate = new Timestamp(niche.getSuggestedDatetime().getTime() + (Niche.HOURS_BETWEEN_NICHE_SUGGESTIONS * IPDateUtil.HOUR_IN_MS));
            // bl: for now, i'm intentionally keeping this requirement in place even on dev and QA servers for testing purposes.
            if (nicheSuggestionAllowedDate.after(now())) {
                // if the user is high rep or tribunal, then allow unlimited niche suggestions
                if(!areaRole.getUser().isCanParticipateInTribunalIssues() && !areaRole.getUser().getReputation().getLevel().isHigh()) {
                    throw new NarrativePermissionRevokedError(wordlet("managedNarrativeCircleType.accessError.title.suggestNiches"), wordlet("submitNicheDetailsActionBase.mustWaitToSuggestNiche", formatNumber(Niche.HOURS_BETWEEN_NICHE_SUGGESTIONS), NetworkDateUtils.dateFormatShortDatetimeNoPrettyTime(nicheSuggestionAllowedDate, null, false)), SuggestNichesRevokeReason.SUGGESTED_IN_LAST_24_HOURS, nicheSuggestionAllowedDate.toInstant());
                }
            }
        }
    }
}
