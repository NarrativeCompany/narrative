package org.narrative.network.core.mentions;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.preferences.AreaNotificationType;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 7/22/16
 * Time: 2:57 PM
 */
public class MentionsUtil {
    private static final String ESCAPE_PREFIX = "[@mention:";
    private static final String ESCAPE_SUFFIX = "]";
    private static final Pattern ESCAPED_MENTION_PATTERN = Pattern.compile(escapeSpecialPatternCharacters(ESCAPE_PREFIX) + "([0-9]*)" + escapeSpecialPatternCharacters(ESCAPE_SUFFIX), Pattern.CASE_INSENSITIVE);

    private MentionsUtil() {
        throw UnexpectedError.getRuntimeException("Should never construct the MentionsUtil!");
    }

    public static Set<OID> getMentionedMemberOids(String escapedHtml) {
        Set<OID> mentionedMemberOids = new HashSet<>();

        Matcher matcher = ESCAPED_MENTION_PATTERN.matcher(escapedHtml);
        while (matcher.find()) {
            mentionedMemberOids.add(OID.valueOf(matcher.group(1)));
        }

        return mentionedMemberOids;
    }

    public static List<OID> getMentionedMemberOidsToNotify(String escapedHtml) {
        return AreaUser.dao().getAllUserOidsWithCommunitySubscription(getMentionedMemberOids(escapedHtml), AreaNotificationType.SOMEONE_MENTIONS_ME);
    }

    public static String getLinkedHtml(String rawBody) {
        return processBody(rawBody, true);
    }

    public static String getPlainTextMentions(String rawBody) {
        return processBody(rawBody, false);
    }

    private static String processBody(String rawBody, boolean asHtml) {
        // jw: first, lets just parse out the memberOids so that we can lookup all of the members in one chunk, and further
        //     we will know if there is even anything that needs to happen.
        Set<OID> mentionedMemberOids = getMentionedMemberOids(rawBody);
        if (mentionedMemberOids.isEmpty()) {
            return rawBody;
        }

        Map<OID, User> userLookup = User.dao().getIDToObjectsFromIDs(mentionedMemberOids);

        StringBuffer body = new StringBuffer();

        // jw: Lets process all of the escaped mentions, and replace them with actual links.
        Matcher matcher = ESCAPED_MENTION_PATTERN.matcher(rawBody);
        while (matcher.find()) {
            OID memberOid = OID.getOIDFromString(matcher.group(1));

            User user = userLookup.get(memberOid);
            // jw: this should never happen, but just in case lets handle the case where we cannot find a member.
            if (!exists(user) || !user.isVisible()) {
                matcher.appendReplacement(body, Matcher.quoteReplacement(getMentionRawText(wordlet("role.formerNarratorUsername"))));

            } else {
                matcher.appendReplacement(body, Matcher.quoteReplacement(asHtml ? MentionsHtmlParser.getMentionLink(user) : getMentionRawText(user.getUsername())));
            }
        }
        matcher.appendTail(body);

        return body.toString();
    }

    private static String getMentionRawText(String username) {
        return "@" + username;
    }

    public static String getMentionPlaceholder(OID memberOid) {
        return ESCAPE_PREFIX + memberOid + ESCAPE_SUFFIX;
    }
}
