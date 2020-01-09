package org.narrative.network.core.user.services.preferences;

import org.narrative.common.util.IPDateUtil;
import org.narrative.network.customizations.narrative.service.api.model.filter.QualityFilter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import java.sql.Timestamp;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Mar 1, 2006
 * Time: 7:31:27 PM
 *
 * @author Brian
 */
@Embeddable
public class UserPreferences {

    private boolean isSuspendAllEmails;

    private Timestamp lastDeactivationDatetime;

    private QualityFilter contentQualityFilter;
    private boolean displayAgeRestrictedContent;
    private boolean hideMyFollowers;
    private boolean hideMyFollows;

    /**
     * @deprecated for hibernate use only
     */
    public UserPreferences() {}

    public UserPreferences(boolean init) {
        if (init) {
            isSuspendAllEmails = false;

            contentQualityFilter = QualityFilter.DEFAULT_FILTER;
            // default to true so that once you get certified, you will see age restricted content, assuming you are 18+
            displayAgeRestrictedContent = true;
        }
    }

    @NotNull
    public boolean isSuspendAllEmails() {
        return isSuspendAllEmails;
    }

    public void setSuspendAllEmails(boolean suspendAllEmails) {
        isSuspendAllEmails = suspendAllEmails;
    }

    public Timestamp getLastDeactivationDatetime() {
        return lastDeactivationDatetime;
    }

    public void setLastDeactivationDatetime(Timestamp lastDeactivationDatetime) {
        this.lastDeactivationDatetime = lastDeactivationDatetime;
    }

    private transient Timestamp deactivationAvailableDatetime;
    private static final long MINIMUM_TIME_BETWEEN_DEACTIVATIONS = IPDateUtil.DAY_IN_MS * 30;

    @Transient
    public Timestamp getDeactivationAvailableDatetime() {
        if (getLastDeactivationDatetime() == null) {
            return null;
        }

        if (deactivationAvailableDatetime == null) {
            deactivationAvailableDatetime = new Timestamp(getLastDeactivationDatetime().getTime() + MINIMUM_TIME_BETWEEN_DEACTIVATIONS);
        }

        return deactivationAvailableDatetime;
    }

    @Transient
    public boolean isCanDeactivateAccount() {
        Timestamp availableDatetime = getDeactivationAvailableDatetime();

        // jw: the user can only deactivate their account if there is no availableDatetime, or it has already passed.
        return availableDatetime == null || availableDatetime.before(now());
    }

    @Column(columnDefinition = "tinyint", nullable = false)
    public QualityFilter getContentQualityFilter() {
        return contentQualityFilter;
    }

    public void setContentQualityFilter(QualityFilter contentQualityFilter) {
        this.contentQualityFilter = contentQualityFilter;
    }

    public boolean isDisplayAgeRestrictedContent() {
        return displayAgeRestrictedContent;
    }

    public void setDisplayAgeRestrictedContent(boolean displayAgeRestrictedContent) {
        this.displayAgeRestrictedContent = displayAgeRestrictedContent;
    }

    public boolean isHideMyFollowers() {
        return hideMyFollowers;
    }

    public void setHideMyFollowers(boolean hideMyFollowers) {
        this.hideMyFollowers = hideMyFollowers;
    }

    public boolean isHideMyFollows() {
        return hideMyFollows;
    }

    public void setHideMyFollows(boolean hideMyFollows) {
        this.hideMyFollows = hideMyFollows;
    }
}
