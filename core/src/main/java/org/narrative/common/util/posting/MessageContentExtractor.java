package org.narrative.common.util.posting;

import org.narrative.common.util.NarrativeLogger;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Apr 10, 2006
 * Time: 3:15:20 PM
 *
 * @author Brian
 */
public class MessageContentExtractor {

    private static final NarrativeLogger logger = new NarrativeLogger(MessageContentExtractor.class);

    /**
     * the empty replacement string to use.
     * Note: we can't use &lt; or &gt; in the empty replacement or else those will be removed
     * by our HTML stripper since "narrativeExtracted" isn't a supported tag.  I'm opting for a
     * UBBCode format instead.  Shouldn't be any problem with that.
     */

    public static enum MessageContentExtractorType {
        GRAEMLINS,
        UBBCODE_CODE,
        PRE_HTML_ELEMENT;

        private final String replacement;
        private final Pattern replacementPattern;

        private MessageContentExtractorType() {
            this.replacement = "[" + name().toLowerCase() + "/]";
            this.replacementPattern = Pattern.compile(replacement, Pattern.LITERAL);
        }
    }

    private String message;
    private List<String> extractedContents = new LinkedList<String>();
    private final List<String> extractedCloses = new LinkedList<String>();
    private final Pattern patternToExtract;
    private final String openReplacement;
    private final String closeReplacement;
    private final MessageContentExtractorType type;

    /**
     * create an instance of the MessageContentExtractor.
     * the supplied pattern should have 3 groupings, e.g.:
     * (group1)(group2)(group3)
     * upon finding a match, we will replace the matched pattern with an arbitrary string.
     * you can then perform any further operations to the string.  supply the new
     * version of the string to doMergeExtractedContentIntoMessage to have the
     * matched strings replaced back into the string.  here's an example:
     * <p>
     * MessageContentExtractor mce = new MessageContentExtractor(Pattern.compile("(a)(.*)(z)"));
     * String message = mce.doExtractContent("abcdz howdy a2z");
     * ... do stuff to message
     * messsage = mce.doMergeExtractedContentIntoMessage(message);
     *
     * @param patternToExtract the pattern to extract.
     */
    public MessageContentExtractor(String message, Pattern patternToExtract, String openReplacement, String closeReplacement, MessageContentExtractorType type) {
        this.message = message;
        this.patternToExtract = patternToExtract;
        this.openReplacement = openReplacement;
        this.closeReplacement = closeReplacement;
        this.type = type;
    }

    /**
     * Routine to convert < and > to &lt; and &gt; inside of a [CODE] tag
     * and separate the body of the code tag out.
     */
    public void doExtractContent() {
        StringBuffer sb = new StringBuffer();
        Matcher matcher = patternToExtract.matcher(message);
        while (matcher.find()) {
            extractedContents.add(matcher.group(2));
            if (isEmpty(this.closeReplacement)) {
                extractedCloses.add(matcher.group(3));
            }
            matcher.appendReplacement(sb, type.replacement);
            onMatch(matcher);
        }
        matcher.appendTail(sb);
        message = sb.toString();
    }

    protected void onMatch(Matcher matcher) {}

    /**
     * Routine to put the code contents back in the message body.
     */
    public String doMergeExtractedContentIntoMessage() {
        // nothing to do?  just return
        if (extractedContents.isEmpty()) {
            return message;
        }

        int index = 0;
        StringBuffer ret = new StringBuffer();
        Matcher matcher = type.replacementPattern.matcher(message);
        while (matcher.find()) {
            if (index > extractedContents.size() - 1) {
                logger.error("More code tags were found in message body during merge than during separation");
            } else {
                // build the replacement string
                StringBuilder replacement = new StringBuilder();
                // <pre>
                replacement.append(getOpenReplacement(index));
                // code contents
                replacement.append(extractedContents.get(index));
                // </pre>
                if (isEmpty(closeReplacement)) {
                    replacement.append(extractedCloses.get(index));
                } else {
                    replacement.append(closeReplacement);
                }
                String replacementStr = Matcher.quoteReplacement(replacement.toString());
                matcher.appendReplacement(ret, replacementStr);
            }
            index++;
        }
        matcher.appendTail(ret);
        message = ret.toString();

        return message;
    }

    public void sanitize(boolean allowAnyHtml) {
        setExtractedContents(sanitizeContents(getExtractedContents(), allowAnyHtml, false, false));
    }

    protected List<String> sanitizeContents(List<String> extractedContents, boolean allowAnyHtml, boolean allowHtml, boolean disableElementsOnly) {
        List<String> codeContentsAfterRemovingVulnerabilities = newArrayList(extractedContents.size());
        for (String codeContents : extractedContents) {
            // we need to kill security vulnerabilities and disable HTML in the code body
            if (!allowAnyHtml) {
                if (!allowHtml) {
                    if (disableElementsOnly) {
                        // jw: I was having a issue with double escaping of HTML because the WYSIWYG will auto escape
                        //     HTML, so lets just escape < and > just to be safe.  Should have no need to escape &
                        codeContents = HtmlTextMassager.disableLessThanAndGreaterThan(codeContents);

                    } else {
                        codeContents = HtmlTextMassager.disableHtml(codeContents);
                    }
                }
                codeContents = MessageTextMassager.killAnySecurityVulnerabilitiesInText(codeContents);
            }
            codeContentsAfterRemovingVulnerabilities.add(codeContents);
        }

        return codeContentsAfterRemovingVulnerabilities;
    }

    protected String getOpenReplacement(int index) {
        return openReplacement;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getExtractedContents() {
        return extractedContents;
    }

    public void setExtractedContents(List<String> extractedContents) {
        this.extractedContents = extractedContents;
    }
}
