package org.narrative.common.util.posting;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 8/7/15
 * Time: 10:20 AM
 */
public class CodeContentExtractor extends MessageContentExtractor {
    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("(<(?:pre|code)\\b[^>]*>)(.+?)(</(?:pre|code)>)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private List<String> extractedOpens = newLinkedList();

    public CodeContentExtractor(String message) {
        // jw: this will escape the code out and then just put it back in, we are not changing the actual container
        super(message, HTML_CODE_PATTERN, null, null, MessageContentExtractorType.PRE_HTML_ELEMENT);
    }

    @Override
    protected void onMatch(Matcher matcher) {
        // jw: to preserve attributes lets get that group
        extractedOpens.add(matcher.group(1));
    }

    @Override
    protected String getOpenReplacement(int index) {
        return extractedOpens.get(index);
    }

    @Override
    public void sanitize(boolean allowAnyHtml) {
        // jw: we will allow HTML to be posted, and will just santize it unless you can post all html (allowAnyHtml)
        setExtractedContents(sanitizeContents(getExtractedContents(), allowAnyHtml, true, false));

        // jw: because we are preserving the attributes in their original state this will ensure that they are sanitized
        extractedOpens = sanitizeContents(extractedOpens, allowAnyHtml, true, false);
    }
}
