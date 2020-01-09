<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ attribute name="itemLink" type="java.lang.String" %>
<%@ attribute name="itemName" type="java.lang.String" %>
<%@ attribute name="itemLinkStyle" type="java.lang.String" %>
<%@ attribute name="isLast" type="java.lang.Boolean" %>
<%@ attribute name="isFirst" type="java.lang.Boolean" %>
<%@ attribute name="isOn" type="java.lang.Boolean" %>
<%@ attribute name="isSubMenuOn" type="java.lang.Boolean" %>
<%@ attribute name="isHide" type="java.lang.Boolean" %>
<%@ attribute name="isSecondTier" type="java.lang.Boolean" %>
<%@ attribute name="isThirdTier" type="java.lang.Boolean" %>
<%@ attribute name="id" type="java.lang.String" %>
<%@ attribute name="itemOnClick" type="java.lang.String" %>
<%@ attribute name="customTitleWidth" type="java.lang.String" %>

<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core" %>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<g:ref var="action" className="org.narrative.network.shared.baseactions.NetworkAction"/>
<g:ref var="networkContext" className="org.narrative.network.shared.context.NetworkContext" />
<g:set var="areaContext" object="${gn:areaContext(pageContext)}" className="org.narrative.network.shared.context.AreaContext" />

<%-- jw: we are now using the body for the sub menu.  This is much easier and intuitive. --%>
<g:set var="subMenu"><jsp:doBody /></g:set>

<g:set var="cssClassForHighlight">
    <c:choose>
        <c:when test="${isOn and empty subMenu}"> navMenuSelected </c:when>
        <c:when test="${isSecondTier or isThirdTier}"> navMenuSelectedSection </c:when>
        <c:otherwise> navMenuUnselected </c:otherwise>
    </c:choose>
</g:set>
<g:set var="cssClassForTier">
    <c:choose>
        <c:when test="${isThirdTier}"> navMenuTierThree </c:when>
        <c:when test="${isSecondTier}"> navMenuTierTwo </c:when>
        <c:otherwise> navMenuTierOne </c:otherwise>
    </c:choose>
</g:set>


<c:if test="${not isHide}">
    <g:set var="liClassToUse" object="${cssClassForHighlight}${isFirst?' vMenuFirst ':''} ${isLast and not isSubMenuOn?' vMenuLast ':''}"/>

    <g:set var="includeExpandCollapse" object="${not empty subMenu}" className="java.lang.Boolean" />

    <table class="${liClassToUse}"
           cellspacing="0"
           ${g:condAttr('id', id)}
            >
        <tr>
            <g:set var="titleWidth" object="${isThirdTier ? 170 : isSecondTier ? 190 : 215}" className="java.lang.Long" />
            <g:set var="navMenuLeftColumn" className="java.lang.String">
                <td class="menuIcon ${cssClassForTier}">
                    <a href="${itemLink}" ${g:condAttr('style', itemLinkStyle)} ${g:condAttr('onclick', itemOnClick)} ><gn:staticImage src="/images/cluster/default-nav-icon18px.png" altTitle="${itemName}" /></a>
                </td>
            </g:set>

            <g:set var="navMenuRightColumn" className="java.lang.String">
                <c:if test="${not empty subMenu}">
                    <td class="menuIcon">
                        <a href="javascript:void(0);" id="expandCollapseLink${id}"
                           onclick="expandCollapseVerticalMenu(event, '${id}', '${cssClassForHighlight}${isFirst?' vMenuFirst ':''}', ${isLast?'true':'false'});"
                           class="vMenuControl">
                            <gn:staticImage src="${isSubMenuOn ? '/images/cluster/arrow-down-expanded-dkgrey-18px.png' : '/images/cluster/arrow-right-collapsed-dkgrey-18px.png'}" id="expandCollapseImg${id}" />
                        </a>
                    </td>
                </c:if>
            </g:set>

            ${navMenuLeftColumn}

            <c:if test="${empty navMenuLeftColumn}">
                <g:set var="titleWidth" object="${titleWidth + 20}" className="java.lang.Long" />
            </c:if>

            <c:if test="${empty navMenuRightColumn}">
                <g:set var="titleWidth" object="${titleWidth + 20}" className="java.lang.Long" />
            </c:if>

            <td>
                <div style="width:${not empty customTitleWidth ? customTitleWidth : titleWidth}px;word-wrap:break-word;overflow:hidden">
                    <a href="${itemLink}" ${g:condAttr('style', itemLinkStyle)} ${g:condAttr('onclick', itemOnClick)} >
                    ${itemName}
                    </a>
                </div>
            </td>

            ${navMenuRightColumn}

        </tr>
    </table>

    <c:if test="${not empty subMenu}">
        <div class="${isSubMenuOn?'subMenuBarOn ':'subMenuBar '}" id="subMenuBar_${id}">
            ${subMenu}
        </div>
    </c:if>
</c:if>