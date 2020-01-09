package org.narrative.network.core.cluster.actions;

import com.opensymphony.xwork2.Preparable;
import org.narrative.common.core.services.interceptors.SubPropertySettable;
import org.narrative.common.web.RequestResponseHandler;
import org.narrative.common.web.struts.AfterPrepare;
import org.narrative.config.StaticConfig;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.core.cluster.services.UserKycList;
import org.narrative.network.core.user.UserKyc;
import org.narrative.network.customizations.narrative.service.api.model.kyc.UserKycStatus;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ManageUserKycStatusAction extends ClusterAction  implements Preparable {
    public static final String ACTION_NAME = "manage-user-kyc-status";
    public static final String FULL_ACTION_PATH = "/" + ACTION_NAME;

    public static final String CRITERIA_PARAM = "criteria";

    private DateTimeFormatter dateTimeFormatter;
    private UserKycList criteria;
    private List<UserKyc> users;

    private int page = 1;

    public static final String PAGE_PARAM = "page";

    @Override
    public void prepare() throws Exception {
        criteria = new UserKycList();
        criteria.setFetchTotalResultCount(true);
        NarrativeProperties narrativeProperties = StaticConfig.getBean(NarrativeProperties.class);
        dateTimeFormatter =  DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss z").withZone(narrativeProperties.getCluster().getKycManagementDisplayZoneId());
    }

    @Override
    public String input() throws Exception {
        // bl: default to Awaiting Metadata when first loading the page.
        if(getCriteria().getKycStatus()==null) {
            getCriteria().setKycStatus(UserKycStatus.AWAITING_METADATA);
        }
        // jw: some of our parameters now require validation, so lets have the criteria handle that for us.
        getCriteria().validate(this, CRITERIA_PARAM);

        if (!hasErrors()) {
            // jw: make sure to use getters here, in case descendants decide to customize behavior.
            getCriteria().doSetPage(getPage());
            users = getNetworkContext().doGlobalTask(getCriteria());
        }

        return INPUT;
    }

    @AfterPrepare
    @SubPropertySettable
    public UserKycList getCriteria() {
        return criteria;
    }

    public void setCriteria(UserKycList criteria) {
        this.criteria = criteria;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    @Override
    public String getSubMenuResource() {
        return ACTION_NAME;
    }

    public String formatInstant(Instant instant) {
        return instant != null ? dateTimeFormatter.format(instant) : "";
    }

    // jw: we need to include any filter parameters into the CSV action, so lets expose our current params
    public Map<String, String[]> getCriteriaParameters() {
        RequestResponseHandler reqResp = getNetworkContext().getReqResp();
        return reqResp.getPrefixedParameterValues(CRITERIA_PARAM+".", null);
    }

    public List<UserKyc> getUsers() {
        return users;
    }
}
