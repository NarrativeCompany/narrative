package org.narrative.network.customizations.narrative.niches.referendum;

import org.narrative.common.util.NameForDisplayProvider;
import org.narrative.common.util.enums.IntegerEnum;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 4/16/18
 * Time: 10:43 AM
 * <p>
 * note: I created this with a generic name, but ultimately I could this being build more holistically into the voting framework
 * by having different reasons for up -vs- down votes, and building the framework to support that. For now, we are only
 * using these options for niche approval down vote reasons, so I am not over complicating the ReferendumType with what
 * I feel a complete implementation would be. Just not worth it until we have different options for different up/down votes.
 */
public enum ReferendumVoteReason implements IntegerEnum, NameForDisplayProvider {
    REDUNDANT(0),
    CONTAINS_PROFANITY(1),
    VIOLATES_TOS(2),
    SPELLING_ISSUE_IN_NAME(3),
    UNCLEAR_NAME_OR_DESCRIPTION(4),
    WRONG_LANGUAGE(5);

    private final int id;

    ReferendumVoteReason(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getNameForDisplay() {
        return wordlet("referendumVoteReason." + this);
    }

    public String getTooltipForDisplay() {
        return wordlet("referendumVoteReason.tooltip." + this);
    }
}