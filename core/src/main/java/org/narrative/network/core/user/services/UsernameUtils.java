package org.narrative.network.core.user.services;

import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.posting.HtmlTextMassager;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 5/21/18
 * Time: 3:02 PM
 */
public class UsernameUtils {
    private UsernameUtils() {
        throw UnexpectedError.getRuntimeException("Should never instantiate this utility class!");
    }

    public static final String DELIMETER = "_";
    public static final int MIN_LENGTH = 3;
    public static final int MAX_LENGTH = 20;

    private static final String INVALID_USERNAME_CHARS_REGEX_STR = "[^a-z0-9_]";
    public static final Pattern INVALID_USERNAME_CHARS_PATTERN = Pattern.compile(INVALID_USERNAME_CHARS_REGEX_STR, Pattern.CASE_INSENSITIVE);
    private static final Pattern SPACE_PATTERN = Pattern.compile(" ");
    private static final String SPACE_REPLACEMENT = Matcher.quoteReplacement(DELIMETER);

    public static String getIdealUsername(String from) {
        // bl: unescape escaped entities first
        String username = HtmlTextMassager.enableDisabledHtml(from);
        // strip accents from characters to normalize as close to ascii as possible
        username = StringUtils.stripAccents(username);
        // bl: trim spaces off the ends
        username = IPStringUtil.getTrimmedString(username);
        // bl: convert consecutive spaces to single space
        username = HtmlTextMassager.convertConsecutiveSpacesToSingleSpace(username);
        // convert any remaining spaces to the delimiter character
        username = SPACE_PATTERN.matcher(username).replaceAll(SPACE_REPLACEMENT);
        // bl:v23.5: not sure I even want the ASCIIFoldingUtil here at this point.
        // use lucene's magical ASCII folding to convert other cyrillics and special characters, as well.
        //username = ASCIIFoldingUtil.getAsciiFoldedString(username);
        // bl: then we need to strip any non-alphanumeric, non-space characters
        username = INVALID_USERNAME_CHARS_PATTERN.matcher(username).replaceAll("");
        // bl: just lowercase and we're done!
        username = username.toLowerCase();

        // jw: let's ensure that the length is within the required dimensions by either trimming it down, or building it
        //     up append the the delimiter character.
        int length = username.length();
        if (length < MIN_LENGTH) {
            StringBuilder paddedUsername = new StringBuilder(username);
            while (paddedUsername.length() < MIN_LENGTH) {
                paddedUsername.append(DELIMETER);
            }
            username = paddedUsername.toString();

        } else if (length > MAX_LENGTH) {
            return username.substring(0, MAX_LENGTH);
        }

        return username;
    }
}
