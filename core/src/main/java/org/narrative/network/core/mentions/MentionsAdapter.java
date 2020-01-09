package org.narrative.network.core.mentions;

import org.narrative.network.core.user.User;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * This adapter class is used to convert user-input plain text mentions (e.g. @username) and convert them into the HTML
 * format that is expected by MentionsHtmlParser.
 *
 * Date: 10/28/18
 * Time: 9:48 AM
 *
 * @author brian
 */
public class MentionsAdapter {
    private static final Pattern MENTION_PATTERN = Pattern.compile("(?<=\\A|\\p{Punct}|\\s)+@([a-z0-9_]+)(?=\\p{Punct}|\\s|\\Z)+", Pattern.CASE_INSENSITIVE);

    public static String convertMentionsToHtml(String body) {
        Map<String, User> usernamesToUser = User.dao().getUsersByUsername(networkContext().getAuthZone(), getAllMentionedUsernames(body));
        // bl: in order to ensure we do case-insensitive matches, convert usernames here to lowercase.
        // it turns out that we have some usernames stored with uppercase letters in the database.
        usernamesToUser = usernamesToUser.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toLowerCase(), Map.Entry::getValue));
        StringBuffer sb = new StringBuffer();
        Matcher matcher = MENTION_PATTERN.matcher(body);
        while(matcher.find()) {
            // bl: usernames in the map are all lowercase, so lowercase the string before
            // doing the lookup here to ensure we effectively do a case-insensitive match.
            String username = matcher.group(1).toLowerCase();
            User user = usernamesToUser.get(username);
            // if there is no active user found, then just keep it as plain text
            if(!exists(user) || !user.isVisible()) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group()));
            } else {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(MentionsHtmlParser.getMentionLink(user)));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    static List<String> getAllMentionedUsernames(String body) {
        List<String> ret = new LinkedList<>();
        Matcher matcher = MENTION_PATTERN.matcher(body);
        while(matcher.find()) {
            ret.add(matcher.group(1));
        }
        return ret;
    }
}
