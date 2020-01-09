package org.narrative.common.util.posting;

import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.html.HTMLEnforcer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Date: Feb 22, 2006
 * Time: 1:52:53 PM
 *
 * @author Brian
 */
public class AnchorTextMassager {

    /**
     * Original patterns from the beginning of the ubbcodetohtml group in PerlExpressions.txt
     * s/(^|\s[\(]?)(http|https|ftp)(:\/\/[^\"\'\`\(\)\s]+)(\.)(\s|$)/$1<A HREF="$2$3" TARGET=_blank>$2$3<\/A>$4$5/isg
     * s/(^|\s[\(]?)(http|https|ftp)(:\/\/[^\"\'\`\(\)\s]+)(\))(\s|$)/$1<A HREF="$2$3" TARGET=_blank>$2$3<\/A>$4$5/isg
     * s/(^|\s[\(]?)(www\.[^\"\'\`\(\)\s]+)(\.)(\s|$)+/$1<A HREF="http:\/\/$2" TARGET=_blank>$2<\/A>$3$4/isg
     * s/(^|\s[\(]?)(www\.[^\"\'\`\(\)\s]+)(\))(\s|$)+/$1<A HREF="http:\/\/$2" TARGET=_blank>$2<\/A>$3$4/isg
     * s/(^|\s[\(]?)(http|https|ftp)(:\/\/[^\"\'\`\(\)\s]+)/$1<A HREF="$2$3" TARGET=_blank>$2$3<\/A>/isg
     * s/(^|\s[\(]?)(www\.[^\"\'\`\(\)\s]+)/$1<A HREF="http:\/\/$2" TARGET=_blank>$2<\/A>/isg
     * s/(\[URL\])(http|https|ftp)(:\/\/[^\"\'\`\(\)\s]+?)(\[\/URL\])/<A HREF="$2$3" TARGET=_blank>$2$3<\/A>/isg
     * s/(\[URL\])([^\"\'\`\(\)\s]+?)(\[\/URL\])/<A HREF="http:\/\/$2" TARGET=_blank>$2<\/A>/isg
     */

    /**
     * bl: these are all of the special characters that can be in a domain name. we have a rule now that auto-linked
     * URLs can not end with one of these characters
     */
    private static final String SPECIAL_DOMAIN_CHARS_PATTERN = "\\.\\*\\?\\&\\+\\$\\-\\~/=!,:;%#@";

    // jw: Intentionally not including quotes in this set because we do not want to match URLs that are already in a
    //     HTML attribute.  EG: href="http://google.com"  (", ' and ` are all valid in this scenario)
    private static final String PRE_AUTO_LINK_GROUP = "(^|\\s|;|[\\(\\)\\[\\]{}>])";
    /**
     * bl: we will now include those special characters in the post-group match since we are excluding them
     * from matching the end of the URL below in VALID_DOMAIN_CHARS_PATTERN
     */
    private static final String POST_AUTO_LINK_GROUP = "([\\s" + SPECIAL_DOMAIN_CHARS_PATTERN + "]|&nbsp;|<|$)";

    // jw: we want to include: a-zA-Z0-9 $-_.!*,/:;=?&#@
    //     \w covers a-zA-Z0-9 and _, so then the rest are all there after that.
    // bl: adding the negative lookaheads here for &lt; and &gt; so that those can not be included in URLs.
    // this way, &lt;http://google.com&gt; will be properly hyperlinked and won't include the trailing &gt;.
    // bl: now adding an extra word character on the end to ensure that our domain ends in a word char, not a special char
    private static final String VALID_DOMAIN_CHARS_PATTERN = "((?!&lt;)(?!&gt;)[\\w" + SPECIAL_DOMAIN_CHARS_PATTERN + "]+\\w)";

    private static final String VALID_DOMAIN_CHARS_RELUCTANT_PATTERN = VALID_DOMAIN_CHARS_PATTERN + "?";
    private static final String VALID_DOMAIN_CHARS_RELUCTANT_GROUP = "(" + VALID_DOMAIN_CHARS_RELUCTANT_PATTERN + ")";

    public static final String HTTP_HTTPS_FTP_PATTERN_PREFIX = "(http|https|ftp)(://";
    private static final String HTTP_HTTPS_FTP_PATTERN_GROUPS = HTTP_HTTPS_FTP_PATTERN_PREFIX + VALID_DOMAIN_CHARS_PATTERN + ")";

    private static final String HTTP_HTTPS_FTP_RELUCTANT_PATTERN_GROUPS = HTTP_HTTPS_FTP_PATTERN_PREFIX + VALID_DOMAIN_CHARS_RELUCTANT_PATTERN + ")";

    private static final String WWW_PATTERN_GROUP = "(www\\." + VALID_DOMAIN_CHARS_PATTERN + ")";

    private static final String UBBCODE_URL_OPEN_GROUP = "(\\[URL\\])";
    private static final String UBBCODE_URL_OPEN_EQUALS_GROUP = "(\\[URL=)";
    private static final String UBBCODE_URL_CLOSE_GROUP = "(\\[/URL\\])";

    /**
     * Note:  Pre-defining these as private static members of this class so that we only have to
     * instantiate them once and from then on we can just use them.  Should speed things up
     * quite a bit.
     */

    // $1<A HREF="$2$3" TARGET=_blank>$2$3<\/A>$5$6
    private static final AnchorPattern ANCHOR_PATTERN_1 = new AnchorPattern(PRE_AUTO_LINK_GROUP + HTTP_HTTPS_FTP_PATTERN_GROUPS + "(\\.)" + POST_AUTO_LINK_GROUP, new int[]{1}, new int[]{2, 3}, new int[]{2, 3}, new int[]{5, 6}, false);

    // $1<A HREF="$2$3" TARGET=_blank>$2$3<\/A>$5$6
    private static final AnchorPattern ANCHOR_PATTERN_2 = new AnchorPattern(PRE_AUTO_LINK_GROUP + HTTP_HTTPS_FTP_PATTERN_GROUPS + "(\\))" + POST_AUTO_LINK_GROUP, new int[]{1}, new int[]{2, 3}, new int[]{2, 3}, new int[]{5, 6}, false);

    // $1<A HREF="http:\/\/$2" TARGET=_blank>$2<\/A>$4$5
    private static final AnchorPattern ANCHOR_PATTERN_3 = new AnchorPattern(PRE_AUTO_LINK_GROUP + WWW_PATTERN_GROUP + "(\\.)" + POST_AUTO_LINK_GROUP, new int[]{1}, new int[]{2}, new int[]{2}, new int[]{4, 5}, true);

    // $1<A HREF="http:\/\/$2" TARGET=_blank>$2<\/A>$4$5
    private static final AnchorPattern ANCHOR_PATTERN_4 = new AnchorPattern(PRE_AUTO_LINK_GROUP + WWW_PATTERN_GROUP + "(\\))" + POST_AUTO_LINK_GROUP, new int[]{1}, new int[]{2}, new int[]{2}, new int[]{4, 5}, true);

    // $1<A HREF="$2$3" TARGET=_blank>$2$3<\/A>
    private static final AnchorPattern ANCHOR_PATTERN_5 = new AnchorPattern(PRE_AUTO_LINK_GROUP + HTTP_HTTPS_FTP_PATTERN_GROUPS, new int[]{1}, new int[]{2, 3}, new int[]{2, 3}, new int[]{}, false);

    // $1<A HREF="http:\/\/$2" TARGET=_blank>$2<\/A>
    private static final AnchorPattern ANCHOR_PATTERN_6 = new AnchorPattern(PRE_AUTO_LINK_GROUP + WWW_PATTERN_GROUP, new int[]{1}, new int[]{2}, new int[]{2}, new int[]{}, true);

    // <A HREF="$2$3" TARGET=_blank>$2$3<\/A>
    private static final AnchorPattern ANCHOR_PATTERN_7 = new AnchorPattern(UBBCODE_URL_OPEN_GROUP + HTTP_HTTPS_FTP_RELUCTANT_PATTERN_GROUPS + UBBCODE_URL_CLOSE_GROUP, new int[]{}, new int[]{2, 3}, new int[]{2, 3}, new int[]{}, false);

    // <A HREF="http:\/\/$2" TARGET=_blank>$2<\/A>
    private static final AnchorPattern ANCHOR_PATTERN_8 = new AnchorPattern(UBBCODE_URL_OPEN_GROUP + VALID_DOMAIN_CHARS_RELUCTANT_GROUP + UBBCODE_URL_CLOSE_GROUP, new int[]{}, new int[]{2}, new int[]{2}, new int[]{}, true);

    // <A HREF="$2$3" TARGET=_blank>$5<\/A>
    private static final AnchorPattern ANCHOR_PATTERN_9 = new AnchorPattern(UBBCODE_URL_OPEN_EQUALS_GROUP + HTTP_HTTPS_FTP_RELUCTANT_PATTERN_GROUPS + "(\\])(.+?)" + UBBCODE_URL_CLOSE_GROUP, new int[]{}, new int[]{2, 3}, new int[]{6}, new int[]{}, false);

    // <A HREF="http:\/\/$2" TARGET=_blank>$4<\/A>
    private static final AnchorPattern ANCHOR_PATTERN_10 = new AnchorPattern(UBBCODE_URL_OPEN_EQUALS_GROUP + VALID_DOMAIN_CHARS_RELUCTANT_GROUP + "(\\])(.+?)" + UBBCODE_URL_CLOSE_GROUP, new int[]{}, new int[]{2}, new int[]{5}, new int[]{}, true);

    private static final List<AnchorPattern> AUTO_LINK_ANCHOR_PATTERNS;

    static {
        List<AnchorPattern> tempList = new ArrayList<AnchorPattern>();
        tempList.add(ANCHOR_PATTERN_1);
        tempList.add(ANCHOR_PATTERN_2);
        tempList.add(ANCHOR_PATTERN_3);
        tempList.add(ANCHOR_PATTERN_4);
        tempList.add(ANCHOR_PATTERN_5);
        tempList.add(ANCHOR_PATTERN_6);
        AUTO_LINK_ANCHOR_PATTERNS = Collections.unmodifiableList(tempList);
    }

    private static final List<AnchorPattern> UBBCODE_ANCHOR_PATTERNS;

    static {
        List<AnchorPattern> tempList = new ArrayList<AnchorPattern>();
        tempList.add(ANCHOR_PATTERN_7);
        tempList.add(ANCHOR_PATTERN_8);
        tempList.add(ANCHOR_PATTERN_9);
        tempList.add(ANCHOR_PATTERN_10);
        UBBCODE_ANCHOR_PATTERNS = Collections.unmodifiableList(tempList);
    }

    /**
     * Applies the Format to each Pattern required to do all of the UBBCode anchor replacements
     *
     * @param content The content that may contain ubb code
     * @return The content with the ubb code converted to HTML
     */
    public static String applyUbbCodeAnchorReplacements(String content) {
        if (IPStringUtil.isEmpty(content)) {
            return content;
        }

        for (AnchorPattern pattern : UBBCODE_ANCHOR_PATTERNS) {
            content = pattern.replace(content);
        }

        return content;
    }

    /**
     * Applies the Format to each Pattern required to do all of the auto-linking anchor replacements
     * <p>
     * Note:  do not call this on text which could have existing linked URLs in it.  The linked URL will be linked again
     *
     * @param content The content that may contain urls to auto-link
     * @return The content with the urls converted to links
     */
    public static String applyAutoLinkAnchorReplacements(String content) {
        return applyAutoLinkAnchorReplacements(content, null);
    }

    public static String applyAutoLinkAnchorReplacements(String content, Collection<? extends AnchorConditionalAttribute> conditionalAttributes) {
        if (IPStringUtil.isEmpty(content)) {
            return content;
        }

        // bl: need to parse with each anchor pattern one at a time to ensure that we do not double-link.
        // internally, HTMLEnforcer will make sure that auto-linking doesn't apply if we're already inside a link.
        for (AnchorPattern pattern : AUTO_LINK_ANCHOR_PATTERNS) {
            content = HTMLEnforcer.autoLinkUrlsWithoutEnforcingHtml(content, pattern, conditionalAttributes);
        }

        return content;
    }

    // bl: this pattern is used to "undo" the above links in the event that a message is being edited. 
    // s/(<A HREF=")(http|https|ftp)(:\/\/)(\S+?)(")( TARGET=_blank)?(>)(.+?)(<\/A>)/[URL=$2:\/\/$4]$8[\/URL]/isg
    private static final Pattern HTML_LINK_PATTERN = Pattern.compile("(<a href=\")(http|https|ftp)(://)(\\S+?)(\")( target=\"_blank\")?( onclick=\"event.cancelBubble=true;\")?(>)(.+?)(</a>)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final String HTML_LINK_REPLACEMENT = "[url=$2://$4]$9[/url]";
    // s/(<A HREF=")(http|https|ftp)(:\/\/)(\S+)(")( TARGET=_blank)?(>)(.+?)(<\/A>)/$2:\/\/$4/isg
    private static final String PLAIN_TEXT_LINK_REPLACEMENT = "$2://$4";

    public static String doAnchorReplacementsToUbbCode(String content) {
        content = HTML_LINK_PATTERN.matcher(content).replaceAll(HTML_LINK_REPLACEMENT);
        return content;
    }

    public static String doAnchorReplacementsToPlainText(String content) {
        content = HTML_LINK_PATTERN.matcher(content).replaceAll(PLAIN_TEXT_LINK_REPLACEMENT);
        return content;
    }

    public static String getTruncatedUrlString(String url) {
        return IPStringUtil.getTruncatedString(url, 30, 20);
    }
}
