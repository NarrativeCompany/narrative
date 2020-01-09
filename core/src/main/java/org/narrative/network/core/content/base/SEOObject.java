package org.narrative.network.core.content.base;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.InvalidParamError;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Date: 4/15/13
 * Time: 1:37 PM
 *
 * @author brian
 */
public interface SEOObject {
    String FIELD__PRETTY_URL_STRING = "prettyUrlString";
    String SEO_ID_PREFIX = "id_";

    String getPrettyUrlString();

    String getDisplayUrl();

    String getPermalinkUrl();

    String getIdForUrl();

    static Pair<OID, String> parseUnknownId(String unknownId, String resourceName) {
        // jw: if we did not get a unknownId, lets return null as a dead giveaway to the caller that it could not be parsed.
        if (StringUtils.isEmpty(unknownId)) {
            return null;
        }

        // jw: if it starts with the expected prefix, let's return a string ID of everything after the prefix.
        if (unknownId.startsWith(SEO_ID_PREFIX)) {
            return Pair.of(null, unknownId.substring(SEO_ID_PREFIX.length()));
        }

        // jw: this will throw a number format exception if the string is not of the expected value.
        try {
            return Pair.of(OID.valueOf(unknownId), null);
        } catch (NumberFormatException nfe) {
            throw new InvalidParamError(resourceName, unknownId);
        }
    }
}
