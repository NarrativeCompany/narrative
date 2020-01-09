package org.narrative.network.core.settings.global.services.translations;

import org.narrative.common.util.enums.IntegerEnum;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Date: 5/19/14
 * Time: 10:37 PM
 *
 * @author brian
 */
public enum WordletClassification implements IntegerEnum {
    GLOBAL(0, true, true),
    CLUSTER(1, false, false),
    NARRATIVE_NICHES(14, true, true);

    private final int id;
    private final boolean isAllowedForWordlets;
    private final boolean isAllowedForTranslations;

    WordletClassification(int id, boolean isAllowedForWordlets, boolean isAllowedForTranslations) {
        this.id = id;
        this.isAllowedForWordlets = isAllowedForWordlets;
        this.isAllowedForTranslations = isAllowedForTranslations;
    }

    public static final Set<WordletClassification> CLASSIFICATIONS_ALLOWED_FOR_WORDLETS;
    public static final Set<WordletClassification> CLASSIFICATIONS_ALLOWED_FOR_TRANSLATIONS;

    static {
        Set<WordletClassification> classificationsForWordlets = EnumSet.noneOf(WordletClassification.class);
        Set<WordletClassification> classificationsForTranslations = EnumSet.noneOf(WordletClassification.class);
        for (WordletClassification wordletClassification : values()) {
            if (wordletClassification.isAllowedForWordlets) {
                classificationsForWordlets.add(wordletClassification);
            }
            if (wordletClassification.isAllowedForTranslations) {
                classificationsForTranslations.add(wordletClassification);
            }
        }
        CLASSIFICATIONS_ALLOWED_FOR_WORDLETS = Collections.unmodifiableSet(classificationsForWordlets);
        CLASSIFICATIONS_ALLOWED_FOR_TRANSLATIONS = Collections.unmodifiableSet(classificationsForTranslations);
    }

    @Override
    public int getId() {
        return id;
    }

    public String getResourceBundleBaseName() {
        return toString().toLowerCase();
    }

}
