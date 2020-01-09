package org.narrative.network.core.area.base.services;

import org.narrative.network.core.area.base.Area;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;

import java.sql.Timestamp;
import java.util.Locale;
import java.util.TimeZone;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Sep 16, 2009
 * Time: 9:21:48 AM
 *
 * @author Steven Benitez
 */
public class CreateCommunityTask extends GlobalTaskImpl<Area> {
    private String emailAddress;
    private boolean emailVerified;
    private String password;
    private String displayName;

    private Locale locale;
    private TimeZone timeZone;

    private boolean sendNewUserEmail = true;

    private Timestamp creationDatetime;

    private final String name;

    public CreateCommunityTask(String name) {
        super(true);
        this.name = name;
    }

    @Override
    protected Area doMonitoredTask() {

        CreateArea createAreaTask = new CreateArea(name);
        if (!isEmpty(emailAddress)) {
            createAreaTask.setEmailAddress(emailAddress);
            createAreaTask.setEmailVerified(emailVerified);
            createAreaTask.setPassword(password);
            createAreaTask.setDisplayName(displayName);
            createAreaTask.setLocale(locale);
            createAreaTask.setTimeZone(timeZone);
            createAreaTask.setSendNewUserEmail(sendNewUserEmail);
        }
        createAreaTask.setCreationDatetime(creationDatetime);

        Area area = getNetworkContext().doGlobalTask(createAreaTask);

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
