package org.narrative.network.core.area.base.services;

import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.TimeZoneWrapper;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.base.AreaRlm;
import org.narrative.network.core.area.base.AreaStats;
import org.narrative.network.core.area.portfolio.Portfolio;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.settings.global.services.translations.DefaultLocale;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.AddUsersToAreaCircle;
import org.narrative.network.core.user.services.CreateNewUser;
import org.narrative.network.core.user.services.UsernameUtils;
import org.narrative.network.core.user.services.preferences.FormatPreferences;
import org.narrative.network.customizations.narrative.niches.NarrativeCircleType;
import org.narrative.network.customizations.narrative.niches.services.EnableNichesCustomization;
import org.narrative.network.customizations.narrative.service.impl.user.RegisterUserTask;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Locale;
import java.util.TimeZone;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 21, 2005
 * Time: 11:25:52 AM
 */
public class CreateArea extends GlobalTaskImpl<Area> {
    private final String areaName;

    private Timestamp creationDatetime;

    private String emailAddress;
    private boolean emailVerified;
    private String password;
    private String displayName;
    private Locale locale;
    private TimeZone timeZone;
    private boolean sendNewUserEmail = true;

    public CreateArea(String areaName) {
        assert !IPStringUtil.isEmpty(areaName) : "Must supply area name when creating a new area!";

        this.areaName = areaName;
    }

    protected Area doMonitoredTask() {

        //create the area
        final Area area = new Area(areaName);

        if (creationDatetime != null) {
            area.setCreationDate(creationDatetime);
        }

        // save the area now to get an OID
        Area.dao().save(area);

        getNetworkContext().doAreaTask(area, new AreaTaskImpl<Object>() {
            protected Object doMonitoredTask() {

                //create the area rlm
                final AreaRlm areaRlm = new AreaRlm(area);

                AreaRlm.dao().save(areaRlm);

                // bl: once the AreaRlm has been saved, set the default Portfolio
                areaRlm.setDefaultPortfolio(new Portfolio(areaRlm));
                areaRlm.getPortfolios().add(areaRlm.getDefaultPortfolio());

                // JW: due to order of operations we need to save the default portfolio here, so that its in the DB for when
                //     we save the owner AreaUserRlm below.
                Portfolio.dao().save(areaRlm.getDefaultPortfolio());

                area.addDefaultAreaResource();

                // jw: before we create the admin, let's first enable the Narrative Customization.
                // note: this will be changing as part of Mark's changes for #190
                // jw: then, run the task to setup the appropriate values for the narrative features. Now, with this
                //     done, we can safely create the admin user since the managed permission circles (suggesters and
                //     bidders) will have been created.
                getAreaContext().doAreaTask(new EnableNichesCustomization());

                // bl: flush to make sure Area is in the database before we create the user
                PartitionType.flushAllOpenSessionsForCurrentPartitionGroup();

                // jw: setup the owner
                assert !isEmpty(emailAddress) : "Should always specify an email address!";
                CreateNewUser createNewUserTask = new CreateNewUser(password, displayName, UsernameUtils.getIdealUsername(displayName), emailAddress, emailVerified);
                createNewUserTask.setAuthZone(area.getAuthZone());
                createNewUserTask.setFormatPreferences(new FormatPreferences(locale == null ? DefaultLocale.getDefaultLocale() : locale, null, timeZone == null ? TimeZoneWrapper.DEFAULT_TIME_ZONE : timeZone));
                User user = getNetworkContext().doGlobalTask(createNewUserTask);
                // bl: mark user as having agreed to the TOS for new narrative installs
                user.getUserFields().setHasUserAgreedToTos(true);

                if (sendNewUserEmail) {
                    RegisterUserTask.sendNewUserEmail(user);
                }

                // bl: add the owner to the Tribunal and Narrative Staff by default to streamline onboarding
                getAreaContext().doAreaTask(new AddUsersToAreaCircle(NarrativeCircleType.TRIBUNAL.getCircle(getAreaContext().getAuthZone()), Collections.singleton(user)));
                getAreaContext().doAreaTask(new AddUsersToAreaCircle(NarrativeCircleType.NARRATIVE_STAFF.getCircle(getAreaContext().getAuthZone()), Collections.singleton(user)));

                return null;
            }
        });

        PartitionType.flushAllOpenSessionsForCurrentPartitionGroup();

        //Need to make sure the owner is counted as a member if this is a Group
        AreaStats.dao().getLocked(area.getOid()).updateMemberCount();

        return area;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public void setSendNewUserEmail(boolean sendNewUserEmail) {
        this.sendNewUserEmail = sendNewUserEmail;
    }

    public void setCreationDatetime(Timestamp creationDatetime) {
        this.creationDatetime = creationDatetime;
    }
}
