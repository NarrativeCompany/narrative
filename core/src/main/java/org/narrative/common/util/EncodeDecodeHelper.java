package org.narrative.common.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 3/6/11
 * Time: 10:12 AM
 *
 * @author brian
 */
public class EncodeDecodeHelper {
    private final Map<Pattern, String> encodePatterns;
    private final Map<Pattern, String> decodePatterns;

    public EncodeDecodeHelper(String encodeStr, String encodeReplacement, Map<String, String> stringToReplacement) {
        encodePatterns = newLinkedHashMap(stringToReplacement.size() + 1);
        decodePatterns = newLinkedHashMap(stringToReplacement.size() + 1);

        // bl: for encoding, we first encode the basic encode string, and then do all of the rest
        encodePatterns.put(Pattern.compile(encodeStr, Pattern.LITERAL), Matcher.quoteReplacement(encodeReplacement));
        for (Map.Entry<String, String> entry : stringToReplacement.entrySet()) {
            String str = entry.getKey();
            String replacement = entry.getValue();
            encodePatterns.put(Pattern.compile(str, Pattern.LITERAL), Matcher.quoteReplacement(replacement));
        }

        // bl: for decoding, we decode in the reverse order
        List<String> order = newArrayList(stringToReplacement.keySet());
        Collections.reverse(order);
        for (String str : order) {
            String replacement = stringToReplacement.get(str);
            decodePatterns.put(Pattern.compile(replacement, Pattern.LITERAL), Matcher.quoteReplacement(str));
        }
        decodePatterns.put(Pattern.compile(encodeReplacement, Pattern.LITERAL), Matcher.quoteReplacement(encodeStr));
    }

    public String encode(String str) {
        return doReplace(str, encodePatterns);
    }

    public String decode(String str) {
        return doReplace(str, decodePatterns);
    }

    private String doReplace(String str, Map<Pattern, String> map) {
        for (Map.Entry<Pattern, String> entry : map.entrySet()) {
            str = entry.getKey().matcher(str).replaceAll(entry.getValue());
        }
        return str;
    }
}
