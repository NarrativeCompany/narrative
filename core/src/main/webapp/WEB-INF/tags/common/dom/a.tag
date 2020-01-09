<%--
  User: jonmark
  Date: 3/6/15
  Time: 4:00 PM
--%>
<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ attribute name="id" type="java.lang.String" %>

<%@ attribute name="href" type="java.lang.String" %>

<%@ attribute name="modalPopupId" type="java.lang.String" %>

<%@ attribute name="dropDownId" type="java.lang.String" %>
<%@ attribute name="dropDownCssClass" type="java.lang.String" %>
<%@ attribute name="isDropDownMenu" type="java.lang.Boolean" %>
<%@ attribute name="closeDropDownOnClick" type="java.lang.Boolean" %>
<%@ attribute name="showDropDownOnHover" type="java.lang.Boolean" %>
<%@ attribute name="addDropdownToEndOfPage" type="java.lang.Boolean" %>
<%@ attribute name="isOnTop" type="java.lang.Boolean" %>
<%@ attribute name="hoverDelayMs" type="java.lang.Integer" %>

<%@ attribute name="delegationTargetClass" type="java.lang.String" %>

<%@ attribute name="itemprop" type="java.lang.String" %>

<%@ attribute name="extraAttributes" type="java.util.Map" %>
<%@ attribute name="ajaxParams" type="java.util.Map" %>
<%@ attribute name="modalParams" type="java.util.Map" %>

<%@ attribute name="cssClasses" type="java.lang.String" %>
<%@ attribute name="tooltip" type="java.lang.String" %>
<%@ attribute name="excludeHasTip" type="java.lang.Boolean" %>
<%@ attribute name="noFollow" type="java.lang.Boolean" %>
<%@ attribute name="targetBlank" type="java.lang.Boolean" %>

<%@ attribute name="showElementIds" type="java.lang.String" %>
<%@ attribute name="hideElementIds" type="java.lang.String" %>

<%@ attribute name="loadEmbeddedLinkLocally" type="java.lang.Boolean" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/components" %>
<%@ taglib prefix="page" tagdir="/WEB-INF/tags/common/page" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>
<%@ taglib prefix="dom" tagdir="/WEB-INF/tags/common/dom" %>
<%@ taglib prefix="s_page" tagdir="/WEB-INF/tags/site/page" %>

<ss:set var="linkText"><jsp:doBody /></ss:set>

${ss:assert(not empty href or not empty modalPopupId or not empty dropDownId or not empty id or not empty delegationTargetClass or not empty showElementIds or not empty hideElementIds, 'You must always provide a id unless you are targeting a specific URL, dropdown, or popup!')}

<ss:set var="dropDownMenuId">
    <c:choose>
        <c:when test="${not empty dropDownId}">${dropDownId}</c:when>
    </c:choose>
</ss:set>

${ss:assert(empty dropDownMenuId or empty modalPopupId, 'Can not use modalPopup and dropdown on the same link!')}

<ss:set var="dataOptions">
    <%-- bl: hover_delay is a custom setting used by our dropdown.js to add a delay on hover before showing the dropdown. --%>
    <c:if test="${not empty dropDownMenuId and showDropDownOnHover}">is_hover:true;<c:if test="${hoverDelayMs>0}">hover_delay:${hoverDelayMs};</c:if></c:if>
    <c:if test="${isOnTop}">align: top; ignore_repositioning:true;</c:if>
</ss:set>

<ss:set var="hasTooltip" object="${not empty tooltip}" className="java.lang.Boolean" />
<ss:set var="resolvedCssClasses" object="${ss:newStringBuilder()}" className="java.lang.StringBuilder" />
${ss:addCssClass(resolvedCssClasses, cssClasses)}
${ss:addCssClass(resolvedCssClasses, delegationTargetClass)}
<c:if test="${hasTooltip and not excludeHasTip}">
    ${ss:addCssClass(resolvedCssClasses, 'has-tooltip')}
</c:if>

<ss:set var="attributeMap" object="${ss:newMap()}" className="java.util.Map" />
${ss:mapPutNonEmpty(attributeMap, 'id', id)}
${ss:mapPutNoNulls(attributeMap, 'target', targetBlank ? '_blank' : null)}
${ss:mapPutNonEmpty(attributeMap, 'href', href)}
${ss:mapPutNonEmpty(attributeMap, 'data-dropdown', dropDownMenuId)}
${ss:mapPutNonEmpty(attributeMap, 'aria-controls', dropDownMenuId)}
${ss:mapPutNonEmpty(attributeMap, 'data-options', dataOptions)}
${ss:mapPutNonEmpty(attributeMap, 'data-reveal-id', modalPopupId)}
<c:if test="${not empty ajaxParams}">
    <ss:set var="ajaxParamPrefix" object="${not empty dropDownMenuId ? 'data-ajax-dropdown-param-' : 'data-ajax-modal-param-'}" className="java.lang.String" />
    <ss:forEach items="${ajaxParams}" obj="entry" className="java.util.Map.Entry">
        <ss:set var="paramName" object="${entry.key}" className="java.lang.String" />
        <ss:set var="paramValue" object="${entry.value}" className="java.lang.Object" />
        ${ss:mapPut(attributeMap, ss:concat(ajaxParamPrefix, paramName), paramValue)}
    </ss:forEach>
    <c:choose>
        <c:when test="${not empty dropDownMenuId}">
            ${ss:mapPut(attributeMap, showDropDownOnHover ? 'data-ajax-dropdown-with-params-hover' : 'data-ajax-dropdown-with-params-click', null)}
        </c:when>
        <c:otherwise>
            ${ss:assert(not empty modalPopupId, 'Should only provide ajaxParams for dropdowns and modal popups!')}
            ${ss:mapPut(attributeMap, 'data-ajax-modal-popup-with-params', null)}
        </c:otherwise>
    </c:choose>
</c:if>
<c:if test="${not empty modalParams}">
    ${ss:assert(not empty modalPopupId, 'Should only provide modalParams for modal popups!')}
    <ss:set var="modalParamPrefix" object="data-ajax-modal-" className="java.lang.String" />
    <ss:forEach items="${modalParams}" obj="entry" className="java.util.Map.Entry">
        <ss:set var="paramName" object="${entry.key}" className="java.lang.String" />
        <ss:set var="paramValue" object="${entry.value}" className="java.lang.Object" />
        ${ss:mapPut(attributeMap, ss:concat(modalParamPrefix, paramName), paramValue)}
    </ss:forEach>
    ${ss:mapPut(attributeMap, 'data-ajax-modal-popup-with-params', null)}
</c:if>

<c:if test="${not empty showElementIds}">
    ${ss:mapPut(attributeMap, 'data-show-element-ids', showElementIds)}
    ${ss:addCssClass(resolvedCssClasses, 'show-elements')}
</c:if>
<c:if test="${not empty hideElementIds}">
    ${ss:mapPut(attributeMap, 'data-hide-element-ids', hideElementIds)}
    ${ss:addCssClass(resolvedCssClasses, 'hide-elements')}
</c:if>

${ss:mapPutNonEmpty(attributeMap, 'class', resolvedCssClasses)}
<%-- bl: don't include the has-tip class since it will have been included above (and we don't want to overwrite the class attribute we already added!) --%>
${ss:mapPutAll(attributeMap, ss:foundationTooltipAttributes(tooltip, false))}
${ss:mapPutAll(attributeMap, extraAttributes)}
<c:if test="${noFollow}">
    ${ss:mapPutNoNulls(attributeMap, 'rel', 'nofollow')}
</c:if>
<c:if test="${not empty itemprop}">
    ${ss:mapPut(attributeMap, 'itemprop', itemprop)}
</c:if>

<c:if test="${loadEmbeddedLinkLocally}">
    <%--
        jw: if a link should be loaded locally lets add that attribute.  Cache the attribute into memory for efficiency.
            Adding this logic here instead of trying to do it in embeddedContentWrapper because we resolve the body of
            some embedded pages before the wrapper, so the variable wouldnt be defined until after the link was generated.
    --%>
    <ss:ref var="global_embeddedLinkDataAttributes" className="java.util.Map" />
    <c:if test="${global_embeddedLinkDataAttributes eq null}">
        <ss:set var="global_embeddedLinkDataAttributes" object="${ss:newMap()}" className="java.util.Map" scope="request" />
        ${ss:mapPut(global_embeddedLinkDataAttributes, 'data-load-internal', null)}
    </c:if>
    ${ss:mapPutAll(attributeMap, global_embeddedLinkDataAttributes)}
</c:if>

<%-- bl: set as variable first so that the output has whitespace trimmed --%>
<ss:set var="linkHtml">
    <a${ss:htmlAttributes(attributeMap)}>${linkText}</a>
</ss:set>
${linkHtml}
