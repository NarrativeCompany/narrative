<%@ tag pageEncoding="UTF-8" body-content="empty"%>
<%@ attribute name="type" %>
<%@ attribute name="name" %>
<%@ attribute name="value" %>
<%@ attribute name="align" %>
<%@ attribute name="src" description="If type=='image', then the src of the image for the image submit button." %>
<%@ attribute name="disabled" type="java.lang.Boolean" %>
<%@ attribute name="tabindex" %>
<%@ attribute name="id" %>
<%@ attribute name="label" %>
<%@ attribute name="cssClass" %>
<%@ attribute name="wrapperCssClass" %>
<%@ attribute name="wrapperCssStyle" %>
<%@ attribute name="cssStyle" %>
<%@ attribute name="title" %>
<%@ attribute name="onclick" description="Set the html onclick attribute on rendered html element" %>
<%@ attribute name="ondblclick" description="Set the html ondblclick attribute on rendered html element" %>
<%@ attribute name="onmousedown" description="Set the html onmousedown attribute on rendered html element" %>
<%@ attribute name="onmouseup" description="Set the html onmouseup attribute on rendered html element" %>
<%@ attribute name="onmouseover" description="Set the html onmouseover attribute on rendered html element" %>
<%@ attribute name="onmousemove" description="Set the html onmousemove attribute on rendered html element" %>
<%@ attribute name="onmouseout" description="Set the html onmouseout attribute on rendered html element" %>
<%@ attribute name="onfocus" description="Set the html onfocus attribute on rendered html element" %>
<%@ attribute name="onblur" description="Set the html onblur attribute on rendered html element" %>
<%@ attribute name="onkeypress" description="Set the html onkeypress attribute on rendered html element" %>
<%@ attribute name="onkeydown" description="Set the html onkeydown attribute on rendered html element" %>
<%@ attribute name="onkeyup" description="Set the html onkeyup attribute on rendered html element" %>
<%@ attribute name="onselect" description="Set the html onselect attribute on rendered html element" %>
<%@ attribute name="onchange" description="Set the html onchange attribute on rendered html element" %>

<%@ attribute name="inputId" description="Set the html onchange attribute on rendered html element" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="gtu" uri="http://legacy.narrative.org/tags/tagUtils" %>

<g:ref var="networkContext" className="org.narrative.network.shared.context.NetworkContext" />

<c:if test="${empty inputId}">
    <g:set var="nameForId" object="${not empty name ? name : g:concat('formButtonName', g:seq())}" />

    <g:set var="inputId" object="${gtu:createInputId(id,formId,nameForId)}"/>
</c:if>


<g:set var="buttonHtml">
<button
    ${g:condAttr('id',inputId)}
    ${g:condAttr('type',type)}
    ${g:condAttr('name',name)}
    ${g:condAttr('value',value)}
    ${g:condAttr('class',cssClass)}
    ${g:condAttr('style',cssStyle)}
    ${g:condAttr('tabindex',tabindex)}
    ${g:condAttr('onclick',onclick)}
    ${g:condAttr('ondblclick',ondblclick)}
    ${g:condAttr('onmousedown',onmousedown)}
    ${g:condAttr('onmouseup',onmouseup)}
    ${g:condAttr('onmouseover',onmouseover)}
    ${g:condAttr('onmousemove',onmousemove)}
    ${g:condAttr('onmouseout',onmouseout)}
    ${g:condAttr('onfocus',onfocus)}
    ${g:condAttr('onblur',onblur)}
    ${g:condAttr('onkeypress',onkeypress)}
    ${g:condAttr('onkeydown',onkeydown)}
    ${g:condAttr('onkeyup',onkeyup)}
    ${g:condAttr('onselect',onselect)}
    ${g:condAttr('onchange',onchange)}
        >${label}</button>
</g:set>

<c:choose>
    <c:when test="${not empty align}">
        <div ${g:attr('id',g:concat('wwctrl_',inputId))} align="${align}" class="yui-skin-sam ${wrapperCssClass}"${g:condAttr('style', wrapperCssStyle)}>${buttonHtml}</div>
    </c:when>
    <c:otherwise>
        <span ${g:attr('id',g:concat('wwctrl_',inputId))} class="yui-skin-sam ${wrapperCssClass}"${g:condAttr('style', wrapperCssStyle)}>${buttonHtml}</span>
    </c:otherwise>
</c:choose>