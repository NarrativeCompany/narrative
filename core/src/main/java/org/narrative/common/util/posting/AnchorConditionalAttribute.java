package org.narrative.common.util.posting;

import java.util.Map;

/**
 * Date: 4/5/16
 * Time: 9:24 AM
 *
 * @author brian
 */
public interface AnchorConditionalAttribute {
    boolean appliesTo(String url);

    Map<String, String> getAttributes();
}
