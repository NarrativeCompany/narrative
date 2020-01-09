package org.narrative.network.core.settings.global.services.translations;

import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.core.user.AuthRealm;
import org.narrative.network.shared.context.NetworkContext;
import org.narrative.network.shared.security.PrimaryRole;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * The NetworkResourceBundle is used to cascade resource bundles with support for both wordlets and translations.
 * <p>
 * Date: Dec 22, 2005
 * Time: 9:26:44 AM
 *
 * @author Brian
 */
public class NetworkResourceBundle extends ResourceBundle {

    private final ResourceBundle resourceBundle;
    private final ResourceBundle fallbackResourceBundle;
    private final boolean useKeysFromFallbackResourceBundle;

    private NetworkResourceBundle(@NotNull ResourceBundle resourceBundle, ResourceBundle fallbackResourceBundle, boolean useKeysFromFallbackResourceBundle) {
        this.resourceBundle = resourceBundle;
        this.fallbackResourceBundle = fallbackResourceBundle;
        this.useKeysFromFallbackResourceBundle = useKeysFromFallbackResourceBundle;
        assert !useKeysFromFallbackResourceBundle || fallbackResourceBundle != null : "Should always provide a fallbackResourceBundle when using keys from the fallback ResourceBundle!";
    }

    protected Object handleGetObject(String key) {
        try {
            return resourceBundle.getString(key);
        } catch (MissingResourceException mre) {
            // if we have a fallback resource bundle lets see if that has the value.
            if (fallbackResourceBundle != null) {
                return fallbackResourceBundle.getString(key);
            }
            // bl: throw exceptions right away on dev servers so we know about the issue
            if (NetworkRegistry.getInstance().isLocalServer()) {
                throw mre;
            } else {
                // bl: silently record the exception so that we'll be made aware of the missing wordlet on the error pages
                StatisticManager.recordException(mre, false, null);
            }
            // couldn't find a value?  then just return the key to prevent a MissingResourceException.
            return key;
        }
    }

    public Enumeration<String> getKeys() {
        if (useKeysFromFallbackResourceBundle) {
            return fallbackResourceBundle.getKeys();
        }
        return resourceBundle.getKeys();
    }

    private static final Pattern APOSTROPHE_PATTERN = Pattern.compile("'", Pattern.LITERAL);
    private static final String APOSTROPHE_REPLACEMENT = "''";

    public String getString(String key, Object... args) {
        String val = getString(key);
        if (isEmptyOrNull(args)) {
            return val;
        }
        // use the user selected locale (not the wordlet set's locale) for formatting
        // numbers and dates.
        Locale locale = networkContext().getLocale();
        // bl: escape all apostrophes with a double apostrophe since MessageFormat treats an apostrophe
        // as a special escape sequence.  this will suffice until we need to actually have special MessageFormat
        // escape sequences in our wordlet values (which i don't anticipate).  doing this will prevent us from needing
        // to double escape apostrophes conditionally based on whether they will have arguments escaped or not.
        val = APOSTROPHE_PATTERN.matcher(val).replaceAll(APOSTROPHE_REPLACEMENT);
        // todo: use a map of named parameters instead of indexed/numbered parameters?
        MessageFormat format = new MessageFormat(val, locale);
        return format.format(args);
    }

    @NotNull
    public static NetworkResourceBundle getResourceBundle(NetworkContext networkContext) {
        return getResourceBundle(networkContext.getAuthRealm(), networkContext.isHasPrimaryRole() ? networkContext.getPrimaryRole() : null);
    }

    @NotNull
    public static NetworkResourceBundle getResourceBundle(AuthRealm authRealm, PrimaryRole primaryRole) {
        DefaultLocale languageLocale = null;
        if (exists(primaryRole)) {
            languageLocale = primaryRole.getFormatPreferences().getLanguageLocale();
        }
        if(languageLocale==null) {
            languageLocale = authRealm.getDefaultLocale();
        }
        return getResourceBundle(languageLocale.getLocale());
    }

    @NotNull
    public static NetworkResourceBundle getResourceBundle(Locale locale) {
        locale = getLocaleForTranslation(locale);
        ResourceBundle resourceBundle = TranslationRegistry.getResourceBundle(locale);
        ResourceBundle fallbackResourceBundle;
        // jw: always fallback to english
        if (!isEqual(resourceBundle.getLocale(), DefaultLocale.getDefaultLocale())) {
            fallbackResourceBundle = new NetworkResourceBundle(TranslationRegistry.getResourceBundle(DefaultLocale.getDefaultLocale()), null, false);
        } else {
            fallbackResourceBundle = null;
        }
        return new NetworkResourceBundle(resourceBundle, fallbackResourceBundle, false);
    }

    @NotNull
    public static NetworkResourceBundle getDefaultResourceBundle() {
        ResourceBundle resourceBundle = TranslationRegistry.getPropertiesResourceBundle(DefaultLocale.getDefault());
        return new NetworkResourceBundle(resourceBundle, null, false);
    }

    /**
     * get the Locale that is supported for translations based off of a NetworkResourceBundleType
     * and the user's selected Locale
     *
     * @param userSelectedLocale the user's selected locale
     * @return the Locale to be used for a Default ResourceBundle lookup
     */
    @NotNull
    private static Locale getLocaleForTranslation(Locale userSelectedLocale) {
        if (TranslationRegistry.isLocaleLanguageSupported(userSelectedLocale)) {
            return userSelectedLocale;
        }

        /* bl: not using default locales.  doubt we ever will.  commenting out.
        if(requestType!=null && requestType.getAreaType().isCommunity()) {
            // the user's supplied locale isn't supported?  then default to the site's default language.
            // bl: trust that the Locale preference is actually supported.
            // i.e. don't need to check with the TranslationRegistry if it is valid.
            Area area = currentArea();
            Locale ret = null;
            if(exists(area)) {
                ret = getArea(area).getCommunitySettings().getAreaDefaultLocale().getLocale();
            }
            if(ret!=null)
                return ret;
        }*/

        // user locale not supported and area locale not set?  then just use the default locale
        return DefaultLocale.getDefaultLocale();
    }

}
