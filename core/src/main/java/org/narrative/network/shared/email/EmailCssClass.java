package org.narrative.network.shared.email;

import org.narrative.network.core.user.AuthZoneMaster;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;
import java.util.TreeSet;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 10/31/12
 * Time: 1:54 PM
 * User: jonmark
 */
public enum EmailCssClass implements EmailCssProvider {
    EMAIL_WRAPPER("border:1px solid #dfdfdf; background-color:#FFFFFF; margin:auto; max-width: 620px; padding: 10px 20px; text-align:left; border-radius:15px;"),
    WRAPPER_IMAGE("margin: 0;"),
    WRAPPER_TABLE("width: 100%;"),
    HEADER_IMAGE_LEFT("text-align: left;"),
    HEADER_IMAGE_CENTER("text-align: center;"),
    HEADER_IMAGE_RIGHT("text-align: right;"),
    EMAIL_CONTENT("margin-top: 20px; margin-bottom:20px;"),
    SALUTATION("font-weight:bold; padding-bottom:20px; font-size:14px; line-height:22px;"),
    INTRO_PARAGRAPH("margin-bottom:15px; font-size:14px; line-height:22px;"),
    TITLE("font-weight:bold; padding-bottom:20px; font-size:28px; line-height:22px;"),
    TITLE_BAR("background-color:#ecf6fe; color:#333333; padding:10px; margin-bottom:10px; font-weight:600; font-size:15px; text-transform:uppercase; border:1px solid #dfdfdf;"),
    DETAILS_BOX_SHADING("background-color:#fbfdff; color:#333333; border:1px solid #e8ecf1; border-radius:3px;"),
    DETAILS_BOX("padding:14px; margin-bottom:15px; font-size:13px;"),
    DETAILS_BOX_TABLE("width: 100%;"),
    DETAILS_BOX_INFO_COLUMN("vertical-align:top; padding-bottom:7px;"),
    DETAILS_BOX_AVATAR_LEFT_COLUMN("width: 70px; padding-right:10px; vertical-align: top;"),
    DETAILS_BOX_SMALL_AVATAR_LEFT_COLUMN("width: 38px; padding-right:10px; vertical-align: top;"),
    DETAILS_BOX_COMMUNITY_AVATAR_LEFT_COLUMN("width: 180px; padding-right:10px; vertical-align: top;"),
    DETAILS_BOX_AVATAR_RIGHT_COLUMN("width: 70px; padding-left:7px; vertical-align: top;"),
    DETAILS_BOX_SMALL_AVATAR_RIGHT_COLUMN("width: 38px; padding-left:7px; vertical-align: top;"),
    DETAILS_BOX_COMMUNITY_AVATAR_RIGHT_COLUMN("width: 200px; padding-left:7px; vertical-align: top;"),
    DETAILS_BOX_LEFT_AVATAR(""),
    DETAILS_BOX_RIGHT_AVATAR(""),
    DETAILS_BOX_LEFT_SMALL_AVATAR(""),
    DETAILS_BOX_SMALL_AVATAR("margin:0;border-radius:50%;vertical-align:middle;"),
    DETAILS_BOX_ROW("padding-bottom:7px; font-size:13px; line-height:20px;"),
    DETAILS_BOX_TITLE("font-size: 10px; text-transform:uppercase; font-weight:600; padding-right:5px; width: 15%; min-width: 90px; vertical-align:top;"),
    DETAILS_BOX_INFO("padding-right: 5px;"),
    DETAILS_BOX_CELL("padding-bottom:7px;word-wrap: break-word;"),
    DETAILS_BOX_PARAGRAPH("margin-bottom: 10px;"),
    PADDED_DETAILS_BOX_PARAGRAPH("margin-bottom: 10px; padding: 7px;"),
    DETAILS_BOX_BODY("margin-top:7px;"),
    SHADED_BOX_TITLE("font-weight:bold; margin-bottom:10px;"),
    SHADED_BOX("background-color:#ffffff; color:#333333; border:1px solid #f0f0f0; padding:7px; margin-bottom:10px; font-size:13px; line-height:20px; color:#333333;word-wrap: break-word;"),
    ACTION_BOX("background-color:#31d0f2; padding:0; margin-bottom:20px; border-radius:3px; text-align:center;"),
    ACTION_BOX_LINK("color:#FFFFF0; font-size:12px; font-weight:bold; text-transform:uppercase; display:block; padding:14px; text-decoration:none; margin-top: 15px;"),
    SIGNATURE("margin-top:10px; margin-bottom:25px; font-size:14px; line-height:22px;"),
    UNSUBSCRIBE("font-size:10px; line-height:14px; margin-bottom: 10px;"),
    FEATURED_STAR_ICON("float: right;"),
    FOOTER("margin-top:10px; font-size:12px;"),
    FOOTER_LINE("text-align:center;"),
    FOOTER_PLEASE_DO_NOT_REPLY("margin-bottom: 10px;font-size: 10px;color: #b5b5b5;"),
    CUSTOM_FOOTER("margin-top: 20px;"),
    CUSTOM_FOOTER_HTML("text-align: left; font-size:12px;"),
    CLEAR("clear: both; min-height: 1px; max-height: 1px;"),
    BOLD_TEXT("font-weight: bold;"),
    DO_NOT_WRITE_BELOW_LINE("text-align: center; color: #b5b5b5;"),
    NO_BORDER("margin:0;"),
    QUOTED_TEXT("padding:10px;margin:10px 0;background:#dfdfdf;border:1px solid #dfdfdf;"),
    QUOTED_TEXT_ALT("padding:7px;margin:10px 0;background:#cecece;border:1px solid #dfdfdf;"),
    QUOTE_HEADER("font-size:10px;line-height:14px;font-weight:bold;margin-bottom:7px;"),
    BIGGEST_TEXT("font-size:1.6rem;line-height:1.3;"),
    BIGGER_TEXT("font-size:1.4rem;line-height:1.3;"),
    BIG_TEXT("font-size:1.2rem;line-height:1.3;"),
    NORMAL_TEXT("font-size:1rem;line-height:1.3;"),
    SMALLER_TEXT("font-size:0.8rem;line-height:1.3;"),
    SMALLEST_TEXT("font-size:0.6rem;line-height:1.3;"),
    RED_TEXT("color:red"),
    GREEN_TEXT("color:green"),

    SIGNATURE_WRAPPER("margin-top:15px;", false),
    CENTERED_BODY("text-align: center; margin: 0 20px 60px 20px;", false),
    LEFT_ALIGNED_BODY("text-align: left; margin: 0 20px 60px 20px;", false),
    ICON_IMAGE("height: 50px; width: 50px; margin: 10px;", false),
    GRAY_TEXT("color: #bdbdbd;", false)
    ;

    public static final Set<EmailCssClass> ORDERED_EMAIL_CLASSES;

    private static final Comparator<EmailCssClass> EMAIL_CSS_CLASS_COMPARATOR = (o1, o2) -> {
        if (o1 == o2) {
            return 0;
        }

        return o1.getCssClass().compareToIgnoreCase(o2.getCssClass());
    };

    static {
        // jw: just going to do this inline since there is no reason to keep this object in memory for the life of the servlet
        Set<EmailCssClass> cssClasses = new TreeSet<>(EMAIL_CSS_CLASS_COMPARATOR);

        // jw: only add the public classes
        for (EmailCssClass cssClass : values()) {
            if (cssClass.hidden) {
                continue;
            }

            cssClasses.add(cssClass);
        }

        ORDERED_EMAIL_CLASSES = Collections.unmodifiableSet(cssClasses);
    }

    private final String css;
    private final String cssClass;
    private final boolean hidden;

    EmailCssClass(String css) {
        this(css, false);
    }

    EmailCssClass(String css, boolean hidden) {
        this.css = css;
        this.cssClass = name().toLowerCase();
        this.hidden = hidden;
    }

    public String getCssClass() {
        return cssClass;
    }

    @Override
    public String getCss() {
        return css;
    }

    public Set<EmailCssClass> getWithDetailBoxShading() {
        return EnumSet.of(DETAILS_BOX_SHADING, this);
    }

    public boolean isQuotedText() {
        return this == QUOTED_TEXT;
    }

}
