<%--
  User: jonmark
  Date: 1/16/14
  Time: 2:30 PM
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" %>

<%@ attribute name="title" required="true" type="java.lang.String" %>
<%@ attribute name="customHeaderColumn" type="java.lang.String" %>
<%@ attribute name="styleCss" type="java.lang.String" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core" %>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network" %>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="gmt" tagdir="/WEB-INF/tags/legacy/master" %>
<%@ taglib prefix="gta" tagdir="/WEB-INF/tags/legacy/area" %>

<g:set var="addHeaderColumn" object="${not empty customHeaderColumn}" className="java.lang.Boolean" />
<g:set var="headerTitleHtml">
    <h1${g:condAttr('class', addHeaderColumn ? 'withAnotherColumn' : null)}>
        ${title}
    </h1>
</g:set>
<c:choose>
    <c:when test="${addHeaderColumn}">
        <table class="generic headerTitle" cellspacing="0"${g:condAttr('style', styleCss)}>
            <tr>
                <c:if test="${not empty title}">
                    <td class="headerColumn">
                        ${headerTitleHtml}
                    </td>
                </c:if>
                <c:if test="${not empty customHeaderColumn}">
                    ${customHeaderColumn}
                </c:if>
            </tr>
        </table>
    </c:when>
    <c:otherwise>
        <c:if test="${not empty headerTitleHtml}">
            <div class="headerTitle"${g:condAttr('style', styleCss)}>
                ${headerTitleHtml}
            </div>
        </c:if>
    </c:otherwise>
</c:choose>
