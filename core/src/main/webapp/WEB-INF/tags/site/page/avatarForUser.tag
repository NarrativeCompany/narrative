<%@ tag import="org.narrative.network.core.fileondisk.image.ImageType" %>
<%@ tag import="org.narrative.network.core.user.User" %>
<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ attribute name="user" required="true" type="org.narrative.network.core.user.User"%>

<%@ attribute name="cssClass" type="java.lang.String" %>
<%@ attribute name="alt" type="java.lang.String" %>
<%@ attribute name="title" type="java.lang.String" %>
<%@ attribute name="isLinkUserAvatar" type="java.lang.Boolean" %>
<%@ attribute name="alternateUserUrl" type="java.lang.String" %>
<%@ attribute name="ignoreUserVisibleFlag" type="java.lang.Boolean" %>
<%@ attribute name="hideAvatarWhenNoAvatar" type="java.lang.Boolean" %>
<%@ attribute name="imageDimensionOverride" type="java.lang.Integer" %>
<%@ attribute name="emailCssClass" type="org.narrative.network.shared.email.EmailCssClass" %>
<%@ attribute name="useTooltip" type="java.lang.Boolean" %>
<%@ attribute name="dontResolveEmailCssClasses" type="java.lang.Boolean" %>
<%@ attribute name="excludeDimensions" type="java.lang.Boolean" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy"%>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>
<%@ taglib prefix="s_page" tagdir="/WEB-INF/tags/site/page" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/components" %>

<ss:ref var="networkRegistry" className="org.narrative.network.core.system.NetworkRegistry"/>
<ss:ref var="contextHolder" className="org.narrative.network.shared.context.NetworkContextHolder"/>
<ss:ref var="isJspEmail" className="java.lang.Boolean" />
<ss:set var="areaContext" object="${h:areaContext(pageContext)}" className="org.narrative.network.shared.context.AreaContext" />

<ss:set var="thumbnailType" object="<%=ImageType.SQUARE_THUMBNAIL%>" className="org.narrative.network.core.fileondisk.image.ImageType" />
<ss:set var="largeThumbnailType" object="<%=ImageType.LARGE_SQUARE_THUMBNAIL%>" className="org.narrative.network.core.fileondisk.image.ImageType" />

<%-- bl: always showing user avatars as square thumbnails now --%>
<%-- jw: We now support large thumbnails for avatars, so we can render these at up to 300x300. --%>
<%-- jw: adding support for using a croppedImageThumbnail when requested larger than 300x300. --%>
<c:choose>
    <c:when test="${imageDimensionOverride gt largeThumbnailType.maxWidth}">
        <ss:set var="imageType" object="<%=ImageType.LARGE%>" className="org.narrative.network.core.fileondisk.image.ImageType" />
    </c:when>
    <c:otherwise>
        <ss:set var="imageType" object="${largeThumbnailType}" className="org.narrative.network.core.fileondisk.image.ImageType" />
        <c:if test="${imageDimensionOverride == null || imageDimensionOverride <= 0}">
            <ss:set var="imageDimensionOverride" object="${thumbnailType.maxWidth}" className="java.lang.Integer" />
        </c:if>
    </c:otherwise>
</c:choose>

<c:choose>
    <c:when test="${user.hasAvatarSet and not user.deleted and (user.visible or ignoreUserVisibleFlag)}">
        <c:choose>
            <c:when test="${imageType.squareThumbnail}">
                <ss:set var="avatarUrl" object="${user.squareThumbnailAvatarUrl}" className="java.lang.String" />
            </c:when>
            <c:when test="${imageType.largeSquareThumbnail}">
                <ss:set var="avatarUrl" object="${user.largeSquareThumbnailAvatarUrl}" className="java.lang.String" />
            </c:when>
            <c:otherwise>
                ${ss:assert(imageType.large, 'expected large image type!')}
                <ss:set var="avatarUrl" object="${user.largeAvatarUrl}" className="java.lang.String" />
            </c:otherwise>
        </c:choose>
    </c:when>
    <c:when test="${not hideAvatarWhenNoAvatar}">
        <ss:set var="defaultAvatarPath" object="<%=User.DEFAULT_AVATAR_PATH%>" className="java.lang.String" />
        <ss:set var="defaultLargeAvatarPath" object="<%=User.DEFAULT_LARGE_AVATAR_PATH%>" className="java.lang.String" />
        <ss:set var="avatarUrl" object="${h:staticImageUrlMetaData(pageContext, imageType.squareThumbnail ? defaultAvatarPath : defaultLargeAvatarPath).url}" className="java.lang.String" />
    </c:when>
</c:choose>

<c:if test="${not empty avatarUrl}">
    <c:if test="${not excludeDimensions}">
        <ss:set var="imageWidth" object="${imageDimensionOverride gt 0 ? imageDimensionOverride : imageType.maxWidth}" className="java.lang.Integer" />
        <ss:set var="imageHeight" object="${imageDimensionOverride gt 0 ? imageDimensionOverride : imageType.maxHeight}" className="java.lang.Integer" />
    </c:if>
    <ss:set var="displayNameText">
        <s_page:displayNameText role="${user}" />
    </ss:set>
    <ss:set var="avatarTitle" object="${not empty title ? title : displayNameText}" className="java.lang.String" />
    <ss:set var="avatarAlt" object="${not empty alt ? alt : displayNameText}" className="java.lang.String" />

    <ss:set var="avatarImg">
        <c:choose>
            <c:when test="${isJspEmail}">
                <e:img src="${avatarUrl}" width="${imageWidth}" height="${imageHeight}" alt="${avatarAlt}" title="${avatarTitle}" cssClass="${emailCssClass}" dontResolveCss="${dontResolveEmailCssClasses}" />
            </c:when>
            <c:otherwise>
                <ss:set var="memberAvatarCssClasses" object="${ss:newStringBuilder()}" className="java.lang.StringBuilder" />
                <ss:forEach obj="circle" items="${user.loneAreaUser.effectiveAreaCircles}" className="org.narrative.network.core.security.area.community.advanced.AreaCircle">
                    <%-- bl: only include circles that are not limited to admins only (those are considered "public") just like we include them in circleLabels.tag. --%>
                    <c:if test="${not circle.viewableByAdminsOnly}">
                        <ss:set var="circleCssClass">h-member-circle-${circle.cssClassSuffix}</ss:set>
                        ${ss:addCssClass(memberAvatarCssClasses, circleCssClass)}
                    </c:if>
                </ss:forEach>
                <%-- bl: also add the supplied CSS class --%>
                ${ss:addCssClass(memberAvatarCssClasses, cssClass)}

                <c:choose>
                    <c:when test="${imageType.large}">
                        <comp:croppedImageThumbnail
                                imageUrl="${avatarUrl}"
                                cssClasses="${memberAvatarCssClasses}"
                        />
                    </c:when>
                    <c:otherwise>
                        <img src="${avatarUrl}"
                             class="member-avatar ${memberAvatarCssClasses}"
                             aria-label="${ss:escapeQuotes(avatarTitle)}"
                             title="${ss:escapeQuotes(avatarTitle)}"
                             alt="${avatarAlt}"
                             ${ss:condAttr('width', imageWidth)}
                             ${ss:condAttr('height', imageHeight)}
                             ${not useTooltip ? null : ss:foundationTooltipAttributeString(avatarTitle, false)}
                        />
                    </c:otherwise>
                </c:choose>
            </c:otherwise>
        </c:choose>
    </ss:set>

    <c:choose>
        <c:when test="${not isLinkUserAvatar or not user.visible}">
            ${avatarImg}
        </c:when>
        <c:when test="${isJspEmail}">
            <e:a href="${not empty alternateUserUrl ? alternateUserUrl : user.permalinkUrl}" dontResolveCss="${dontResolveEmailCssClasses}">${avatarImg}</e:a>
        </c:when>
        <c:otherwise>
            <ss:set var="avatarLink">
                <a href="${not empty alternateUserUrl ? alternateUserUrl : user.profileUrl}">${avatarImg}</a>
            </ss:set>
            ${avatarLink}
        </c:otherwise>
    </c:choose>
</c:if>
