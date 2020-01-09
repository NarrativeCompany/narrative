package org.narrative.network.customizations.narrative.niches.tribunal;

import org.narrative.common.util.NameForDisplayProvider;
import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.network.customizations.narrative.niches.referendum.ReferendumType;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: martin
 * Date: 12/02/2018
 * Time: 13:51
 */
public enum TribunalIssueType implements IntegerEnum, NameForDisplayProvider {
    APPROVE_REJECTED_NICHE(0, ReferendumType.TRIBUNAL_APPROVE_REJECTED_NICHE),
    RATIFY_NICHE(1, ReferendumType.TRIBUNAL_RATIFY_NICHE),
    APPROVE_NICHE_DETAIL_CHANGE(2, ReferendumType.TRIBUNAL_APPROVE_NICHE_DETAIL_CHANGE),
    RATIFY_PUBLICATION(3, ReferendumType.TRIBUNAL_RATIFY_PUBLICATION),
    ;

    private final int id;
    private final ReferendumType referendumTypeForTribunal;

    public static final Map<ReferendumType, TribunalIssueType> BY_TRIBUNAL_REFERENDUM_TYPE = Collections.unmodifiableMap(new HashMap<ReferendumType, TribunalIssueType>() {{
        for (TribunalIssueType type : TribunalIssueType.values()) {
            put(type.getReferendumTypeForTribunal(), type);
        }
    }});

    /**
     * all of the appeal types that are used for appeal submission purposes. a niche name/description edit isn't as
     * much of an appeal as it is an edit request. also, we are going to start charging for it eventually, so
     * it's excluded.
     */
    public static final Set<TribunalIssueType> APPEAL_TYPES = Collections.unmodifiableSet(EnumSet.complementOf(EnumSet.of(APPROVE_NICHE_DETAIL_CHANGE)));

    TribunalIssueType(int id, ReferendumType referendumTypeForTribunal) {
        assert referendumTypeForTribunal != null : "Must always provide a ReferendumType for tribunal! t/" + this;

        this.id = id;
        this.referendumTypeForTribunal = referendumTypeForTribunal;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getNameForDisplay() {
        return wordlet("tribunalIssueType." + this);
    }

    public String getDescriptionForIssue() {
        return wordlet("tribunalIssueType.description." + this);
    }

    public ReferendumType getReferendumTypeForTribunal() {
        return referendumTypeForTribunal;
    }

    public boolean isApproveRejectedNiche() {
        return this == APPROVE_REJECTED_NICHE;
    }

    public boolean isRatifyNiche() {
        return this == RATIFY_NICHE;
    }

    public boolean isApproveNicheDetailChange() {
        return this == APPROVE_NICHE_DETAIL_CHANGE;
    }

    public boolean isRatifyPublication() {
        return this == RATIFY_PUBLICATION;
    }
}
