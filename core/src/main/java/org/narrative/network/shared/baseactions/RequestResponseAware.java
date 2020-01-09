package org.narrative.network.shared.baseactions;

import org.narrative.common.web.RequestResponseHandler;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 6, 2005
 * Time: 1:55:34 AM
 */

public interface RequestResponseAware {
    public void setRequestResponse(RequestResponseHandler reqResp);
}
