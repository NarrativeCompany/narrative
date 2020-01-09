<%@ tag pageEncoding="UTF-8" body-content="empty"%>
<%@ attribute name="name" %>
<%@ attribute name="value" %>
<%@ attribute name="align" %>
<%@ attribute name="disabled" type="java.lang.Boolean" %>
<%@ attribute name="tabindex" %>
<%@ attribute name="id" %>
<%@ attribute name="cssClass" %>
<%@ attribute name="wrapperCssClass" %>
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
<%@ attribute name="buttonText" type="java.lang.String" description="Submits will default to display the value unless the body of the <input /> has text within it, this allows us to optionally set that" %>

<%@ attribute name="inputId" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="gtu" uri="http://legacy.narrative.org/tags/tagUtils" %>

<g:ref var="networkContext" className="org.narrative.network.shared.context.NetworkContext" />

<c:if test="${empty inputId}">
    <g:set var="nameForId" object="${not empty name ? name : g:concat('formSubmitName', g:seq())}" />

    <g:set var="inputId" object="${gtu:createInputId(id,formId,nameForId)}"/>
</c:if>

<g:set var="submitHTML">
    ${gtu:createInput("submit",name,value,null,formId,null,null,null,disabled,null,null,tabindex,inputId,cssClass,
        cssStyle,title,onclick,ondblclick,onmousedown,onmouseup,onmouseover,onmousemove,onmouseout,onfocus,onblur,onkeypress,
        onkeydown,onkeyup,null,null,null,onselect,onchange,null,buttonText)}
</g:set>

<c:choose>
    <c:when test="${not empty align}">
        <div ${g:attr('id',g:concat('wwctrl_',inputId))} align="${align}" class="yui-skin-sam ${wrapperCssClass}">${submitHTML}</div>
    </c:when>
    <c:otherwise>
        <span ${g:attr('id',g:concat('wwctrl_',inputId))} class="yui-skin-sam ${wrapperCssClass}">${submitHTML}</span>
    </c:otherwise>
</c:choose>