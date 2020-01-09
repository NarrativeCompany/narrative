package org.narrative.network.core.user.services;

import org.narrative.common.util.posting.HtmlTextMassager;
import org.narrative.network.core.user.NarrativeAuthZoneMaster;
import org.narrative.network.core.user.User;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.apache.http.client.utils.URIBuilder;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 12/9/16
 * Time: 9:01 AM
 */
public class TwoFactorAuthUtils {
    // jw: lets store a single instance of this to use for all of our calls.  We may want to customize the Authenticator.
    private static final GoogleAuthenticator AUTHENTICATOR = new GoogleAuthenticator();

    public static String getSecretKey() {
        // jw: the GoogleAuthenticationKey is disposable, since it adds no value lets not bother using that object.
        return AUTHENTICATOR.createCredentials().getKey();
    }

    public static boolean isValidCode(String secretKey, int code) {
        return AUTHENTICATOR.authorize(secretKey, code);
    }

    public static List<String> getSplitSecretKey(String secretKey) {
        List<String> splitup = new LinkedList<>();

        int i = 0;
        while ((i++ * 4) < secretKey.length()) {
            splitup.add(secretKey.substring((i - 1) * 4, i * 4));
        }

        return splitup;
    }

    // jw: basing this off of GoogleAuthenticatorQRGenerator, just updating it to take the secret directly instead of using GoogleAuthenticatorKey since those cannot be directly managed.

    public static String getOtpAuthTotpURL(User user, String secretKey) {
        return getOtpAuthTotpURL(NarrativeAuthZoneMaster.NARRATIVE_NAME, HtmlTextMassager.enableDisabledHtml(user.getAuthZone().getName()) + ": " + user.getEmailAddress(), secretKey);
    }

    private static String getOtpAuthTotpURL(String issuer, String accountName, String secret) {
        URIBuilder uri = new URIBuilder().setScheme("otpauth").setHost("totp").setPath("/" + formatLabel(issuer, accountName)).setParameter("secret", secret);

        if (issuer != null) {
            if (issuer.contains(":")) {
                throw new IllegalArgumentException("Issuer cannot contain the \':\' character.");
            }

            uri.setParameter("issuer", issuer);
        }

        return uri.toString();
    }

    // jw: confirmed here that this is the proper format to seperate a issue and an account: https://github.com/google/google-authenticator/wiki/Key-Uri-Format
    private static String formatLabel(String issuer, String accountName) {
        if (accountName == null || accountName.trim().length() == 0) {
            throw new IllegalArgumentException("Account name must not be empty.");
        }

        StringBuilder sb = new StringBuilder();
        if (issuer != null) {
            if (issuer.contains(":")) {
                throw new IllegalArgumentException("Issuer cannot contain the \':\' character.");
            }

            sb.append(issuer);
            sb.append(":");
        }

        sb.append(accountName);

        return sb.toString();
    }
}
