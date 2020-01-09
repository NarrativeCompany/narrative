package org.narrative.network.customizations.narrative.channels;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssueType;

import java.util.Comparator;

/**
 * Date: 2018-12-19
 * Time: 10:17
 *
 * @author jonmark
 */
public interface ChannelConsumer {
    // jw: these constants are used for both Niches and Publications, so centralizing them here even if ChannelConsumer
    //     does not directly define them.
    int MIN_NAME_LENGTH = 3;
    int MAX_NAME_LENGTH = 60;

    int MAX_DESCRIPTION_LENGTH = 256;
    int MIN_DESCRIPTION_LENGTH = 10;

    Comparator<ChannelConsumer> NAME_COMPARATOR = (o1, o2) -> {
        int ret = o1.getName().compareToIgnoreCase(o2.getName());
        if (ret != 0) {
            return ret;
        }
        return OID.compareOids(o1.getOid(), o2.getOid());
    };

    OID getOid();
    ChannelType getChannelType();
    Channel getChannel();
    User getChannelOwner();
    String getName();
    String getNameForHtml();
    String getDisplayUrl();
    TribunalIssueType getPossibleTribunalIssueType();
    boolean isCanCurrentRolePost();
}
