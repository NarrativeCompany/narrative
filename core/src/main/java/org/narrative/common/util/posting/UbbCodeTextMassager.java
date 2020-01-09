package org.narrative.common.util.posting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date: Feb 22, 2006
 * Time: 9:06:42 AM
 *
 * @author Brian
 */
public class UbbCodeTextMassager {

    // s/(\[EMAIL\])([^\"\'\`\(\)\s]+?\@[^\"\`\(\)\s]+?)(\[\/EMAIL\])/<A HREF="mailto:$2">$2<\/A>/isg
    private static final Pattern UBBCODE_EMAIL_PATTERN = Pattern.compile("(\\[email\\])([^\\\"\\'\\`\\(\\)\\s]+?\\@[^\\\"\\`\\(\\)\\s]+?)(\\[/email\\])", Pattern.CASE_INSENSITIVE);
    private static final String UBBCODE_EMAIL_REPLACEMENT = "<a href=\"mailto:$2\">$2</a>";

    // s/(\[i\])(.+?)(\[\/i\])/<I>$2<\/I>/isg
    private static final Pattern UBBCODE_ITALICS_PATTERN = Pattern.compile("(\\[i\\])(.+?)(\\[/i\\])", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final String UBBCODE_ITALICS_REPLACEMENT = "<i>$2</i>";

    // s/(\[b\])(.+?)(\[\/b\])/<B>$2<\/B>/isg
    private static final Pattern UBBCODE_BOLD_PATTERN = Pattern.compile("(\\[b\\])(.+?)(\\[/b\\])", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final String UBBCODE_BOLD_REPLACEMENT = "<b>$2</b>";

    // s/(\[strike\])(.+?)(\[\/strike\])/<STRIKE>$2<\/STRIKE>/isg
    private static final Pattern UBBCODE_STRIKETHROUGH_PATTERN = Pattern.compile("(\\[strike\\])(.+?)(\\[/strike\\])", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final String UBBCODE_STRIKETHROUGH_REPLACEMENT = "<strike>$2</strike>";
    
    // todo: css class for HRs?
    // s/(\[hr\])/<hr class="ev_code_hr" \/>/isg
    private static final Pattern UBBCODE_HR_PATTERN = Pattern.compile("(\\[hr\\])", Pattern.CASE_INSENSITIVE);
    private static final String UBBCODE_HR_REPLACEMENT = "<hr />";

    // s/(\[sub\])(.+?)(\[\/sub\])/<sub>$2<\/sub>/isg
    private static final Pattern UBBCODE_SUBSCRIPT_PATTERN = Pattern.compile("(\\[sub\\])(.+?)(\\[/sub\\])", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final String UBBCODE_SUBSCRIPT_REPLACEMENT = "<sub>$2</sub>";

    public static String doUbbCodeToHtml(String message) {
        message = UBBCODE_EMAIL_PATTERN.matcher(message).replaceAll(UBBCODE_EMAIL_REPLACEMENT);
        message = UBBCODE_ITALICS_PATTERN.matcher(message).replaceAll(UBBCODE_ITALICS_REPLACEMENT);
        message = UBBCODE_BOLD_PATTERN.matcher(message).replaceAll(UBBCODE_BOLD_REPLACEMENT);
        message = UBBCODE_STRIKETHROUGH_PATTERN.matcher(message).replaceAll(UBBCODE_STRIKETHROUGH_REPLACEMENT);
        /*message = UBBCODE_LIST_PATTERN.matcher(message).replaceAll(UBBCODE_LIST_REPLACEMENT);
        message = UBBCODE_ORDERED_LIST_PATTERN.matcher(message).replaceAll(UBBCODE_ORDERED_LIST_REPLACEMENT);
        message = UBBCODE_LIST_ITEM_PATTERN.matcher(message).replaceAll(UBBCODE_LIST_ITEM_REPLACEMENT);*/
        message = UBBCODE_HR_PATTERN.matcher(message).replaceAll(UBBCODE_HR_REPLACEMENT);
        message = UBBCODE_SUBSCRIPT_PATTERN.matcher(message).replaceAll(UBBCODE_SUBSCRIPT_REPLACEMENT);

        message = UbbCodeTextMassager.doUbbCodeQuoteTagToHtml(message);

        return message;
    }

    // bl: changed so that we just strip out the javascript and vbscript img patterns.
    // s/(\[IMG\])(\s*?)(javascript)(\s*?)(:)(.+?)(\[\/IMG\])/<IMG SRC="\/common\/emoticons\icon_redface.gif">/isg
    private static final Pattern UBBCODE_IMAGE_NO_JAVASCRIPT_PATTERN = Pattern.compile("(\\[img\\])(\\s*?)(javascript)(\\s*?)(:)(.+?)(\\[/img\\])", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final String UBBCODE_IMAGE_NO_JAVASCRIPT_REPLACEMENT = "";

    // bl: changed so that we just strip out the javascript and vbscript img patterns.
    // s/(\[IMG\])(\s*?)(vbscript)(\s*?)(:)(.+?)(\[\/IMG\])/<IMG SRC="\/common\/emoticons\icon_redface.gif">/isg
    private static final Pattern UBBCODE_IMAGE_NO_VBSCRIPT_PATTERN = Pattern.compile("(\\[img\\])(\\s*?)(vbscript)(\\s*?)(:)(.+?)(\\[/img\\])", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final String UBBCODE_IMAGE_NO_VBSCRIPT_REPLACEMENT = "";

    // s/(\[IMG\])([^\"\`\(\)\s]+?)(\[\/IMG\])/<IMG SRC="$2"> /isg
    private static final Pattern UBBCODE_IMAGE_PATTERN = Pattern.compile("(\\[img\\])([^\\\"\\`\\(\\)\\s]+?)(\\[/img\\])", Pattern.CASE_INSENSITIVE);
    private static final String UBBCODE_IMAGE_REPLACEMENT = "<img src=\"$2\"> ";

    // s/(\[IMG:(left|right|top)\])([^\"\`\(\)\s]+?)(\[\/IMG\])/<IMG ALIGN="$2" SRC="$3"> /isg
    private static final Pattern UBBCODE_IMAGE_ALIGN_PATTERN = Pattern.compile("(\\[img:(left|right|top)\\])([^\\\"\\`\\(\\)\\s]+?)(\\[/img\\])", Pattern.CASE_INSENSITIVE);
    private static final String UBBCODE_IMAGE_ALIGN_REPLACEMENT = "<img align=\"$2\" src=\"$3\"> ";

    public static String doUbbCodeImagesToHtml(String message) {
        message = UBBCODE_IMAGE_NO_JAVASCRIPT_PATTERN.matcher(message).replaceAll(UBBCODE_IMAGE_NO_JAVASCRIPT_REPLACEMENT);
        message = UBBCODE_IMAGE_NO_VBSCRIPT_PATTERN.matcher(message).replaceAll(UBBCODE_IMAGE_NO_VBSCRIPT_REPLACEMENT);

        message = UBBCODE_IMAGE_PATTERN.matcher(message).replaceAll(UBBCODE_IMAGE_REPLACEMENT);
        message = UBBCODE_IMAGE_ALIGN_PATTERN.matcher(message).replaceAll(UBBCODE_IMAGE_ALIGN_REPLACEMENT);
        return message;
    }

    private static final String SUPPORTED_UBBCODE_COLORS_PATTERN_GROUP = "(red|green|blue|yellow|white|black|pink|purple|brown|grey)";

    // todo: how to do the colors?  doing just a style color: now.
    // s/(\[color:)(red|green|blue|yellow|white|black|pink|purple|brown|grey)(\])(.+?)(\[\/color\])/<span class="ev_code_$2">$4<\/span>/isg
    private static final Pattern UBBCODE_COLORS_PATTERN = Pattern.compile("(\\[color:)" + SUPPORTED_UBBCODE_COLORS_PATTERN_GROUP + "(\\])(.+?)(\\[/color\\])", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final String UBBCODE_COLORS_REPLACEMENT = "<span style=\"color:$2\">$4</span>";

    public static String doUbbCodeColorsToHtml(String message) {
        message = UBBCODE_COLORS_PATTERN.matcher(message).replaceAll(UBBCODE_COLORS_REPLACEMENT);
        return message;
    }

    // todo: css class for ubbcode quote?
    // s/(\[QUOTE\])(.+?)(\[\/QUOTE\])/<BLOCKQUOTE class="ip-ubbcode-quote"><div class="ip-ubbcode-quote-title">quote:<\/div><div class="ip-ubbcode-quote-content">$2 <\/div><\/BLOCKQUOTE>/isg
    private static final Pattern UBBCODE_QUOTE_PATTERN = Pattern.compile("(\\[quote\\])(.+?)(\\[/quote\\])", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final String UBBCODE_QUOTE_REPLACEMENT = "<blockquote><div>quote:</div><div>$2</div></blockquote>";

    /**
     * this method will recursively replace quote tags in a message to support nested quotes
     */
    private static String doUbbCodeQuoteTagToHtml(String msg) {
        return doUbbCodeQuoteReplacement(msg, UBBCODE_QUOTE_PATTERN, UBBCODE_QUOTE_REPLACEMENT);
    }

    private static String doUbbCodeQuoteReplacement(String msg, Pattern quotePatternToMatch, String replacement) {
        // jonmark says to allow at most 30 replacements.  ok :)
        for (int i = 0; i < 30; i++) {
            Matcher matcher = quotePatternToMatch.matcher(msg);
            // stop as soon as we don't find it.
            if (!matcher.find()) {
                break;
            }
            msg = matcher.replaceAll(replacement);
        }
        return msg;
    }

    // todo: css class for code tags?
    // s/(<pre)( class="[^"]+?")?(>)(.+?)(<\/pre>)/[code]$4[\/code]/isg
    private static final Pattern UBBCODE_CODE_PATTERN = Pattern.compile("(\\[code\\])(.+?)(\\[/code\\])", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    // s/(<BLOCKQUOTE)( class="[^"]+?")?(><font size="[^"]+?")( face="[^"]+?")?(>code:<\/font><HR><pre)( class="[^"]+?")?(>)(.+?)(<\/pre><HR><\/BLOCKQUOTE>)/[code]$8[\/code]/isg
    private static final String HTML_OPEN_PRE = "<pre>";
    private static final String HTML_CLOSE_PRE = "</pre>";
    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("(" + HTML_OPEN_PRE + ")(.+?)(" + HTML_CLOSE_PRE + ")", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final String UBBCODE_OPEN_CODE = "[code]";
    private static final String UBBCODE_CLOSE_CODE = "[/code]";

    /**
     * Routine to convert < and > to &lt; and &gt; inside of a [CODE] tag
     * and separate the body of the code tag out.
     */
    public static MessageContentExtractor doSeparateCodeTags(String msg, boolean isUbbCodeToHtml) {
        // for ubbcode -> html, use <pre> tags for the replacements.
        // for html -> ubbcode, use [code] tags for the replacements.

        MessageContentExtractor ret = new MessageContentExtractor(msg, isUbbCodeToHtml ? UBBCODE_CODE_PATTERN : HTML_CODE_PATTERN, isUbbCodeToHtml ? HTML_OPEN_PRE : UBBCODE_OPEN_CODE, isUbbCodeToHtml ? HTML_CLOSE_PRE : UBBCODE_CLOSE_CODE, MessageContentExtractor.MessageContentExtractorType.UBBCODE_CODE);
        ret.doExtractContent();
        return ret;
    }

}

