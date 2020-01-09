package org.narrative.network.core.locations;

import org.narrative.common.util.NameForDisplayProvider;
import org.narrative.common.util.enums.StringEnum;

import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Jul 28, 2006
 * Time: 3:49:02 PM
 */
public enum Country implements StringEnum, NameForDisplayProvider {
    AD,
    AE,
    AF,
    AG,
    AI,
    AL,
    AM,
    AN,
    AO,
    AQ,
    AR,
    AS,
    AT,
    AU,
    AW,
    AX,
    AZ,
    BA,
    BB,
    BD,
    BE,
    BF,
    BG,
    BH,
    BI,
    BJ,
    BL,
    BM,
    BN,
    BO,
    BQ,
    BR,
    BS,
    BT,
    BV,
    BW,
    BY,
    BZ,
    CA,
    CC,
    CD,
    CF,
    CG,
    CH,
    CI,
    CK,
    CL,
    CM,
    CN,
    CO,
    CR,
    CU,
    CV,
    CW,
    CX,
    CY,
    CZ,
    DE,
    DJ,
    DK,
    DM,
    DO,
    DZ,
    EC,
    EE,
    EG,
    EH,
    ER,
    ES,
    ET,
    FI,
    FJ,
    FK,
    FM,
    FO,
    FR,
    GA,
    GB,
    GD,
    GE,
    GF,
    GG,
    GH,
    GI,
    GL,
    GM,
    GN,
    GP,
    GQ,
    GR,
    GS,
    GT,
    GU,
    GW,
    GY,
    GZ,
    HK,
    HM,
    HN,
    HR,
    HT,
    HU,
    ID,
    IE,
    IL,
    IM,
    IN,
    IO,
    IQ,
    IR,
    IS,
    IT,
    JE,
    JM,
    JO,
    JP,
    KE,
    KG,
    KH,
    KI,
    KM,
    KN,
    KP,
    KR,
    KW,
    KY,
    KZ,
    LA,
    LB,
    LC,
    LI,
    LK,
    LR,
    LS,
    LT,
    LU,
    LV,
    LY,
    MA,
    MC,
    MD,
    ME,
    MF,
    MG,
    MH,
    MK,
    ML,
    MM,
    MN,
    MO,
    MP,
    MQ,
    MR,
    MS,
    MT,
    MU,
    MV,
    MW,
    MX,
    MY,
    MZ,
    NA,
    NC,
    NE,
    NF,
    NG,
    NI,
    NL,
    NO,
    NP,
    NR,
    NU,
    NZ,
    OM,
    PA,
    PE,
    PF,
    PG,
    PH,
    PK,
    PL,
    PM,
    PN,
    PR,
    PS,
    PT,
    PW,
    PY,
    QA,
    RE,
    RO,
    RS,
    RU,
    RW,
    SA,
    SB,
    SC,
    SD,
    SE,
    SG,
    SH,
    SI,
    SJ,
    SK,
    SL,
    SM,
    SN,
    SO,
    SR,
    SS,
    ST,
    SV,
    SX,
    SY,
    SZ,
    TC,
    TD,
    TF,
    TG,
    TH,
    TJ,
    TK,
    TL,
    TM,
    TN,
    TO,
    TR,
    TT,
    TV,
    TW,
    TZ,
    UA,
    UG,
    UM,
    US,
    UY,
    UZ,
    VA,
    VC,
    VE,
    VG,
    VI,
    VN,
    VU,
    WF,
    WS,
    XK,
    YE,
    YT,
    ZA,
    ZM,
    ZW;

    public static final int MAX_COUNTRY_LENGTH = 2;
    public static final String ENUM_FIELD_TYPE = "char(" + MAX_COUNTRY_LENGTH + ")";

    @Override
    public String getIdStr() {
        // bl: for country codes, since we won't ever want to change them, just use the enum name as the idStr.
        return name();
    }

    public String getCountryCode() {
        return name();
    }

    public String getCountryDisplayName() {
        return wordlet("countryCode." + this);
    }

    @Override
    public String getNameForDisplay() {
        return getCountryDisplayName();
    }

    public boolean isUnitedStates() {
        return this == US;
    }

    public static Set<Country> getOrderedCountries(Locale locale) {
        Set<Country> set = new TreeSet<>(new CountryNameComparator(locale));
        set.addAll(Arrays.asList(Country.values()));
        return set;
    }

    public static Set<Country> getOrderedCountries() {
        return getOrderedCountries(networkContext().getAuthRealm().getDefaultLocale().getLocale());
    }

    private static final class CountryNameComparator implements Comparator<Country> {

        private final Collator instance;

        public CountryNameComparator(Locale locale) {
            instance = Collator.getInstance(locale);
            // normalise accented characters
            instance.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        }

        @Override
        public int compare(Country c1, Country c2) {
            if (c1 == c2) {
                return 0;
            }
            if (c1.isUnitedStates()) {
                return -1;
            } else if (c2.isUnitedStates()) {
                return 1;
            }
            return instance.compare(c1.getCountryDisplayName(), c2.getCountryDisplayName());
        }
    }
}
