package org.narrative.network.shared.context;

import org.narrative.common.web.RequestResponseHandler;
import org.narrative.network.core.settings.global.services.translations.NetworkResourceBundle;
import org.narrative.network.shared.security.PrimaryRole;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 19, 2005
 * Time: 10:53:27 AM
 */
public interface NetworkContextInternal extends NetworkContext {
    public void setResourceBundle(NetworkResourceBundle resourceBundle);

    public void setPrimaryRole(@NotNull PrimaryRole role);

    public void setRequestType(RequestType requestType);

    public void setRequestResponse(RequestResponseHandler reqResp);

    //todo: don't really like this, but we need to really look at how wordlets and formats, etc, are being set up, because they don't work on internal tasks
    public void setupSystemRole();

    public void changeRole(PrimaryRole primaryRole);

    public void setContextData(String key, Object obj);

    public void reassociateCachedContextAfterSessionClear();
}
