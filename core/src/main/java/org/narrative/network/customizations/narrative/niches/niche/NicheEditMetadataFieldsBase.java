package org.narrative.network.customizations.narrative.niches.niche;

import org.narrative.common.util.posting.HtmlTextMassager;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/16/18
 * Time: 10:04 AM
 */
public abstract class NicheEditMetadataFieldsBase {
    public abstract String getNewName();

    public abstract void setNewName(String name);

    public abstract String getNewDescription();

    public abstract void setNewDescription(String description);

    public abstract String getOriginalName();

    public abstract void setOriginalName(String name);

    public abstract String getOriginalDescription();

    public abstract void setOriginalDescription(String description);

    public String getNewNameForHtml() {
        return HtmlTextMassager.disableHtml(getNewName());
    }

    public String getNewDescriptionForHtml() {
        return HtmlTextMassager.disableHtml(getNewDescription());
    }

    public String getOriginalNameForHtml() {
        return HtmlTextMassager.disableHtml(getOriginalName());
    }

    public String getOriginalDescriptionForHtml() {
        return HtmlTextMassager.disableHtml(getOriginalDescription());
    }

    public void setup(Niche niche, String newName, String newDescription) {
        // jw: for full transparency, let's track the original name and desription no matter what!
        setOriginalName(niche.getName());
        setOriginalDescription(niche.getDescription());

        // jw: now, lets only store the new name/description if the provided values are actually different
        if (!isEqual(niche.getName(), newName)) {
            setNewName(newName);
        }

        if (!isEqual(niche.getDescription(), newDescription)) {
            setNewDescription(newDescription);
        }
    }

    public boolean isWasNameChanged() {
        // jw: due to the logic above, we know that the name was changed if we have a value for the property
        return !isEmpty(getNewName());
    }

    public boolean isWasDescriptionChanged() {
        // jw: due to the logic above, we know that the description was changed if we have a value for the property
        return !isEmpty(getNewDescription());
    }
}
