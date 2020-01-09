<%@ tag pageEncoding="UTF-8" body-content="empty" %>
<%@ attribute name="name" required="true"%>
<%@ attribute name="value" required="true"%>
<%@ attribute name="enableDisabledHtmlInValue" description="true if the value contains disabled html that should be enabled prior to display for edit.  defaults to true, so specify false to disable this behavior." %>
<%@ attribute name="id" %>
<%@ attribute name="cssClass" %>
<%@ attribute name="cssStyle" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:if test="${enableDisabledHtmlInValue==null || enableDisabledHtmlInValue}">
    <g:set var="value" object="${g:enableDisabledHtml(value)}" />
</c:if>

<input type="hidden" ${g:attr('name',name)} ${g:attr('value',value)}
    <c:if test="${!empty id}">${g:attr('id',id)}</c:if>
    <c:if test="${!empty cssClass}">${g:attr('class',cssClass)}</c:if>
    <c:if test="${!empty cssStyle}">${g:attr('style',cssStyle)}</c:if>
    />