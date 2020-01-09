package org.narrative.common.util.posting;

import org.narrative.common.util.IPHTMLUtil;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.MailUtil;
import org.narrative.common.util.html.HTMLEnforcer;
import org.narrative.network.core.master.graemlins.Graemlin;
import org.narrative.network.core.system.NetworkRegistry;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Post a test message like this to test:
 * [b]ubb code bold[/b]
 * <p>
 * ubbcode image [img]http://localhost/common/cp/images/emoticons/logo.gif[/img]
 * <p>
 * ;) smilies
 *
 * <b>bold</b>
 *
 * <script>scripttagremoved</script>
 * <p>
 * Date: Feb 22, 2006
 * Time: 9:04:57 AM
 *
 * @author Brian
 */
public class MessageTextMassager {
    private boolean isConvertLfToBr;
    private boolean isHtmlEnabled;
    private final boolean isUbbCodeEnabled;
    private boolean isGraemlinsEnabled;
    private final boolean isAllowAnyHtml;
    private final Formattable formattable;
    private final boolean areAllBodyElementsSupported;
    // todo: come up with a better way to specify the graemlins
    private final Map<String, Graemlin> graemlinKeystrokeToReplacementHtml;

    private Map<String, Collection<String>> supportedElementNamesToAttributes;

    public MessageTextMassager(Formattable formattable, Map<String, Graemlin> graemlinKeystrokeToReplacementHtml, boolean areAllBodyElementsSupported) {
        this(formattable, graemlinKeystrokeToReplacementHtml, areAllBodyElementsSupported, false);
    }

    public MessageTextMassager(Formattable formattable, Map<String, Graemlin> graemlinKeystrokeToReplacementHtml, boolean areAllBodyElementsSupported, boolean forceIsAllowAnyHtml) {
        this.formattable = formattable;
        this.areAllBodyElementsSupported = areAllBodyElementsSupported;
        // only convert lf to br if not using the WYSIWYG editor
        // bl: now, we just assume everything uses the WYSIWYG editor and submits raw HTML posts.
        // because of that, we don't ever want to convert line feeds to BR elements anymore.
        isConvertLfToBr = false;
        // bl: always enable html, ubb code, and graemlins
        isHtmlEnabled = true;
        // bl: UBB Code is now disabled by default. we'll only enable it when we actually want it, such as during imports
        // from other platforms that use BB/UBB Code.
        // to generically support massaging during imports, we'll only set this to true when importing.
        isUbbCodeEnabled = NetworkRegistry.getInstance().isImporting();
        isGraemlinsEnabled = true;
        if (forceIsAllowAnyHtml) {
            isAllowAnyHtml = true;
        } else {
            // bl: we'll now allow any HTML and JavaScript for users who have permission to do so.
            // bl: ignore for imports, so that they are unaffected by this.
            // jw: for Narrative, let's never allow any HTML or javascript, regardless of the roles permissions
            //isAllowAnyHtml = !NetworkRegistry.getInstance().isImporting() && areaContext().getAreaRole().isAllowAnyHtmlJavascript();
            isAllowAnyHtml = false;
        }
        this.graemlinKeystrokeToReplacementHtml = graemlinKeystrokeToReplacementHtml;
    }

    public void massageBody() {
        String ret = formattable.getBody();

        if (IPStringUtil.isEmpty(ret)) {
            return;
        }

        //convert to html
        MessageContentExtractor messageContentExtractor = null;
        if (isUbbCodeEnabled) {
            messageContentExtractor = UbbCodeTextMassager.doSeparateCodeTags(ret, true);
            ret = messageContentExtractor.getMessage();
        }

        // jw: lets parse out the contents of all "pre" tags
        CodeContentExtractor codeExtractor = new CodeContentExtractor(ret);
        // jw: extract the contents of the pre tag
        codeExtractor.doExtractContent();
        // jw: get the new message body!
        ret = codeExtractor.getMessage();

        // bl: need to prevent &nbsp; from appearing 10 times in a row.
        // need to have this separate from the convertConsecutiveSpacesToNbsp since the user may submit the message
        // with &nbsp; already in the message (e.g. as TinyMCE does), in which case we may not even evaluate converting
        // consecutive spaces to &nbsp; (if WYSIWYG is enabled).
        ret = HtmlTextMassager.stripLongConsecutiveNbspStrings(ret);

        // bl: have to convert lf to br prior to escaping unsupported HTML or else newlines and brs will be lost.
        // darn, I owe brett $5 bucks.  put in the <BR> tags regardless
        // of whether UBBCode or HTML is enabled.
        if (isConvertLfToBr) {
            ret = HtmlTextMassager.convertCrAndLfToHtml(ret);
        }

        // sb: we also need to strip conditional comments from user input, since this can distort the way
        // things look in Internet Explorer.
        // jw: need to do this before the spaces get turned into &nbsp;
        if (isHtmlEnabled) {
            ret = HtmlTextMassager.stripConditionalComments(ret);
        }

        // bl: also, convert multiple consecutive spaces into a single space followed by &nbsp;
        // piggy-backing off of convert lf to br.  no need to have a separate setting for this, i don't think.
        // essentially, the setting just indicates that the user is using a plain text editor (textarea)
        // as opposed to the WYSIWYG editor.
        if (isConvertLfToBr) {
            ret = HtmlTextMassager.convertConsecutiveSpacesToNbsp(ret);
        }

        // convert any html entities to their ascii equivalent.
        ret = IPHTMLUtil.getDeHTMLEntityizedString(ret, true);

        if (isUbbCodeEnabled) {
            // bl: prior to converting ubbcode to html, let's escape any html entities from the message.
            // why?  well, it's possible that a user might encode a " as &#34; and that would cause a javascript
            // vulnerability.
            // ubbcode hack format: [URL=http://null&#34;onmouseover=&#34;location='http://thriveserver/dotx/ubb.x?a=tpc&s=2001&f=8401002&m=900102541&cookie='+cookie;]mouse over me[/URL]

            // JW:Code - remove the old call to escape the HTML in Code tags
            //ret = Message.getInstance().doEscapeHTMLInCODETags(message_body);
            ret = UbbCodeTextMassager.doUbbCodeToHtml(ret);

            ret = UbbCodeTextMassager.doUbbCodeImagesToHtml(ret);

            ret = UbbCodeTextMassager.doUbbCodeColorsToHtml(ret);
        }

        if (isHtmlEnabled) {
            // no need to strip elements if allowing any HTML
            if (!isAllowAnyHtml) {
                // if html is enabled, let's go ahead and remove any unsupported HTML elements up front.
                // nb. don't have to worry about code tag contents removed above since those will be
                // de-htmlized before they are re-inserted.
                // nb. don't have to worry about ubbcode either, as we trust any ubbcode->html conversions that we do.
                HTMLEnforcer.Options options = new HTMLEnforcer.Options(isGraemlinsEnabled ? graemlinKeystrokeToReplacementHtml : null);
                if (!isEmptyOrNull(supportedElementNamesToAttributes)) {
                    ret = HtmlTextMassager.escapeUnsupportedHtml(ret, supportedElementNamesToAttributes, options);
                } else {
                    ret = HtmlTextMassager.escapeUnsupportedHtml(ret, areAllBodyElementsSupported, options);
                }
            }

            ret = AnchorTextMassager.applyAutoLinkAnchorReplacements(ret);
        } else {
            ret = AnchorTextMassager.applyAutoLinkAnchorReplacements(ret);

            // now do graemlins. since HTML is not enabled, we don't have to worry about graemlins in HTML attributes.
            if (isGraemlinsEnabled) {
                ret = GraemlinMassager.doGraemlinsToHtml(ret, graemlinKeystrokeToReplacementHtml, null);
            }
        }

        // kill any security vulnerabilities in the text.
        if (!isAllowAnyHtml) {
            ret = killAnySecurityVulnerabilitiesInText(ret);
        }

        if (isUbbCodeEnabled) {
            // The Anchor tag replacements handle any security for preventing javascript from running so we will convert them after
            // the security filters from now on.
            ret = AnchorTextMassager.applyUbbCodeAnchorReplacements(ret);
        }

        ret = linkifyEmailAddresses(ret);

        // jw: sanitize the code contents and add them back into the HTML
        codeExtractor.setMessage(ret);
        codeExtractor.sanitize(isAllowAnyHtml);
        ret = codeExtractor.doMergeExtractedContentIntoMessage();

        // last step of ubb code is to merge the code tag contents back into the message body
        if (isUbbCodeEnabled) {
            messageContentExtractor.setMessage(ret);

            // we've already removed security vulnerabilities from the body.  now let's remove security vulnerabilities
            // from the contents of the code tags.
            messageContentExtractor.sanitize(isAllowAnyHtml);

            // merge the now secure code contents back into the message body.
            ret = messageContentExtractor.doMergeExtractedContentIntoMessage();
        }

        // bl: remove any whitespace from the beginning and end of posts.
        // nb. run iteratively until the message is unchanged.
        ret = trimWhitespace(ret);

        // set the body.
        formattable.setBody(ret);
    }

    public void setConvertLfToBr(boolean convertLfToBr) {
        isConvertLfToBr = convertLfToBr;
    }

    public void setHtmlEnabled(boolean htmlEnabled) {
        isHtmlEnabled = htmlEnabled;
    }

    public void setGraemlinsEnabled(boolean graemlinsEnabled) {
        isGraemlinsEnabled = graemlinsEnabled;
    }

    private static String trimWhitespace(String ret) {
        while (true) {
            String original = ret;
            ret = ret.trim();
            ret = HtmlTextMassager.trimHtmlWhitespace(ret);
            // bl: keep doing this until we didn't make any changes
            if (ret.equals(original)) {
                break;
            }
        }
        return ret;
    }

    public static String killAnySecurityVulnerabilitiesInText(String ret) {
        // kill script tags even when html is enabled

        // convert javascript and other baddies written as html entities
        // back to regular text
        ret = IPHTMLUtil.getDeHTMLEntityizedString(ret, true);

        // bl: they may have used URL encoded chars in their HTML tags to do harmful things.  decode those vals so we can
        // catch them in the script tag/function killing.
        // jw: let's no longer perform this replacement... (, a-z, A-Z should be left alone.
        //ret = IPHTMLUtil.decodeURLEncodedCharactersInHTMLTags(ret);

        // bastards.  they might have used URL encoded chars to write html entities.  so we have to call one of these
        // guys twice no matter what.  d'oh.
        // jw: since we are no longer decoding % encoded characters above there is no reason to run this again
        //ret = IPHTMLUtil.getDeHTMLEntityizedString(ret, true);

        ret = HtmlTextMassager.killScriptTags(ret);
        ret = HtmlTextMassager.killScriptFunctions(ret);
        return ret;
    }

    public static String enforceHtmlRules(String message) {
        HTMLEnforcer.Options options = new HTMLEnforcer.Options();
        message = HtmlTextMassager.escapeUnsupportedHtml(message, true, options);
        message = killAnySecurityVulnerabilitiesInText(message);
        return message;
    }

    /**
     * massage text for a "basic" textarea.  for now, this just means converting
     * newlines to BRs, multiple spaces to &nbsp; and auto-linking URLs
     *
     * @param text the text to massage
     * @param useParagraphs set to true if you want to use paragraphs instead of br elements
     * @return the massaged text
     */
    public static String getMassagedTextForBasicTextarea(String text, boolean useParagraphs) {
        if (IPStringUtil.isEmpty(text)) {
            return text;
        }
        text = HtmlTextMassager.disableHtml(text);
        if(useParagraphs) {
            text = HtmlTextMassager.convertCrAndLfToParagraphs(text);
        } else {
            text = HtmlTextMassager.convertCrAndLfToHtml(text);
        }
        text = HtmlTextMassager.convertConsecutiveSpacesToNbsp(text);
        text = AnchorTextMassager.applyAutoLinkAnchorReplacements(text);
        // finally, trim out any whitespace (including HTML whitespace).
        text = trimWhitespace(text);
        return text;
    }

    /**
     * reverse the massaging done in getMassagedTextForBasicTextarea().
     * de-HTML-ize links, convert BRs to newlines, and convert &nbsp; to space.
     *
     * @param text the text to unmassage
     * @param useParagraphs set to true if the source body is using paragraphs instead of br elements
     * @return the unmassaged text
     */
    public static String getUnmassagedTextForBasicTextareaEdit(String text, boolean useParagraphs) {
        if (IPStringUtil.isEmpty(text)) {
            return text;
        }
        text = AnchorTextMassager.doAnchorReplacementsToPlainText(text);
        text = HtmlTextMassager.convertNbspToSpace(text);
        if(useParagraphs) {
            text = HtmlTextMassager.convertParagraphTagsToCrLf(text);
        } else {
            text = HtmlTextMassager.convertBrAndParagraphTagsToCrLf(text);
        }
        text = HtmlTextMassager.enableDisabledHtml(text);
        return text;
    }

    /**
     * This regex will do the following:
     * - Ensure that the email address does not start with >, ", :, or =, as this would indicate it already
     * is wrapped within HTML.
     * - Ensure that the local (username) portion contains only a-z, 0-9, ., +, -, and _.
     * - Ensure the domain portion only contains a-z, 0-9, ., or -.
     * - Ensure the domain ends with an extension containing on a-z and that is at least two-character.
     */
    private static final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile("[a-zA-Z0-9\\.\\+\\-_]+@[a-zA-Z0-9\\.\\-]+\\.[a-zA-Z]{2,}");

    public static String linkifyEmailAddresses(final String content) {
        if (IPStringUtil.isEmpty(content)) {
            return content;
        }

        StringBuffer sb = new StringBuffer();
        Matcher matcher = EMAIL_ADDRESS_PATTERN.matcher(content);

        while (matcher.find()) {
            String email = matcher.group();
            if (MailUtil.isEmailAddressValid(email)) {
                int start = matcher.start();
                String previousCharacter = "";
                if (start > 0 && start <= content.length() - 1) {
                    previousCharacter = content.substring(start - 1, start);
                }

                // we're looking for the colon because of mailto:, quote because of entity attributes, and greater
                // than because of entity values. if these match, then do not linkify.
                if (":".equals(previousCharacter) || ">".equals(previousCharacter) || "\"".equals(previousCharacter)) {
                    matcher.appendReplacement(sb, email);
                } else {
                    matcher.appendReplacement(sb, "<a href=\"mailto:" + email + "\">" + email + "</a>");
                }
            } else {
                matcher.appendReplacement(sb, email);
            }
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    public void setSupportedElementNamesToAttributes(Map<String, Collection<String>> supportedElementNamesToAttributes) {
        this.supportedElementNamesToAttributes = supportedElementNamesToAttributes;
    }
}

