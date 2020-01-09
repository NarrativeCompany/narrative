<%--
  User: jonmark
  Date: 2/20/15
  Time: 12:41 PM
--%>
<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ attribute name="id" type="java.lang.String" %>
<%@ attribute name="tooltip" type="java.lang.String" %>
<%@ attribute name="itemProperty" type="java.lang.String" %>

<%@ attribute name="isSecondary" type="java.lang.Boolean" %>
<%@ attribute name="isAlert" type="java.lang.Boolean" %>
<%@ attribute name="isWarning" type="java.lang.Boolean" %>
<%@ attribute name="isInfo" type="java.lang.Boolean" %>
<%@ attribute name="isSuccess" type="java.lang.Boolean" %>
<%@ attribute name="isCategory" type="java.lang.Boolean" %>
<%--
    jw: per: http://foundation.zurb.com/docs/components/labels.html
        there is also supportt for info and warning
--%>

<%@ attribute name="isSmall" type="java.lang.Boolean" %>
<%@ attribute name="isTiny" type="java.lang.Boolean" %>
<%@ attribute name="isRadius" type="java.lang.Boolean" %>
<%@ attribute name="isRound" type="java.lang.Boolean" %>

<%@ attribute name="dontSpace" type="java.lang.Boolean" description="By default labels will include the margin-right-5 since the vaste majority of the time that is what we want.  This will suppress that." %>

<%@ attribute name="cssClass" type="java.lang.String" %>

<%@ attribute name="extraAttributes" type="java.util.Map" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>

<ss:set var="hasTooltip" object="${not empty tooltip}" className="java.lang.Boolean" />
<ss:set var="cssClasses" object="${ss:newStringBuilder()}" className="java.lang.StringBuilder" />
${ss:addCssClass(cssClasses, 'label')}

<c:if test="${not dontSpace}">
${ss:addCssClass(cssClasses, 'margin-right-5')}
</c:if>

<tool:foundationClassesHelper
        stringBuilder="${cssClasses}"
        isSecondary="${isSecondary}"
        isSuccess="${isSuccess}"
        isAlert="${isAlert}"
        isWarning="${isWarning}"
        isInfo="${isInfo}"
        isSmall="${isSmall}"
        isTiny="${isTiny}"
        isRadius="${isRadius}"
        isRound="${isRound}"
        extraCssClasses="${cssClass}"
/>
<c:if test="${isCategory}">
    ${ss:addCssClass(cssClasses, 'category')}
</c:if>
<c:if test="${hasTooltip}">
    ${ss:addCssClass(cssClasses, 'has-tooltip')}
</c:if>

<%-- jw: lets process the label through a variable to remove whitespace --%>
<ss:set var="labelHtml">
    <span${ss:condAttr('id', id)} class="${cssClasses}"${ss:foundationTooltipAttributeString(tooltip, false)}${ss:condAttr('itemprop', itemProperty)}${ss:htmlAttributes(extraAttributes)}><jsp:doBody /></span>
</ss:set>

${labelHtml}
