/*
 * Created on Mar 5, 2003
 */
package org.narrative.common.util;

import org.narrative.network.core.system.NetworkRegistry;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.regex.Pattern;

import static org.narrative.common.util.CoreUtils.*;

public class MailUtil {
    private static final NarrativeLogger logger = new NarrativeLogger(MailUtil.class);

    private static final Pattern INVALID_EMAIL_PATTERN;
    private static final Pattern VALID_EMAIL_PATTERN;
    private static final Properties mailServerProps;
    private static Session mailSession;

    public static final String PRECEDENCE_HEADER = "Precedence";
    public static final String BULK_PRECEDENCE_VALUE = "bulk";

    private static final String AUTO_RESPONSE_SUPPRESS_HEADER = "X-Auto-Response-Suppress";
    private static final String AUTO_RESPONSE_SUPPRESS_VALUES = "OOF, AutoReply";

    static {
        /**
         * Ensure it:
         * 1. Doesn't start with .
         * 2. Doesn't have two consecutive .
         * 3. Doesn't have a . and @ next to each other
         * 4. Doesn't have any of the following characters: ( ) < > , ; : \
         * 4a. Added ? and !
         * 5. Doesn't contain any of the characters with ASCII values <= 32 (hex00-20) (CTRL characters and space)
         * 6. Doesn't end with .
         */
        INVALID_EMAIL_PATTERN = Pattern.compile("^[\\.]|[\\.]{2}|[@\\.]{2}|[\\(\\)\\<\\>\\,\\;\\:\\\\\\?\\!]|[\\x00-\\x20]|[\\.]$");

        //MUST HAVE AN '@' followed by text, followed by a '.', followed by text
        VALID_EMAIL_PATTERN = Pattern.compile("[@].+\\..+");

        mailServerProps = new Properties();
        mailServerProps.setProperty("mail.transport.protocol", "smtp");
        // Socket connection timeout value in milliseconds. Default is infinite timeout.
        mailServerProps.setProperty("mail.smtp.connectiontimeout", "60000");
        // Socket I/O timeout value in milliseconds. Default is infinite timeout.
        mailServerProps.setProperty("mail.smtp.timeout", "60000");
    }

    public static void init(String smtpHost, int smtpPort, final String username, final String password, boolean useTls) {
        mailServerProps.setProperty("mail.smtp.host", smtpHost);
        mailServerProps.setProperty("mail.smtp.port", Integer.toString(smtpPort));
        if (useTls) {
            mailServerProps.put("mail.smtp.starttls.enable", "true");
        }
        Authenticator authenticator;
        if (!isEmpty(username) && !isEmpty(password)) {
            mailServerProps.setProperty("mail.smtp.auth", "true");
            authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            };
        } else {
            authenticator = null;
        }
        //Get session
        mailSession = Session.getInstance(mailServerProps, authenticator);
    }

    public static InternetAddress getEmail(String email, String displayName, boolean isRequireValidEmail) {
        if (IPStringUtil.isEmpty(email)) {
            return null;
        }

        if (!isEmailAddressValid(email)) {
            // bl: if we aren't requiring a valid email address, then translate an invalid email address to null
            if (!isRequireValidEmail) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Found an invalid email address for sending emails! email/" + email);
                }
                return null;
            }
            assert false : "Should always supply valid email addresses when getting InternetAddress objects (and requiring a valid email)! email/" + email;
        }

        if (IPStringUtil.isEmpty(displayName)) {
            displayName = email;
        }

        try {
            return new InternetAddress(email, displayName);
        } catch (UnsupportedEncodingException use) {
            Debug.assertMsg(logger, false, "Failed getting email details", use);
        }
        return null;
    }

    public static boolean isEmailAddressValid(String email) {
        // if the email is empty, then it is not valid.
        if (IPStringUtil.isEmpty(email)) {
            return false;
        }

        // Parse the e-mail address to ensure it complies with RFC822 standards
        // An exception will be thrown if the e-mail address does not comply

        // Make sure address complies with *most* (but not all) RFC822 standards
        try {
            InternetAddress.parse(email, true);
        } catch (AddressException ae) {
            return false;
        } catch (StringIndexOutOfBoundsException e) {
            // bl: a bit odd to be catching an exception that isn't declared to be thrown, but InternetAddress.parse
            // has a bug that can cause a StringIndexOutOfBoundsException on InternetAddress.parse:745.
            // the result is that end_personal is less than start_personal for whatever reason.
            // so, we'll just catch that here and obviously treat this as an invalid email address.
            return false;
        }

        return (!INVALID_EMAIL_PATTERN.matcher(email).find() && VALID_EMAIL_PATTERN.matcher(email).find());
    }

    public static void main(String[] args) {
        Collection<String> testEmails = Arrays.asList("test", "test@", "test@t.t", "test@t.t.", "a@b.c", "a", "@", ".", "a..@b.c", "b@a..c", "i@dont@kno", "guest@;;&quot;?//zz\\\\@&%^\\(/#!.com", "natas@fearedillusions@w1.com", "aaets@+7673097213@msn.com");
        for (String testEmail : testEmails) {
            System.out.println(testEmail + " " + isEmailAddressValid(testEmail));
        }
    }

    public static void sendEmail(InternetAddress fromEmail, InternetAddress replyToEmail, Collection<InternetAddress> toEmails, String subject, String body, boolean textEmail) throws MessagingException {

        assert mailSession != null : "Should always initialize the mail session at servlet startup before attempting to send an email!";

        //Compose email
        MimeMessage message = new MimeMessage(mailSession);

        message.setFrom(fromEmail);
        if (replyToEmail != null) {
            message.setReplyTo(new InternetAddress[]{replyToEmail});
        }

        if (!isEmptyOrNull(toEmails)) {
            toEmails.removeIf(internetAddress -> !isEmailAddressValid(internetAddress.getAddress()));
            message.addRecipients(Message.RecipientType.TO, toEmails.toArray(new InternetAddress[]{}));
        }

        if (message.getAllRecipients() == null || message.getAllRecipients().length == 0) {
            throw new NarrativeException("Must have at least 1 recipient for mail message. subject/" + subject);
        }

        message.setSubject(subject, IPUtil.IANA_UTF8_ENCODING_NAME);
        message.setSentDate(new java.util.Date());

        MimeMultipart mp = new MimeMultipart();
        // bl: MimeMultipart defaults to multipart/mixed. that's proper if you have attachments.
        // if there are no attachments, then let's set it to multipart/alternative, which is the appropriate
        // way to handle HTML emails.
        mp.setSubType("alternative");

        message.setHeader(PRECEDENCE_HEADER, BULK_PRECEDENCE_VALUE);
        message.setHeader(AUTO_RESPONSE_SUPPRESS_HEADER, AUTO_RESPONSE_SUPPRESS_VALUES);

        MimeBodyPart bodyMBP = new MimeBodyPart();
        bodyMBP.setContent(body, (textEmail ? "text/plain" : "text/html") + "; charset=\"" + IPUtil.IANA_UTF8_ENCODING_NAME + "\"");
        mp.addBodyPart(bodyMBP);

        if (logger.isTraceEnabled()) {
            StringBuilder toEmailSB = new StringBuilder();
            for (InternetAddress toEmail : toEmails) {
                toEmailSB.append(toEmail.getAddress()).append(",");
            }

            logger.trace("Sending Email To: " + toEmailSB);

            logger.trace("Email From: " + fromEmail);
            logger.trace("Subject: " + subject);
            logger.trace("Body: " + body);
        }

        message.setContent(mp);
        if(NetworkRegistry.getInstance().isDisableEmailDelivery()) {
            logger.warn("SKIPPING email delivery since emails are disabled on this environment.");
        } else {
            Transport.send(message);
        }
    }
}