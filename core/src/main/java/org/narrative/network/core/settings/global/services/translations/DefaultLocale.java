package org.narrative.network.core.settings.global.services.translations;

import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.common.util.enums.StringEnum;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: Apr 21, 2006
 * Time: 6:29:12 PM
 *
 * @author Brian
 */
public enum DefaultLocale implements StringEnum, IntegerEnum {
    ENGLISH(1, Locale.ENGLISH),
    SPANISH(2, new Locale("es")),
    SWEDISH(3, new Locale("sv")),
    FRENCH(4, new Locale("fr")),
    POLISH(5, new Locale("pl")),
    GERMAN(6, new Locale("de")),
    PORTUGUESE(7, new Locale("pt")),
    JAPANESE(8, new Locale("ja"))
    //,CHINESE(Locale.CHINESE)
    // refer: http://www.mpi-sb.mpg.de/~pesca/locales.html
    //PORTUGUESE(IPUtil.parseLocaleString("pt")),
    ;

    public static final String ENUM_FIELD_TYPE = "varchar(10)";

    private final int id;
    private final Locale locale;

    private DefaultLocale(int id, Locale locale) {
        this.id = id;
        this.locale = locale;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getIdStr() {
        return locale.getLanguage();
    }

    public Locale getLocale() {
        return locale;
    }

    public String getNameForDisplay() {
        return wordlet(getNameForDisplayKey());
    }

    public String getNameForDisplayInLanguage() {
        return NetworkResourceBundle.getResourceBundle(getLocale()).getString(getNameForDisplayKey());
    }

    private String getNameForDisplayKey() {
        return "locale." + getLocale();
    }

    public boolean isEnglish() {
        return this == ENGLISH;
    }

    public boolean isDefault() {
        return isEnglish();
    }

    public static DefaultLocale getDefault() {
        return DefaultLocale.ENGLISH;
    }

    public static Locale getDefaultLocale() {
        return Locale.ENGLISH;
    }

    private static final Set<Locale> AVAILABLE_LOCALES = Collections.unmodifiableSet(newHashSet(Locale.getAvailableLocales()));

    public static boolean isAvailableLocale(Locale locale) {
        return locale != null && AVAILABLE_LOCALES.contains(locale);
    }

    public static void main(String[] args) {
        Set<String> locales = newTreeSet();
        for (Locale availableLocale : AVAILABLE_LOCALES) {
            locales.add(availableLocale + ": " + availableLocale.getDisplayName() + " " + availableLocale.getDisplayVariant() + " " + availableLocale.getDisplayCountry());
        }
        for (String locale : locales) {
            System.out.println(locale);
        }
        for (Locale locale : AVAILABLE_LOCALES) {
            if (!isEmpty(locale.getVariant())) {
                System.out.println("Variant: " + locale + ": " + locale.getVariant());
            }
        }
    }
}
