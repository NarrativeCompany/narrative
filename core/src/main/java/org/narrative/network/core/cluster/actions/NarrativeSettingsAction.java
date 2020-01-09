package org.narrative.network.core.cluster.actions;

import org.narrative.common.util.UrlUtil;
import org.narrative.common.web.HttpMethodType;
import org.narrative.common.web.struts.MethodDetails;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.settings.area.community.advanced.SandboxedCommunitySettings;
import org.narrative.network.customizations.narrative.neo.services.NeoUtils;
import org.narrative.network.shared.services.ConfirmationMessage;
import org.narrative.network.shared.struts.NetworkResponses;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2020-01-03
 * Time: 10:34
 *
 * @author jonmark
 */
public class NarrativeSettingsAction extends ClusterAction {
    public static final String ACTION_NAME = "narrative-settings";
    public static final String FULL_ACTION_PATH = "/"+ ACTION_NAME;

    public static final String FIAT_PAYMENTS_ENABLED_PARAM = "fiatPaymentsEnabled";
    public static final String SHUTDOWN_NOTICE_URL_PARAM = "shutdownNoticeUrl";
    public static final String NRVE_SCRIPT_HASH_PARAM = "nrveScriptHash";

    private boolean fiatPaymentsEnabled;
    private String shutdownNoticeUrl;
    private String nrveScriptHash;


    @Override
    public void validate() {
        NeoUtils.validateNeoScriptHash(this, NRVE_SCRIPT_HASH_PARAM, "NRVE Script Hash", nrveScriptHash);

        if(!isEmpty(shutdownNoticeUrl) && !UrlUtil.isUrlValid(shutdownNoticeUrl)) {
            addInvalidFieldError(SHUTDOWN_NOTICE_URL_PARAM, "Shutdown Notice URL");
        }
    }

    @Override
    @MethodDetails(httpMethodType = HttpMethodType.GET)
    public String input() throws Exception {
        SandboxedCommunitySettings settings = Area.getAreaRlm(Area.dao().getNarrativePlatformArea()).getSandboxedCommunitySettings();

        nrveScriptHash = settings.getNrveScriptHash();
        fiatPaymentsEnabled = settings.isFiatPaymentsEnabled();
        shutdownNoticeUrl = settings.getShutdownNoticeUrl();

        return INPUT;
    }

    @Override
    @MethodDetails(httpMethodType = HttpMethodType.POST)
    public String execute() throws Exception {
        SandboxedCommunitySettings settings = Area.getAreaRlm(Area.dao().getNarrativePlatformArea()).getSandboxedCommunitySettings();

        settings.setNrveScriptHash(nrveScriptHash);
        settings.setFiatPaymentsEnabled(fiatPaymentsEnabled);
        settings.setShutdownNoticeUrl(shutdownNoticeUrl);

        setConfirmationMessageForSubsequentRequest(new ConfirmationMessage("Settings Saved.", true));
        return NetworkResponses.redirectResponse();
    }

    public boolean isFiatPaymentsEnabled() {
        return fiatPaymentsEnabled;
    }

    public void setFiatPaymentsEnabled(boolean fiatPaymentsEnabled) {
        this.fiatPaymentsEnabled = fiatPaymentsEnabled;
    }

    public String getShutdownNoticeUrl() {
        return shutdownNoticeUrl;
    }

    public void setShutdownNoticeUrl(String shutdownNoticeUrl) {
        this.shutdownNoticeUrl = shutdownNoticeUrl;
    }

    public String getNrveScriptHash() {
        return nrveScriptHash;
    }

    public void setNrveScriptHash(String nrveScriptHash) {
        this.nrveScriptHash = nrveScriptHash;
    }

    @Override
    public Object getSubMenuResource() {
        return ACTION_NAME;
    }
}
