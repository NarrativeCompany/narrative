package org.narrative.network.core.master.graemlins;

import org.narrative.common.persistence.OID;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Feb 28, 2006
 * Time: 11:00:53 PM
 *
 * @author Brian
 */
@MappedSuperclass
public class Graemlin {
    protected OID oid;
    protected int sortOrder;
    protected String keystroke;
    private boolean enabled = true;

    private transient Pattern keystrokePattern;
    private int height;
    private int width;
    private final boolean isDefault;
    private String emoji;

    public static final Map<String, Graemlin> DEFAULT_GRAEMLIN_MAP;

    static {
        Map<String, Graemlin> map = new LinkedHashMap<>();

        //This creates the default graemlins, which can actually be altered.
        addDefaultGraemlin(map, 1, ":)", "\uD83D\uDE0A", true);
        addDefaultGraemlin(map, 2, ":(", "\uD83D\uDE41️", true);
        // bl: making big grin not case insensitive since it doesn't make sense as ":d".  Oh, and it was breaking my object/embed
        // stuff since we use a classid="clsid:d27cdb6e-ae6d-11cf-96b8-444553540000" ;)
        addDefaultGraemlin(map, 3, ":D", "\uD83D\uDE00", false);
        addDefaultGraemlin(map, 4, ";)", "\uD83D\uDE09", true);
        addDefaultGraemlin(map, 5, ":o", "\uD83D\uDE32", true);
        addDefaultGraemlin(map, 6, ":p", "\uD83D\uDE1B", true);

        DEFAULT_GRAEMLIN_MAP = Collections.unmodifiableMap(map);
    }

    private static void addDefaultGraemlin(Map<String, Graemlin> map, int id, String keystroke, String emoji, boolean isCaseInsensitive) {
        int order = map.size() + 1;
        Graemlin defaultGraemlin = new Graemlin(true);
        defaultGraemlin.keystroke = keystroke;
        defaultGraemlin.keystrokePattern = Pattern.compile(keystroke, Pattern.LITERAL | (isCaseInsensitive ? Pattern.CASE_INSENSITIVE : 0));
        defaultGraemlin.emoji = emoji;
        defaultGraemlin.oid = OID.valueOf(id);
        defaultGraemlin.sortOrder = order;

        map.put(defaultGraemlin.getKeystroke(), defaultGraemlin);
    }

    public Graemlin(){this.isDefault=true;}

    public Graemlin(boolean isDefault) {
        this.isDefault = isDefault;
    }

    /**
     * constructor to duplicate a default Graemlin with a different sortOrder
     *
     * @param graemlin  the default graemlin to duplicate
     * @param sortOrder the new sortOrder to use
     */
    public Graemlin(Graemlin graemlin, int sortOrder) {
        assert graemlin.isDefault : "Should only duplicate default Graemlins!";
        this.oid = graemlin.oid;
        this.sortOrder = sortOrder;
        this.keystroke = graemlin.keystroke;
        this.keystrokePattern = graemlin.keystrokePattern;
        this.enabled = graemlin.enabled;
        this.height = graemlin.height;
        this.width = graemlin.width;
        this.isDefault = graemlin.isDefault;
        this.emoji = graemlin.emoji;
    }

    @Id
    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    public static final int MIN_KEYSTROKE_LENGTH = 1;
    public static final int MAX_KEYSTROKE_LENGTH = 40;

    @NotNull
    @Length(min = MIN_KEYSTROKE_LENGTH, max = MAX_KEYSTROKE_LENGTH)
    public String getKeystroke() {
        return keystroke;
    }

    public void setKeystroke(String keystroke) {
        this.keystroke = keystroke;
        keystrokePattern = null;
    }

    @NotNull
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Transient
    public int getHeight() {
        return height;
    }

    @Transient
    public int getWidth() {
        return width;
    }

    @Transient
    public Pattern getKeystrokePattern() {
        assert !isDefaultGraemlin() || keystrokePattern != null : "The keystrokePattern should always be initialized up front for default Graemlins!";
        if (keystrokePattern == null) {
            Graemlin defaultGraemlin = getOverriddenDefaultGraemlin();
            // bl: if it's an override of a default graemlin, just re-use the keystroke pattern from the default
            if (defaultGraemlin != null) {
                this.keystrokePattern = defaultGraemlin.getKeystrokePattern();
            } else {
                this.keystrokePattern = Pattern.compile(":" + getKeystroke() + ":", Pattern.LITERAL | Pattern.CASE_INSENSITIVE);
            }
        }

        return keystrokePattern;
    }

    @Transient
    public String getReplacement() {
        if (getType().isEmoji()) {
            return getEmoji();
        }
        return null;
    }

    @Transient
    public GraemlinType getType() {
        return isEmpty(getEmoji()) ? GraemlinType.IMAGE : GraemlinType.EMOJI;
    }

    @Transient
    public boolean isDefaultGraemlin() {
        return isDefault;
    }

    @Transient
    private Graemlin getOverriddenDefaultGraemlin() {
        return isDefaultGraemlin() ? null : DEFAULT_GRAEMLIN_MAP.get(getKeystroke());
    }

    public static final int MIN_EMOJI_LENGTH = 1;
    public static final int MAX_EMOJI_LENGTH = 10;

    @Length(min = MIN_EMOJI_LENGTH, max = MAX_EMOJI_LENGTH)
    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }
}
