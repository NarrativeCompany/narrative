package org.narrative.network.core.settings.global.services.translations;

import org.narrative.common.util.IPUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Dec 9, 2005
 * Time: 9:21:54 AM
 *
 * @author Brian
 */
public class TranslationRegistry {

    private static final Map<String, DefaultLocale> SUPPORTED_LANGUAGE_TO_LOCALES = new HashMap<>();

    private static void addSupportedTranslationLocale(DefaultLocale locale) {
        assert !SUPPORTED_LANGUAGE_TO_LOCALES.containsKey(locale.getLocale().getLanguage()) : "Currently don't support multiple Locales for the same language!";
        SUPPORTED_LANGUAGE_TO_LOCALES.put(locale.getLocale().getLanguage(), locale);
    }

    static {
        // bl: for now, let's only initialize the default (English). we'll need to update this later
        // when we support other languages so a config controls which language gets loaded.
        addSupportedTranslationLocale(DefaultLocale.getDefault());
    }

    public static void init() {
        TaskRunner.doRootGlobalTask(new InitTask());
    }

    public static class InitTask extends GlobalTaskImpl<Object> {
        public InitTask() {
            super(false);
        }

        @Override
        protected Object doMonitoredTask() {
            // be sure that all of the various translation ResourceBundles are loaded (and thus cached).
            for (DefaultLocale defaultLocale : SUPPORTED_LANGUAGE_TO_LOCALES.values()) {
                // bl: initialize without any custom WordletClassifications. we only want to prime the "default" cache
                getResourceBundle(defaultLocale.getLocale());
            }
            return null;
        }
    }

    /**
     * given a Locale, get the "closest" Locale that is supported.
     * first checks to see if the Locale is supported directly.  if it
     * is, then the supplied Locale is returned.  if it is not supported
     * directly, then we test the language to see if the supplied locale's
     * language (but not country or region) is supported.  if it is, then
     * the Locale representing the language alone is returned.  if the language
     * isn't supported at all (or the supplied locale is null), then Locale.ENGLISH
     * (the default Locale) is returned.
     *
     * @param locale the Locale from which to obtain the supported Translation Locale
     * @return the supported translation Locale for the supplied Locale
     */
    @NotNull
    private static DefaultLocale getSupportedTranslationLocaleForLocale(Locale locale) {
        if (locale == null) {
            return DefaultLocale.getDefault();
        }
        DefaultLocale langMatch = SUPPORTED_LANGUAGE_TO_LOCALES.get(locale.getLanguage());
        if (langMatch != null) {
            return langMatch;
        }
        return DefaultLocale.getDefault();
    }

    public static boolean isLocaleLanguageSupported(Locale locale) {
        return SUPPORTED_LANGUAGE_TO_LOCALES.containsKey(locale.getLanguage());
    }

    public static ResourceBundle getResourceBundle(Locale locale) {
        final DefaultLocale defaultLocale = getSupportedTranslationLocaleForLocale(locale);
        // bl: for Narrative, we're just always going to use the Properties-backed ResourceBundle.
        return getPropertiesResourceBundle(defaultLocale);
    }

    public static ResourceBundle getPropertiesResourceBundle(DefaultLocale defaultLocale) {
        Map<String, String> map = newHashMap();
        // bl: aggregate the ResourceBundles together into a single ResourceBundle.
        for (WordletClassification wordletClassification : getWordletClassifications()) {
            ResourceBundle resourceBundle = getPropertiesResourceBundle(defaultLocale, wordletClassification);
            Set<String> keys = resourceBundle.keySet();
            // bl: make sure we don't have any key collisions between the stock classifications!
            // bl: note that we now allow customization classifications to override the stock wordlets
            Collection<String> collisions = CollectionUtils.intersection(map.keySet(), keys);
            if (!collisions.isEmpty()) {
                throw UnexpectedError.getRuntimeException("Found key collision between WordletClassification for: " + collisions);
            }
            for (String key : keys) {
                map.put(key, resourceBundle.getString(key));
            }
        }

        return new MapResourceBundle(defaultLocale.getLocale(), map);
    }

    public static Set<WordletClassification> getWordletClassifications() {
        return EnumSet.allOf(WordletClassification.class);
    }

    public static ResourceBundle getPropertiesResourceBundle(DefaultLocale defaultLocale, WordletClassification classification) {
        if (TRANSLATION_BUNDLE_CONTROL == null) {
            synchronized (TranslationRegistry.class) {
                if (TRANSLATION_BUNDLE_CONTROL == null) {
                    TRANSLATION_BUNDLE_CONTROL = new TranslationResourceBundleControl(NetworkRegistry.getInstance().isLocalServer());
                }
            }
        }

        return ResourceBundle.getBundle(classification.getResourceBundleBaseName(), defaultLocale.getLocale(), TRANSLATION_BUNDLE_CONTROL);
    }

    private static TranslationResourceBundleControl TRANSLATION_BUNDLE_CONTROL;

    private static final class TranslationResourceBundleControl extends ResourceBundle.Control {
        private final boolean needsReload;

        private TranslationResourceBundleControl(boolean needsReload) {
            this.needsReload = needsReload;
        }

        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {
            String bundleName = toBundleName(baseName, locale);
            ResourceBundle bundle = null;
            if (format.equals("java.class")) {
                try {
                    Class<? extends ResourceBundle> bundleClass = (Class<? extends ResourceBundle>) loader.loadClass(bundleName);

                    // If the class isn't a ResourceBundle subclass, throw a
                    // ClassCastException.
                    if (ResourceBundle.class.isAssignableFrom(bundleClass)) {
                        bundle = bundleClass.newInstance();
                    } else {
                        throw new ClassCastException(bundleClass.getName() + " cannot be cast to ResourceBundle");
                    }
                } catch (ClassNotFoundException e) {
                }
            } else if (format.equals("java.properties")) {
                final String resourceName = toResourceName(bundleName, "properties");
                final ClassLoader classLoader = loader;
                final boolean reloadFlag = reload;
                InputStream stream = null;
                try {
                    stream = AccessController.doPrivileged(new PrivilegedExceptionAction<InputStream>() {
                        public InputStream run() throws IOException {
                            InputStream is = null;
                            if (reloadFlag) {
                                URL url = classLoader.getResource(resourceName);
                                if (url != null) {
                                    URLConnection connection = url.openConnection();
                                    if (connection != null) {
                                        // Disable caches to get fresh data for
                                        // reloading.
                                        connection.setUseCaches(false);
                                        is = connection.getInputStream();
                                    }
                                }
                            } else {
                                is = classLoader.getResourceAsStream(resourceName);
                            }
                            return is;
                        }
                    });
                } catch (PrivilegedActionException e) {
                    throw (IOException) e.getException();
                }
                if (stream != null) {
                    try {
                        // bl: this line ensures that we load the ResourceBundle using UTF-8 encoding.
                        bundle = new PropertyResourceBundle(new InputStreamReader(stream, IPUtil.IANA_UTF8_ENCODING_NAME));
                    } finally {
                        stream.close();
                    }
                }
            } else {
                throw new IllegalArgumentException("unknown format: " + format);
            }
            return bundle;
        }

        public long getTimeToLive(String baseName, Locale locale) {
            //return TTL_DONT_CACHE;
            //return TTL_NO_EXPIRATION_CONTROL;
            // bl: for dev servers, force us to check the properties file on each request (the needsReload method checks last modified date).
            if (needsReload) {
                // a value of 0 means that we will check on each request if the ResourceBundle needs to be reloaded.
                return 0;
            }
            // bl: in non-dev environments, just always use the no expiration control (default behavior).  we don't
            // ever want cache invalidations in this case.
            return super.getTimeToLive(baseName, locale);
        }

        public List<String> getFormats(String baseName) {
            // bl: only allowing properties file formats
            return FORMAT_PROPERTIES;
        }

        public List<Locale> getCandidateLocales(String baseName, Locale locale) {
            // bl: to prevent the lookup of global.properties every time, only allow the candidate locales
            // list to contain the actual locale that we are looking for (en and the global_en.properties file).
            return Collections.singletonList(locale);
        }

        /* bl: the default needsReload function in ResourceBundle.Control will suffice.  it properly checks
           the last modified date of the properties file without doing any caching.
        public boolean needsReload(String baseName, Locale locale, String format, ClassLoader loader, ResourceBundle bundle, long loadTime) {

        }*/

        /* bl: the default newBundle function in ResourceBundle.Control will suffice.  if needsReload returns true
           (which will be the case if the properties file changes), then reload will be set to true here and the
           properties file will be re-read from disk.
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {

        }*/
    }
}
