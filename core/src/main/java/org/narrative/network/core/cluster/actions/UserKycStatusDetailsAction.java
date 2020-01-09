package org.narrative.network.core.cluster.actions;

import com.google.common.collect.Lists;
import com.opensymphony.xwork2.Preparable;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.web.HttpRequestType;
import org.narrative.common.web.struts.MethodDetails;
import org.narrative.config.StaticConfig;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.core.user.UserKyc;
import org.narrative.network.core.user.UserKycEvent;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

public class UserKycStatusDetailsAction extends ClusterAction implements Preparable {
    public static final String ACTION_NAME = "user-kyc-status-details";
    public static final String FULL_ACTION_PATH = "/"+ ACTION_NAME;

    private DateTimeFormatter dateTimeFormatter;
    private UserKyc userKyc;
    private List<UserKycEvent> events;

    @Override
    public void prepare() throws Exception {
        NarrativeProperties narrativeProperties = StaticConfig.getBean(NarrativeProperties.class);
        dateTimeFormatter =  DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss z").withZone(narrativeProperties.getCluster().getKycManagementDisplayZoneId());
    }

    @Override
    public void checkRightAfterParams() {
        if (!exists(getUserKyc())) {
            throw UnexpectedError.getRuntimeException("Should always have a userKyc at this point!");
        }

        events = Lists.reverse(userKyc.getEvents());
    }

    @Override
    @MethodDetails(requestType = HttpRequestType.AJAX)
    public String input() throws Exception {
        return INPUT;
    }

    public String formatInstant(Instant instant) {
        return instant != null ? dateTimeFormatter.format(instant) : "";
    }

    @Override
    public String getSubMenuResource() {
        return ACTION_NAME;
    }

    public UserKyc getUserKyc() {
        return userKyc;
    }

    public void setUserKyc(UserKyc userKyc) {
        this.userKyc = userKyc;
    }

    public List<UserKycEvent> getEvents() {
        return events;
    }

    public void setEvents(List<UserKycEvent> events) {
        this.events = events;
    }
}
