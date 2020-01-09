<%--
  User: april
  Date: May 15, 2009
  Time: 3:47:44 PM
--%>
<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ attribute name="linkName" type="java.lang.String" description="title of link " %>
<%@ attribute name="linkUrl" type="java.lang.String" description="url of link " %>
<%@ attribute name="onClick" type="java.lang.String" description="url of link " %>
<%@ attribute name="id" type="java.lang.String" description="id of div" %>
<%@ attribute name="cssClass" type="java.lang.String" %>
<%@ attribute name="wrapperCssClass" type="java.lang.String" %>
<%@ attribute name="wrapperCssStyle" type="java.lang.String" %>
<%@ attribute name="style" type="java.lang.String" %>
<%@ attribute name="align" type="java.lang.String" %>
<%@ attribute name="buttonType" type="java.lang.String" description="Set this to 'push' to have it treated as a 'regular' button that won't cause the parent form (if any) to be submitted on click." %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core" %>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network"%>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>


<g:ref var="action" className="org.narrative.network.shared.baseactions.NetworkAction"/>

<g:set var="actionButtonId" object="${not empty id ? id : g:concat('actionButton', g:seq())}" />

<c:choose>
    <c:when test="${not empty onClick}">
        <gfs:button
                cssClass="${cssClass}"
                wrapperCssClass="${wrapperCssClass}"
                wrapperCssStyle="${wrapperCssStyle}"
                cssStyle="${style}"
                onclick="${onClick}"
                id="${actionButtonId}"
                label="${linkName}"
                align="${align}"
                buttonType="${buttonType}" />
    </c:when>
    <c:otherwise>
        <c:if test="${empty linkUrl}">
            ${g:throwUnexpectedError('must supply either onClick or linkUrl to actionButton.tag!')}
        </c:if>
        <span class="yui-skin-sam ${wrapperCssClass}"${g:condAttr('style', wrapperCssStyle)}>
            <a class="${cssClass}" style="${style}" href="${linkUrl}" id="${actionButtonId}">${linkName}</a>
        </span>
        <script type="text/javascript">
            ${actionButtonId}Button = new YAHOO.widget.Button('${actionButtonId}');
        </script>
    </c:otherwise>
</c:choose>