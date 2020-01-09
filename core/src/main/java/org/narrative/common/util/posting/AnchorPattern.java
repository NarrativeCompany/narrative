package org.narrative.common.util.posting;

import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.util.IPHTMLUtil;
import org.narrative.common.util.IPRegExUtil;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.IPUtil;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: Sep 6, 2005
 * Time: 2:20:34 AM
 * This class encapsulates the logic to parse a URL out of a string using the provided pattern during instantiation
 * <p>
 * This class is needed because the core Regex libraries do not allow us to perform programatic changes to regex
 * result groups during the regex replacement process.  This way we can truncate the Content body of the Anchor tag.
 */
public class AnchorPattern {
    private final Pattern pattern;
    private final int[] preGroups;
    private final int[] urlGroups;
    private final int[] contentGroups;
    private final int[] postGroups;
    private final boolean prependHttp;

    /**
     * Creates a AnchorPattern which allows the creator to specify the Regex used to find URL patterns in a string
     * and allows the creator to define the groups that contain all of the important information needed
     * to create the URL string.  The Important information is:
     * [pre]<a href="[url]">[content]</a>[post]
     *
     * @param pattern       The Regex that pulls out the Anchor Information from a string
     * @param preGroups     The Group(s) that contain the [pre] information
     * @param urlGroups     The Group(s) that contain the [url] information
     * @param contentGroups The Group(s) that contain the [content] information
     * @param postGroups    The Group(s) that contain the [post] information
     * @param prependHttp   Whether or not the "http://" needs to be prepended to the [url].  Some Regex's do not have this.
     */
    public AnchorPattern(String pattern, int[] preGroups, int[] urlGroups, int[] contentGroups, int[] postGroups, boolean prependHttp) {
        this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
        this.preGroups = preGroups;
        this.urlGroups = urlGroups;
        this.contentGroups = contentGroups;
        this.postGroups = postGroups;
        this.prependHttp = prependHttp;
    }

    /**
     * Iterates through all Pattern matches within the provided string and replaces them with
     * a anchor tag.
     *
     * @param content The String that may contain URL's that need to be converted to Anchor tags
     * @return The String after replacing all Matches from the Anchor Pattern
     */
    public String replace(String content) {
        if (IPStringUtil.isEmpty(content)) {
            return content;
        }

        Matcher matcher = pattern.matcher(content);

        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            matcher.appendReplacement(result, getReplacementString(matcher));
        }

        matcher.appendTail(result);

        return result.toString();
    }

    private String getReplacementString(Matcher matcher) {
        StringBuilder replacement = new StringBuilder();

        // the replacements for the "pre" groups
        IPRegExUtil.appendRegExReplacementStringForGroups(replacement, preGroups);

        replacement.append("<a href=\"");
        if (prependHttp) {
            replacement.append("http://");
        }

        String url = getUrlFromMatcher(matcher);
        String tagContent = getTagContentFromMatcher(matcher);

        // jw: if we are stripping off non breaking spaces from the end of the URL as part of URL as content
        //     processing we will want to track the breaking spaces that were removed so that we can include
        //     them after the new link.
        List<String> trailingHardSpaces = null;

        // Lets only truncate the content if it is the URL.  Otherwise someone typed it in and we
        // want to go ahead and leave it as is
        if (IPUtil.isEqual(url, tagContent)) {
            ObjectPair<String, List<String>> urlAndMatches = IPStringUtil.getStringAndMatchesAfterStrippingFromEnd(url, "&nbsp;", "&#160;");
            url = urlAndMatches.getOne();

            tagContent = AnchorTextMassager.getTruncatedUrlString(url);
            trailingHardSpaces = urlAndMatches.getTwo();
        }

        // sanitize the url, escape replacement reg ex chars as necessary, and append to our replacement string.
        url = Matcher.quoteReplacement(IPHTMLUtil.getSanitizedUrl(url));
        replacement.append(url);

        replacement.append("\" target=\"_blank\"");

        replacement.append(">");

        // make sure to escape any invalid chars from the content (i.e. backslashes and dollar signs)
        replacement.append(Matcher.quoteReplacement(tagContent));

        replacement.append("</a>");

        if (trailingHardSpaces != null) {
            for (String hardSpace : trailingHardSpaces) {
                replacement.append(hardSpace);
            }
        }

        // the replacements for the "post" groups
        IPRegExUtil.appendRegExReplacementStringForGroups(replacement, postGroups);

        return replacement.toString();
    }

    private String getTagContentFromMatcher(Matcher matcher) {
        return IPRegExUtil.getGroupsAsString(matcher, contentGroups);
    }

    private String getUrlFromMatcher(Matcher matcher) {
        return IPRegExUtil.getGroupsAsString(matcher, urlGroups);
    }
}
