package org.narrative.network.customizations.narrative.publications;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Date: 9/26/19
 * Time: 8:28 PM
 *
 * @author brian
 */
public class PublicationUrlTypeTest {

    @Test
    void isDoesUrlMatch_SocialURLs_MatchesProperly() {
        Map<String,PublicationUrlType> tests = new LinkedHashMap<>();
        tests.put("https://twitter.com/me", PublicationUrlType.TWITTER);
        tests.put("https://www.twitter.com/me", PublicationUrlType.TWITTER);
        tests.put("https://facebook.com/me", PublicationUrlType.FACEBOOK);
        tests.put("https://www.facebook.com/me", PublicationUrlType.FACEBOOK);
        tests.put("https://instagram.com/me", PublicationUrlType.INSTAGRAM);
        tests.put("https://www.instagram.com/me", PublicationUrlType.INSTAGRAM);
        tests.put("https://youtube.com/me", PublicationUrlType.YOUTUBE);
        tests.put("https://www.youtube.com/me", PublicationUrlType.YOUTUBE);
        tests.put("https://snapchat.com/me", PublicationUrlType.SNAPCHAT);
        tests.put("https://www.snapchat.com/me", PublicationUrlType.SNAPCHAT);
        tests.put("https://pinterest.com/me", PublicationUrlType.PINTEREST);
        tests.put("https://www.pinterest.com/me", PublicationUrlType.PINTEREST);
        tests.put("https://linkedin.com/me", PublicationUrlType.LINKED_IN);
        tests.put("https://www.linkedin.com/me", PublicationUrlType.LINKED_IN);

        for (Map.Entry<String, PublicationUrlType> entry : tests.entrySet()) {
            String url = entry.getKey();
            PublicationUrlType expectedType = entry.getValue();
            for (PublicationUrlType type : PublicationUrlType.values()) {
                if(type.isWildcard()) {
                    continue;
                }
                if(expectedType==type) {
                    assertTrue(type.isDoesUrlMatch(url), "URL " + url + " did NOT match type/" + type + "!");
                } else {
                    assertFalse(type.isDoesUrlMatch(url), "URL " + url + " DID match type/" + type + " but shouldn't have!");
                }
            }
        }
    }
}
