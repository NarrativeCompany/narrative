package org.narrative.network.shared.email;

import org.narrative.common.util.NarrativeException;
import org.narrative.common.util.SerializationUtil;
import org.narrative.network.core.quartz.NetworkJob;
import org.narrative.network.core.quartz.services.QuartzJobScheduler;
import org.narrative.network.core.user.AuthZone;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import java.util.Collection;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * User: barry
 * Date: Nov 28, 2007
 */

public class ResendFailedEmailJob extends NetworkJob {
    private static final String AUTH_ZONE = "authZone";
    private static final String FROM_ADDRESS = "fromAddress";
    private static final String REPLY_TO_ADDRESS = "replyToAddress";
    private static final String TO_ADDRESSES = "toAddresses";
    private static final String BODY = "body";
    private static final String SUBJECT = "subject";
    private static final String TEXT_EMAIL = "textEmail";

    public ResendFailedEmailJob() { }

    @Override
    protected void executeJob(JobExecutionContext context) throws JobExecutionException {

        AuthZone authZone = getAuthZoneFromJobDataMap(context.getMergedJobDataMap());

        InternetAddress fromAddress;
        InternetAddress replyToAddress;
        try {
            fromAddress = new InternetAddress(context.getMergedJobDataMap().getString(FROM_ADDRESS));
            String replyToEmailAddress = context.getMergedJobDataMap().getString(REPLY_TO_ADDRESS);
            replyToAddress = isEmpty(replyToEmailAddress) ? null : new InternetAddress(replyToEmailAddress);
        } catch (AddressException ax) {
            throw new NarrativeException("Could not recreate Internet Address from string", ax);
        }

        List<InternetAddress> toAddresses = SerializationUtil.deserializeCollection(context.getMergedJobDataMap().getString(TO_ADDRESSES), List.class, InternetAddress.class);
        String subject = context.getMergedJobDataMap().getString(SUBJECT);
        String body = context.getMergedJobDataMap().getString(BODY);
        boolean textEmail = context.getMergedJobDataMap().getBoolean(TEXT_EMAIL);

        NetworkMailUtil.NetworkEmail email = new NetworkMailUtil.NetworkEmail(authZone, fromAddress, replyToAddress, toAddresses, subject, body, textEmail);

        NetworkMailUtil.sendEmail(email, false, false);
    }

    public static void schedule(AuthZone authZone, InternetAddress fromAddress, InternetAddress replyToAddress, Collection<InternetAddress> toAddresses, String subject, String body, boolean textEmail) {
        JobBuilder jobBuilder = QuartzJobScheduler.createRecoverableJobBuilder(ResendFailedEmailJob.class);

        if (authZone != null) {
            jobBuilder.usingJobData(AUTH_ZONE, authZone.getOid().getValue());
        }
        jobBuilder.usingJobData(FROM_ADDRESS, fromAddress.toString());
        if (replyToAddress != null) {
            jobBuilder.usingJobData(REPLY_TO_ADDRESS, replyToAddress.toString());
        }
        jobBuilder.usingJobData(TO_ADDRESSES, SerializationUtil.serializeCollection(toAddresses));
        jobBuilder.usingJobData(BODY, body);
        jobBuilder.usingJobData(SUBJECT, subject);
        jobBuilder.usingJobData(TEXT_EMAIL, Boolean.toString(textEmail));

        QuartzJobScheduler.GLOBAL.scheduleForFiveMinutesFromNow(jobBuilder);
    }

    private static AuthZone getAuthZoneFromJobDataMap(JobDataMap jobDataMap) {
        if (jobDataMap.get(AUTH_ZONE) == null) {
            return null;
        }
        return AuthZone.getAuthZone(Long.valueOf(jobDataMap.getLong(AUTH_ZONE)));
    }
}
