package org.narrative.common.util.posting;

import org.narrative.common.util.Alias;
import org.narrative.common.util.IPStringUtil;
import org.narrative.network.core.master.graemlins.Graemlin;
import org.narrative.network.core.master.graemlins.GraemlinType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date: Feb 22, 2006
 * Time: 12:07:08 PM
 *
 * @author Brian
 */
public class GraemlinMassager {
    // bl:graemlin the custom graemlin delimiter
    private static final String CUSTOM_GRAEMLIN_DELIMITER = ":";
    // bl:graemlin it's useful to know how many characters we'll have to match when looking for non-delimited graemlins
    private static final int MAX_NON_DELIMITED_GRAEMLIN_LENGTH = 2;
    // bl:graemlin the non-standard graemlins we support
    // nb: if we decide to get rid of the delimiter, then we can generate a similar array by parsing through the
    //     custom graemlins and caching per site.  note that in this case, we'll probably have to parse the message for each
    //     custom graemlin anyway.
    private static final char[][] NON_DELIMITED_GRAEMLINS = new char[][]{":)".toCharArray(), ":(".toCharArray(), ":D".toCharArray(), ":o".toCharArray(), ":p".toCharArray()};
    // bl:graemlin the ;) smilie is the one special case for us to handle
    private static final String NON_STANDARD_GRAEMLIN = ";)";
    private static final Pattern NON_STANDARD_GRAEMLIN_PATTERN = Pattern.compile(NON_STANDARD_GRAEMLIN, Pattern.LITERAL);

    public static final String GRAEMLIN_COMMENT_PREFIX = "<!--graemlin:";
    // bl:graemlin the list of stop words that we don't want graemlins to start with.
    // for now, the list is just the string "mailto:" because problems occur when you've got
    // mailto:paul@company.com because as you see, :p is in the string which will be converted
    // to a smilie which is a "bad thing".  same thing applies for mailto:D and mailto:o.  so,
    // we'll just add mailto: as a stop word and we're set.
    // bl: also, stop on the graemlin comment prefix followed by either the graemlin delimiter
    // or the non-standard graemlin.  this will prevent the WYSIWYG editor from breaking graemlins.
    // :) gets converted to <img src="..."><!--graemlin::)--> and we don't want the :) inside of
    // the graemlin comment to be translated to a graemlin.
    private static final char[][] GRAEMLIN_STOP_WORDS = new char[][]{"mailto:".toCharArray(), (GRAEMLIN_COMMENT_PREFIX + CUSTOM_GRAEMLIN_DELIMITER).toCharArray(), (GRAEMLIN_COMMENT_PREFIX + NON_STANDARD_GRAEMLIN).toCharArray()};

    private static final char[][] GRAEMLIN_START_MATCHES = new char[][]{CUSTOM_GRAEMLIN_DELIMITER.toCharArray()};

    private static final String OPEN_HTML_COMMENT = "<!--";
    private static final String CLOSE_HTML_COMMENT = "-->";
    private static final Pattern HTML_COMMENT_PATTERN = Pattern.compile("(" + OPEN_HTML_COMMENT + ")(.*?)(" + CLOSE_HTML_COMMENT + ")", Pattern.DOTALL);

    private static final String MAILTO_STRING = "mailto";

    /**
     * bl:graemlin this method goes "backwards" and converts a smilie image tag back into its original "smilie" form.
     * nb: still runs the old reg exes so that we maintain support for editing older messages.
     */
    public static String doHtmlGraemlinsToText(String msg, @NotNull Map<String, Graemlin> graemlinKeystrokeToReplacementHtml) {
        if (IPStringUtil.isEmpty(msg)) {
            return msg;
        }

        // first, convert back any of the old smilie format
        // bl: not supporting this anymore.
        //msg = TextReplacer.getInstance().applyReplacementSet(TextReplacer.HTML_W_SMILIES_TO_TEXT, msg);
        // now, we need to parse through and look for <img> tags with a "type" attribute.
        // then, what we'll do, is we'll do a lookup to see if that smilie exists on this site.
        // if it does, then we'll do the replacement.  otherwise, we'll just leave it in the html
        // so that at least the image will remain.

        StringBuffer ret = new StringBuffer();

        char[] msgCharArray = msg.toCharArray();
        int searchIndex = 0;
        int firstCharOfContentNotYetUsed = 0;

        do {
            int iStartOfTag;
            int iStartOfAttributes;
            {
                Alias.MatchResult startMatch = Alias.getMatchResult(msgCharArray, searchIndex, msgCharArray.length, new char[][]{"<img".toCharArray()}, null, -1);
                // no img tag found?  we're done!
                if (startMatch == null) {
                    break;
                }
                // start searching from this point forward
                iStartOfTag = startMatch.foundIndex;
                iStartOfAttributes = startMatch.afterMatchIndex;
            }
            {
                Alias.MatchResult endMatch = Alias.getMatchResult(msgCharArray, iStartOfAttributes, msgCharArray.length, new char[][]{">".toCharArray()}, new char[][]{"<".toCharArray()}, -1);
                // no end tag found?  found a < before we found a >?  well, then, see ya!
                if (endMatch == null) {
                    break;
                }
                // found an img tag.  set the searchIndex so we can look for the comment.
                searchIndex = endMatch.afterMatchIndex;
            }

            // found an img tag, now we need to look for the <!--graemlin:--> comment
            // immediately following the img tag.
            int iStartGraemlinKeystroke;
            int iEndGraemlinKeystroke;
            {
                Alias.MatchResult graemlinCommentMatch = Alias.getMatchResult(msgCharArray, searchIndex, searchIndex + GRAEMLIN_COMMENT_PREFIX.length(), new char[][]{GRAEMLIN_COMMENT_PREFIX.toCharArray()}, null, searchIndex);
                // no graemlin comment found?  probably just a normal image.  continue on.
                if (graemlinCommentMatch == null) {
                    continue;
                }
                iStartGraemlinKeystroke = graemlinCommentMatch.afterMatchIndex;
            }
            // find the end of the comment
            {
                Alias.MatchResult endCommentMatch = Alias.getMatchResult(msgCharArray, iStartGraemlinKeystroke, msgCharArray.length, new char[][]{"-->".toCharArray()}, null, -1);
                // no end comment found?  crappy.  that means we won't be able to match any more smilies!  just get out.
                if (endCommentMatch == null) {
                    break;
                }
                iEndGraemlinKeystroke = endCommentMatch.foundIndex - 1;
                searchIndex = endCommentMatch.afterMatchIndex;
            }

            // ok, we found the graemlin keystroke!  grab the string and replace it!
            String substitution;
            {
                int graemlinLength = iEndGraemlinKeystroke - iStartGraemlinKeystroke + 1;
                String graemlinStr = new String(msgCharArray, iStartGraemlinKeystroke, graemlinLength);
                // check that this smilie exists on the site.
                // if no siteOID is provided, then there is no smiliesToReplacements hashtable.  in that case, substitute anyway.
                // a little hack to set the replacement equal to a non-null value to be sure the substitution is made.
                Graemlin graemlin = graemlinKeystrokeToReplacementHtml.get(graemlinStr);
                // didn't find the replacement?  ok, then just leave the HTML in there and continue.
                if (graemlin == null) {
                    continue;
                }
                // found the replacement, so let's make the substitution!
                substitution = graemlinStr;
            }
            // if the start of the smilie was past the current starting point, then we need to append the unused portion to the buf
            if (firstCharOfContentNotYetUsed < iStartOfTag) {
                int size = iStartOfTag - firstCharOfContentNotYetUsed;
                ret.append(msgCharArray, firstCharOfContentNotYetUsed, size);
            }
            ret.append(substitution);
            firstCharOfContentNotYetUsed = searchIndex;
        } while (true);

        // check if we stopped before the end of the message.  if so, append on the remaining part.
        if (firstCharOfContentNotYetUsed <= msgCharArray.length - 1) {
            ret.append(msgCharArray, firstCharOfContentNotYetUsed, msgCharArray.length - firstCharOfContentNotYetUsed);
        }

        return ret.toString();
    }

    /**
     * bl:graemlin this method converts all smilies in a String to their corresponding HTML equivalent.
     * the method handles the special case of the ;) smilie and then does direct replacements
     * for all of the "non-standard" smilies (i.e. smilies not delimited on both ends).
     * it will short circuit whenever it finds that there are no more ':' (the delimiter) left in the message.
     */
    public static String doGraemlinsToHtml(String msg, @NotNull Map<String, Graemlin> graemlinKeystrokeToGraemlin, GraemlinType limitToGraemlinType) {
        if (IPStringUtil.isEmpty(msg)) {
            return msg;
        }

        // bl: switched this to use the new MessageContentExtractor.  the purpose is to first
        // remove all HTML comments from the message.  then we can run the filter
        MessageContentExtractor messageContentExtractor = new MessageContentExtractor(msg, HTML_COMMENT_PATTERN, OPEN_HTML_COMMENT, CLOSE_HTML_COMMENT, MessageContentExtractor.MessageContentExtractorType.GRAEMLINS);
        messageContentExtractor.doExtractContent();
        msg = messageContentExtractor.getMessage();
        for (Graemlin graemlin : graemlinKeystrokeToGraemlin.values()) {
            // jw: if requested, skip any graemlins not of the requested type.
            if (limitToGraemlinType != null && limitToGraemlinType != graemlin.getType()) {
                continue;
            }
            String replacement = graemlin.getReplacement();
            Matcher matcher = graemlin.getKeystrokePattern().matcher(msg);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                int start = matcher.start();
                int startOfMailto = start - MAILTO_STRING.length();
                // bl: special case for mailto:p@narrative.org - don't want the :p to turn into
                // a tongue face.  to prevent that, inspect any matches to see if they are preceeded
                // by mailto.  if they are, then don't do a replacement for this match.
                if (startOfMailto >= 0 && MAILTO_STRING.equals(msg.substring(startOfMailto, start))) {
                    // just append the text
                    matcher.appendReplacement(sb, "$0");
                } else {
                    matcher.appendReplacement(sb, replacement);
                }
            }
            matcher.appendTail(sb);
            msg = sb.toString();
        }
        messageContentExtractor.setMessage(msg);
        messageContentExtractor.doMergeExtractedContentIntoMessage();
        return messageContentExtractor.getMessage();
    }
}
