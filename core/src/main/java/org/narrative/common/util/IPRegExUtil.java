package org.narrative.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date: Feb 23, 2006
 * Time: 12:03:17 PM
 *
 * @author Brian
 */
public class IPRegExUtil {
    /**
     * Simple utility function to concatenate the results of one or more groups together from a given Matcher.
     * <p>
     * Note: If we start developing more Regex Utility functions we should probably create a RegexUtil and put this there.
     * In that case it will need to be beefed up to account for a Matcher not necissarily having a group that is being
     * flattened.  In this context we have already ensured that the Matcher has all of the Groups so we are safe.
     *
     * @param match  The Matcher that contains all of the groups we want to flatten into a single string
     * @param groups The Groups that we want to flatten,  in the order that we want to flatten them.
     * @return The flattened string: match.group(groups[n])+match.group(groups[n+1])+match.group(groups[n+x]);
     */
    public static String getGroupsAsString(Matcher match, int[] groups) {
        StringBuilder ret = new StringBuilder();
        for (int group : groups) {
            ret.append(match.group(group));
        }

        return ret.toString();
    }

    public static void appendRegExReplacementStringForGroups(StringBuilder stringBuilder, int[] groups) {
        if (groups == null) {
            return;
        }

        for (int group : groups) {
            stringBuilder.append("$");
            stringBuilder.append(Integer.toString(group));
        }
    }

    /**
     * given an array of ints representing groups, get a String that can be used in a replacement (via Matcher)
     * to extract the values of the specified groups.
     * <p>
     * e.g. {1, 2} will result in the following String: "$1$2"
     *
     * @param groups the groups to get the reg ex replacement string for
     * @return the reg ex replacement string for the specified groups
     */
    public static String getRegExReplacementStringForGroups(int[] groups) {
        StringBuilder ret = new StringBuilder();
        appendRegExReplacementStringForGroups(ret, groups);
        return ret.toString();
    }

    private static final Pattern BACKSLASH_PATTERN = Pattern.compile("\\", Pattern.LITERAL);
    private static final String BACKSLASH_REPLACEMENT = Matcher.quoteReplacement("\\\\");

    private static final Pattern DOT_PATTERN = Pattern.compile(".", Pattern.LITERAL);
    private static final String DOT_REPLACEMENT = Matcher.quoteReplacement("\\.");

    private static final Pattern QUESTION_MARK_PATTERN = Pattern.compile("?", Pattern.LITERAL);
    private static final String QUESTION_MARK_REPLACEMENT = Matcher.quoteReplacement("\\?");

    private static final Pattern OPEN_PAREN_PATTERN = Pattern.compile("(", Pattern.LITERAL);
    private static final String OPEN_PAREN_REPLACEMENT = Matcher.quoteReplacement("\\(");

    private static final Pattern CLOSE_PAREN_PATTERN = Pattern.compile(")", Pattern.LITERAL);
    private static final String CLOSE_PAREN_REPLACEMENT = Matcher.quoteReplacement("\\)");

    private static final Pattern ASTERISK_PATTERN = Pattern.compile("*", Pattern.LITERAL);
    private static final String ASTERISK_REPLACEMENT = Matcher.quoteReplacement("\\*");

    private static final Pattern DOLLAR_SIGN_PATTERN = Pattern.compile("$", Pattern.LITERAL);
    private static final String DOLLAR_SIGN_REPLACEMENT = Matcher.quoteReplacement("\\$");

    public static String getStringAfterEscapingPatternRegExChars(String original) {
        original = BACKSLASH_PATTERN.matcher(original).replaceAll(BACKSLASH_REPLACEMENT);
        original = DOT_PATTERN.matcher(original).replaceAll(DOT_REPLACEMENT);
        original = QUESTION_MARK_PATTERN.matcher(original).replaceAll(QUESTION_MARK_REPLACEMENT);
        original = OPEN_PAREN_PATTERN.matcher(original).replaceAll(OPEN_PAREN_REPLACEMENT);
        original = CLOSE_PAREN_PATTERN.matcher(original).replaceAll(CLOSE_PAREN_REPLACEMENT);
        original = ASTERISK_PATTERN.matcher(original).replaceAll(ASTERISK_REPLACEMENT);
        original = DOLLAR_SIGN_PATTERN.matcher(original).replaceAll(DOLLAR_SIGN_REPLACEMENT);
        return original;
    }
}
