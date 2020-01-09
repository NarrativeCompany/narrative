package org.narrative.config.localization;

import jodd.util.LocaleUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import javax.servlet.http.HttpServletRequest;

import java.util.Locale;

/**
 * Locale resolver that will utilize a fixed language derived from the default locale while delegating to
 * {@link AcceptHeaderLocaleResolver#resolveLocale} to determine the country if the Accept-Language header
 * is specified for a request.
 */
public class FixedLanguageHeaderLocaleResolver extends AcceptHeaderLocaleResolver {
    private final Locale defaultLocale;

    public FixedLanguageHeaderLocaleResolver(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    /**
     * Resolve the locale for this request
     *
     * @param request Incoming request
     * @return Resolved locale for the request
     */
    @NotNull
    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        //Resolve locale from the incoming request
        Locale headerLocale = super.resolveLocale(request);

        //Build a new Locale that utilizes the fixed language and country from the request
        return LocaleUtil.getLocale(defaultLocale.getLanguage(), headerLocale.getCountry());
    }
}
