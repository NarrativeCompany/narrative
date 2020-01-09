package org.narrative.network.shared.email;

import org.narrative.common.util.CoreUtils;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.MailUtil;
import org.narrative.common.util.SubListIterator;
import org.narrative.common.util.posting.HtmlTextMassager;
import org.narrative.config.StaticConfig;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.core.system.EmailToJSPMapping;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.core.user.AuthZoneMaster;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.context.NetworkContextImplBase;
import org.narrative.network.shared.context.NetworkContextInternal;
import org.narrative.network.shared.interceptors.NetworkContextInterceptor;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.NetworkTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkJspRunner;
import org.narrative.network.shared.util.NetworkLogger;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.internet.InternetAddress;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Brought over CoreMailer from the old platform code
 * <p>
 * Date: Jan 18, 2006
 * Time: 10:40:46 AM
 *
 * @author Brian
 */
public class NetworkMailUtil {

    private static final NetworkLogger logger = new NetworkLogger(NetworkMailUtil.class);

    public static final String IS_JSP_EMAIL_ATTR_NAME = "isJspEmail";
    public static final String IS_JSP_EMAIL_ALLOWS_REPLIES_ATTR_NAME = "isJspEmailAllowsReplies";

    static void sendEmail(NetworkEmail networkEmail, boolean sendInSeparateThread, boolean sendOnSuccessOrFailure) {
        //if there is a partition group active, then only send e-mails at the end of the partition group's life to insure e-mails are not sent even if the partition group errors out
        // bl: updating to rely on a current PartitionGroup being set (as is always the case currently and should be going forward).
        if (sendInSeparateThread) {
            if (sendOnSuccessOrFailure) {
                PartitionGroup.addEndOfPartitionGroupRunnableForUtilityThreadForSuccessOrError(networkEmail);
            } else {
                PartitionGroup.addEndOfPartitionGroupRunnableForUtilityThread(networkEmail);
            }
        } else if (sendOnSuccessOrFailure) {
            PartitionGroup.addEndOfPartitionGroupRunnableForSuccessOrError(networkEmail);
        } else {
            PartitionGroup.addEndOfPartitionGroupRunnable(networkEmail);
        }
    }

    private static final String EMAIL_FROM_AUTH_ZONE_MASTER = NetworkMailUtil.class.getSimpleName() + "-EmailFromAuthZoneMaster";

    public static AuthZoneMaster getEmailFromAuthZoneMaster() {
        return networkContext().getContextData(EMAIL_FROM_AUTH_ZONE_MASTER);
    }

    public static void sendJspCreatedEmail(final NetworkTaskImpl<?> task, User user) {
        sendJspCreatedEmail(task, user, null, null);
    }

    public static void sendJspCreatedEmail(final NetworkTaskImpl<?> task, User user, String subject, String overrideEmailAddress) {
        sendJspCreatedEmail(task, user, subject, overrideEmailAddress, null, null);
    }

    public static void sendJspCreatedEmail(final NetworkTaskImpl<?> task, User user, String subject, String overrideEmailAddress, String overrideReplyToEmailAddress, String overrideUserName) {
        // jw: lets only try to send the mail if we are not using a custom email address, and the users email address passes our current requirements.
        if (isEmpty(overrideEmailAddress) && !MailUtil.isEmailAddressValid(user.getEmailAddress())) {
            if (logger.isInfoEnabled()) {
                logger.info("Found a user with invalid email address! Likely due to changes in our email validation logic. user/" + user.getOid() + " email/" + user.getEmailAddress());
            }
            return;
        }

        networkContext().runAsPrimaryRole(user, () -> NetworkMailUtil.sendJspCreatedEmail(task, !isEmpty(overrideEmailAddress) ? overrideEmailAddress : user.getEmailAddress(), !isEmpty(overrideUserName) ? overrideUserName : user.getDisplayNameResolved(), !isEmpty(overrideReplyToEmailAddress) ? overrideReplyToEmailAddress : null, subject));
    }

    private static void sendJspCreatedEmail(final NetworkTaskImpl<?> task, String toEmail, String toDisplayName, String replyToEmail, String subject) {
        AuthZone authZone = task.getNetworkContext().getAuthZone();

        String fromDisplayName = authZone.getReplyToEmailAlias();
        String fromEmail = authZone.getReplyToEmailAddress();

        sendJspCreatedEmail(task, fromEmail, fromDisplayName, replyToEmail, Collections.singletonList(toEmail), Collections.singletonList(toDisplayName), subject);
    }

    private static void sendJspCreatedEmail(final NetworkTaskImpl<?> task, String fromEmail, String fromDisplayName, String replyToEmail, Collection<String> toEmails, Collection<String> toDisplayNames, String subject) {
        final String jspPage = EmailToJSPMapping.INSTANCE.getJSPFileForEmail(task);

        NetworkContextInternal networkContextInternal = (NetworkContextInternal) task.getNetworkContext();

        networkContextInternal.setContextData(NetworkContextImplBase.IS_PROCESSING_JSP_EMAIL_CONTEXT_DATA_PARAM, true);

        // bl: if there is no NetworkContext found, then that means it is an email that is being sent from
        // an AuthZone master. in that case, we don't care about the associated AuthZone for billing purposes.
        // thus, just use the Network AuthZone in that case. in all other cases where an email is being sent
        // from an Area, the proper AuthZone should be determined here.
        AuthZone authZone = networkContextInternal.getAuthZone();

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(IS_JSP_EMAIL_ATTR_NAME, Boolean.TRUE);
        // bl: allow replies if the reply-to email is not empty.
        attributes.put(IS_JSP_EMAIL_ALLOWS_REPLIES_ATTR_NAME, !isEmpty(replyToEmail));
        // jw: to ensure that the networkContext is always referencable in our JSP, lets setup the standard attribute.
        attributes.put(NetworkContextInterceptor.REQUEST_ATTRIBUTE_NETWORK_CONTEXT, task.getNetworkContext());

        String body;
        try {
            body = StaticConfig.getBean(NetworkJspRunner.class).runJsp(jspPage, task, attributes);
        } finally {
            networkContextInternal.setContextData(NetworkContextImplBase.IS_PROCESSING_JSP_EMAIL_CONTEXT_DATA_PARAM, null);
        }

        // bl: if this is a plain text email, then we need to enable disabled HTML in the email body prior to sending.
        boolean textEmail = false;

        if (attributes.get("textEmail") != null) {
            textEmail = (Boolean) attributes.get("textEmail");
        }

        if (textEmail) {
            body = HtmlTextMassager.enableDisabledHtml(body);
        }

        if (IPStringUtil.isEmpty(subject)) {
            subject = (String) attributes.get("subject");
            if (subject == null) {
                subject = "";
            }
        }

        // bl: enable disabled HTML in the display names so that display names containing <, >, or & will
        // appear correctly in emails.
        fromDisplayName = HtmlTextMassager.enableDisabledHtml(fromDisplayName);
        if (toDisplayNames != null) {
            Collection<String> newToDisplayNames = new ArrayList<>(toDisplayNames.size());
            for (String toDisplayName : toDisplayNames) {
                newToDisplayNames.add(HtmlTextMassager.enableDisabledHtml(toDisplayName));
            }
            toDisplayNames = newToDisplayNames;
        }

        sendJavaCreatedEmail(authZone, fromEmail, fromDisplayName, replyToEmail, toEmails, toDisplayNames, subject, body, textEmail, true, task instanceof SendEmailOnSuccessOrErrorTask);
    }

    public static void sendJavaCreatedEmail(AuthZone authZone, String fromEmail, String fromDisplayName, String replyToEmail, Collection<String> toEmails, Collection<String> toDisplayNames, String subject, String body, boolean isTextEmail, boolean sendInSeparateThread, boolean sendOnSuccessOrError) {

        // bl: now that we're disabling HTML in all input fields by default, we need to make
        // sure that we enable disabled HTML in subjects prior to sending the email or else
        // the email will have entities instead of <, >, and & characters.
        // bl: subjects generally should be HTML disabled fields, so we shouldn't really
        // ever be including "regular" (valid) HTML in subjects.
        subject = HtmlTextMassager.enableDisabledHtml(subject);
        // bl: detect if the subject has newlines in it, which will break mail headers.
        if (subject.contains("\n")) {
            String message = "Found subject with newlines! Will break email header formatting! subject/" + subject;
            StatisticManager.recordException(new Throwable(message), false, null);
            if (logger.isErrorEnabled()) {
                logger.error(message);
            }
        }

        InternetAddress fromAddress = MailUtil.getEmail(fromEmail, fromDisplayName, true);
        InternetAddress replyToEmailAddress = MailUtil.getEmail(replyToEmail, fromDisplayName, true);
        Collection<InternetAddress> toAddresses = AddresseeFactory.getInternetAddresses(toEmails, toDisplayNames);

        if (toAddresses.size() > 0) {
            SubListIterator<InternetAddress> toAddressesIterable = new SubListIterator<>(new ArrayList<>(toAddresses), 500);
            while (toAddressesIterable.hasNext()) {
                List<InternetAddress> toEmailsSubList = toAddressesIterable.next();
                NetworkEmail networkEmail = new NetworkEmail(authZone, fromAddress, replyToEmailAddress, toEmailsSubList, subject, body, isTextEmail);
                NetworkMailUtil.sendEmail(networkEmail, sendInSeparateThread, sendOnSuccessOrError);
            }
        } else {
            NetworkEmail networkEmail = new NetworkEmail(authZone, fromAddress, replyToEmailAddress, toAddresses, subject, body, isTextEmail);
            NetworkMailUtil.sendEmail(networkEmail, sendInSeparateThread, sendOnSuccessOrError);
        }
    }

    static final class NetworkEmail implements Runnable {
        private final AuthZone authZone;
        private final InternetAddress fromAddress;
        private final InternetAddress replyToAddress;
        private final Collection<InternetAddress> toAddresses;
        private final String subject;
        private final String body;
        private final boolean textEmail;

        NetworkEmail(AuthZone authZone, InternetAddress fromAddress, InternetAddress replyToAddress, Collection<InternetAddress> toAddresses, String subject, String body, boolean textEmail) {
            this.authZone = authZone;
            this.fromAddress = fromAddress;
            this.replyToAddress = replyToAddress;
            this.toAddresses = toAddresses;
            this.subject = subject;
            this.body = body;
            this.textEmail = textEmail;
        }

        public void run() {
            // bl: just always do the sending of email in a root global task.  that way, the entire process is always
            // transactional.  plus, we may do things (like logging statistics) that require a current PartitionGroup.
            TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>() {
                @Override
                protected Object doMonitoredTask() {
                    // jw: Always send the email within a authZone task so that we can rely on the authZone to be accurate
                    //     within the network context.
                    //     Further this task will ensure that the proper Realm partition will be in scope for Area authZones.
                    return getNetworkContext().doAuthZoneTask(authZone, new GlobalTaskImpl<Object>() {
                        @Override
                        protected Object doMonitoredTask() {
                            if (!CoreUtils.isEmpty(NetworkRegistry.getInstance().getTestEmailToUse())) {
                                if (!CoreUtils.isEmptyOrNull(toAddresses)) {
                                    for (InternetAddress toAddress : toAddresses) {
                                        toAddress.setAddress(NetworkRegistry.getInstance().getTestEmailToUse());
                                    }
                                }
                            }

                            try {
                                MailUtil.sendEmail(fromAddress, replyToAddress, toAddresses, subject, body, textEmail);
                            } catch (MessagingException mex) {
                                //there was a messaging exception, lets log it and create a durable scheduled task
                                logger.error("Could not send email. subject/" + subject + " from/" + fromAddress + " to/" + IPStringUtil.getCommaSeparatedList(toAddresses), mex);
                                StatisticManager.recordException(mex, false, null);

                                if (mex instanceof SendFailedException) {
                                    SendFailedException sfe = (SendFailedException) mex;

                                    Set<InternetAddress> toAddressSet = new HashSet<>(toAddresses);

                                    Set<InternetAddress> addressesToRemove = new HashSet<>();

                                    if (sfe.getInvalidAddresses() != null) {
                                        for (Address address : sfe.getInvalidAddresses()) {
                                            if (address instanceof InternetAddress) {
                                                addressesToRemove.add((InternetAddress) address);
                                            }
                                        }
                                    }

                                    if (sfe.getValidSentAddresses() != null) {
                                        for (Address address : sfe.getValidSentAddresses()) {
                                            if (address instanceof InternetAddress) {
                                                addressesToRemove.add((InternetAddress) address);
                                            }
                                        }
                                    }

                                    toAddressSet.removeAll(addressesToRemove);
                                    if (!toAddressSet.isEmpty()) {
                                        ResendFailedEmailJob.schedule(authZone, fromAddress, replyToAddress, toAddressSet, subject, body, textEmail);
                                    }
                                } else {
                                    ResendFailedEmailJob.schedule(authZone, fromAddress, replyToAddress, toAddresses, subject, body, textEmail);
                                }
                            }
                            return null;
                        }
                    });
                }
            });
        }
    }

    /**
     * a Factory to create internet addresses
     */
    private static class AddresseeFactory {

        private static Collection<InternetAddress> getInternetAddresses(Collection<String> emailAddresses, Collection<String> displayNames) {
            if (emailAddresses == null || emailAddresses.isEmpty() || (displayNames != null && displayNames.size() != emailAddresses.size())) {
                return Collections.emptyList();
            }

            Collection<InternetAddress> emailList = new ArrayList<>(emailAddresses.size());

            String[] displayNamesArray = null;
            if (displayNames != null) {
                displayNamesArray = displayNames.toArray(new String[]{});
            }

            int i = 0;
            for (String emailAddress : emailAddresses) {
                InternetAddress internetAddress = MailUtil.getEmail(emailAddress, displayNamesArray != null ? displayNamesArray[i] : null, false);
                // bl: skip any invalid emails
                if (internetAddress == null) {
                    continue;
                }
                emailList.add(internetAddress);
                i++;
            }

            return emailList;
        }
    }
}
