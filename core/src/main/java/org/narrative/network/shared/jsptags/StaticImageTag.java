package org.narrative.network.shared.jsptags;

import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.XMLUtil;
import org.narrative.common.util.tags.StringWriterTagSupport;
import org.narrative.network.core.fileondisk.image.ImageUrlMetaData;
import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.email.NetworkMailUtil;
import org.narrative.network.shared.util.NetworkLogger;

import javax.servlet.jsp.PageContext;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: May 24, 2007
 * Time: 9:04:44 AM
 *
 * @author Brian
 */
public class StaticImageTag extends StringWriterTagSupport {
    private static final NetworkLogger logger = new NetworkLogger(StaticImageTag.class);

    public static final String NO_BORDER = "noBorder";

    private String src;
    private String alt;
    private String title;
    private String altTitle;
    private String cssClass;
    private String cssStyle;
    private String onclick;
    private String id;
    private String align;
    private String valign;
    private Integer width;
    private Integer height;
    private String useMap;

    public void release() {
        super.release();
        src = null;
        alt = null;
        title = null;
        altTitle = null;
        cssClass = null;
        cssStyle = null;
        onclick = null;
        id = null;
        align = null;
        valign = null;
        width = null;
        height = null;
        useMap = null;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAltTitle(String altTitle) {
        this.altTitle = altTitle;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }

    public void setCssStyle(String cssStyle) {
        this.cssStyle = cssStyle;
    }

    public void setOnclick(String onclick) {
        this.onclick = onclick;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAlign(String align) {
        this.align = align;
    }

    public void setValign(String valign) {
        this.valign = valign;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public void setUseMap(String useMap) {
        this.useMap = useMap;
    }

    private String getOutputString(ImageUrlMetaData metaData, Integer width, Integer height, StaticImageProperties imageProperties) {
        StringBuilder sb = new StringBuilder();
        sb.append("<img src=\"");
        sb.append(metaData.getUrl());
        sb.append("\"");
        sb.append(" width=\"");
        sb.append(width != null && width > 0 ? width : metaData.getWidth());
        sb.append("\"");
        sb.append(" height=\"");
        sb.append(height != null && height > 0 ? height : metaData.getHeight());
        sb.append("\"");
        if (!isEmpty(id)) {
            sb.append(" id=\"");
            sb.append(id);
            sb.append("\"");
        }
        // sb: alt is required and should always be there, even if empty
        // jw: updating these two attributes to use the XMLUtil to ensure that if the title has " in it then it will be escaped
        //     this is similar to what we do for g.tld:attr
        sb.append(" alt=\"");
        if (!isEmpty(alt)) {
            sb.append(XMLUtil.getEscapedXMLForAttribute(alt, true));
        } else if (!isEmpty(altTitle)) {
            sb.append(XMLUtil.getEscapedXMLForAttribute(altTitle, true));
        } else if (!isEmpty(imageProperties.getAltTitle())) {
            sb.append(XMLUtil.getEscapedXMLForAttribute(imageProperties.getAltTitle(), true));
        }
        sb.append("\"");
        if (!isEmpty(title)) {
            XMLUtil.addNameEqualsValueAttribute(sb, "title", title, true);
        } else if (!isEmpty(altTitle)) {
            XMLUtil.addNameEqualsValueAttribute(sb, "title", altTitle, true);
        } else if (!isEmpty(imageProperties.getAltTitle())) {
            XMLUtil.addNameEqualsValueAttribute(sb, "title", imageProperties.getAltTitle(), true);
        }
        if (!isEmpty(cssClass) || !isEmpty(imageProperties.getForceCssClass())) {
            sb.append(" class=\"");
            if (!isEmpty(cssClass)) {
                sb.append(cssClass);
            }
            if (!isEmpty(imageProperties.getForceCssClass())) {
                if (!isEmpty(cssClass)) {
                    sb.append(" ");
                }
                sb.append(imageProperties.getForceCssClass());
            }
            sb.append("\"");
        }
        if (!isEmpty(cssStyle)) {
            sb.append(" style=\"");
            sb.append(cssStyle);
            sb.append("\"");
        }
        if (!isEmpty(onclick)) {
            sb.append(" onclick=\"");
            sb.append(onclick);
            sb.append("\"");
        }
        if (!isEmpty(align)) {
            sb.append(" align=\"");
            sb.append(align);
            sb.append("\"");
        }
        if (!isEmpty(valign)) {
            sb.append(" valign=\"");
            sb.append(valign);
            sb.append("\"");
        }
        if (!isEmpty(useMap)) {
            sb.append(" usemap=\"");
            sb.append(useMap);
            sb.append("\"");
        }

        // jw: if we are rendering an image for an HTML email, and the image is set to not use a border then lets make
        //     sure we set the border="0" attribute as well, this way the border will be suppressed for Gmail as well.
        Boolean isJspEmail = (Boolean) pageContext.getAttribute(NetworkMailUtil.IS_JSP_EMAIL_ATTR_NAME, PageContext.REQUEST_SCOPE);
        if (isJspEmail != null && isJspEmail && NO_BORDER.equals(imageProperties.getForceCssClass())) {
            sb.append(" border=\"0\"");
        }

        sb.append(" />");

        return sb.toString();
    }

    public String getOutputString() {
        AuthZone authZone = networkContext().getAuthZone();

        StaticImageProperties imageProperties = getStaticImageProperties(src, authZone);
        ImageUrlMetaData metaData = getImageUrlMetaData(pageContext, authZone, src);

        return getOutputString(metaData, width, height, imageProperties);
    }

    public static StaticImageProperties getStaticImagePropertiesFromGlobalLookupMap(String src) {
        // jw: by not specifying a authZone we will ensure that the lookup happens from the global map!
        return getStaticImageProperties(src, null);
    }

    private static StaticImageProperties getStaticImageProperties(String src, AuthZone authZone) {
        Map<String, StaticImageProperties> lookupMap = getStaticImageLookup(authZone);
        StaticImageProperties image = lookupMap.get(src);
        if (image != null) {
            return image;
        }
        // jw: if we are using the global lookup map then throw a exception because the image is not mapped.
        if (lookupMap == IMAGE_LOOKUP) {
            throw UnexpectedError.getRuntimeException("Specified an unrecognized image file for img tag! src: " + src);
        }
        // jw: If this is a dev Or QA environment lets throw a exception that the grouped image could not be found,
        //     otherwise look it up in the global map after logging an error so that we know that a grouped image is
        //     configured incorrectly.
        String error = "Failed looking up grouped StaticImageProperties for src/" + src + " authZone/" + authZone;
        if (NetworkRegistry.getInstance().isLocalServer()) {
            throw UnexpectedError.getRuntimeException(error);
        } else {
            if (logger.isErrorEnabled()) {
                logger.error(error);
            }
            StatisticManager.recordException(UnexpectedError.getRuntimeException(error), false, null);
        }

        // jw: now lets call ourselves again, but this time we will get the properties from the global map which will
        //     throw an exception if the image is still not found.
        return getStaticImagePropertiesFromGlobalLookupMap(src);
    }

    public static Map<String, StaticImageProperties> getStaticImageLookup(AuthZone authZone) {
        return IMAGE_LOOKUP;
    }

    public static ImageUrlMetaData getImageUrlMetaData(String src) {
        return getImageUrlMetaData(null, src);
    }

    public static ImageUrlMetaData getImageUrlMetaData(PageContext pageContext, String src) {
        return getImageUrlMetaData(pageContext, isNetworkContextSet() ? networkContext().getAuthZone() : null, src);
    }

    public static ImageUrlMetaData getImageUrlMetaData(PageContext pageContext, AuthZone authZone, String src) {
        StaticImageProperties imageProperties = getStaticImageProperties(src, authZone);

        return new ImageUrlMetaData(isNetworkContextSet() && networkContext().isUseSecureUrls() ? imageProperties.getDefaultSecureFileUrl() : imageProperties.getDefaultFileUrl(), imageProperties.getWidth(), imageProperties.getHeight());
    }

    // jw: looks up images by relative Url
    private static Map<String, StaticImageProperties> IMAGE_LOOKUP;

    @SuppressWarnings("MagicNumber")
    public static void initStaticImageMap() {
        if (IMAGE_LOOKUP != null) {
            return;
        }
        synchronized (StaticImageTag.class) {
            if (IMAGE_LOOKUP != null) {
                return;
            }

            // jw: lets use this utility class to register all images:
            StaticImageSetup setup = new StaticImageSetup();

            // bl: the default avatar can be displayed in emails (e.g. new user registration)
            setup.addImage("defaultMemberAvatar", User.DEFAULT_AVATAR_PATH, 600, 600, null, NO_BORDER);
            setup.addImage("defaultLargeMemberAvatar", User.DEFAULT_LARGE_AVATAR_PATH, 780, 780, null, NO_BORDER);

            ////////////////////////////////
            // bl: the following images are used in the cluster control panel
            ////////////////////////////////

            // bl: this is used in the please wait popup in the cluster cp
            setup.addImage("defaultCustomGraphic.pleaseWait", "/images/cluster/loading.gif", 100, 100, null, NO_BORDER);

            setup.addImage("icon.18.delete", "/images/cluster/trashcan18px.png", 18, 18, null, NO_BORDER);

            // bl: these 3 are used in the control panel vertical nav in 1.0
            setup.addImage("icon.18.collapseSection", "/images/cluster/arrow-down-expanded-dkgrey-18px.png", 18, 18, null, NO_BORDER);
            setup.addImage("icon.18.expandSection", "/images/cluster/arrow-right-collapsed-dkgrey-18px.png", 18, 18, null, NO_BORDER);
            // jw: the last usages of this tag without alts/titles are on the wysiwyg editor, and those are too diverse to handle generically
            setup.addImage("icon.18.defaultNav", "/images/cluster/default-nav-icon18px.png", 18, 18, null, NO_BORDER);

            IMAGE_LOOKUP = Collections.unmodifiableMap(setup.imageLookup);
        }
    }

    private static class StaticImageSetup {
        private final Set<String> imageIds = newHashSet();
        private final Map<String, StaticImageProperties> imageLookup = newHashMap();

        private void addImage(String id, String relativeFilePath, int width, int height, String defaultAltTitleWordletKey, String forceCssClass) {
            assert !isEmpty(id) && id.length() <= 50 : "Should always provide an id that is not empty and is no larger than 50";
            assert !imageIds.contains(id) : "Attempting to add multiple images with the same ID/" + id;
            assert !imageLookup.containsKey(relativeFilePath) : "Attempting to add multiple images with the same relativeFilePath/" + relativeFilePath;

            StaticImageProperties properties = new StaticImageProperties(id, relativeFilePath, width, height, defaultAltTitleWordletKey, forceCssClass);

            // add the image data to the global lookups first
            imageIds.add(id);
            imageLookup.put(relativeFilePath, properties);
        }

    }

}

