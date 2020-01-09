package org.narrative.network.core.composition.base;

import org.narrative.common.persistence.OID;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 3/11/15
 * Time: 4:08 PM
 */
public interface ShareableObject {
    public OID getOid();

    public String getDisplayUrl();

    public String getPermalinkUrl();

}
