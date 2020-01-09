<%@ tag import="org.narrative.network.shared.interceptors.*" %>
<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>
<%@ attribute name="id" description="id for referencing element. For UI and form tags it will be used as HTML id attribute" %>
<%@ attribute name="onsubmit" description="HTML onsubmit attribute" %>
<%@ attribute name="action" required="true" description="Set action nane to submit to, without .action suffix" %>
<%@ attribute name="target" description="HTML form target attribute" %>
<%@ attribute name="isMultipart" type="java.lang.Boolean" description="True if this is a multipart form to enable file uploads.  False if it is not.  Defaults to false." %>
<%@ attribute name="method" required="true" description="HTML form method attribute" %>
<%--<%@ attribute name="namespace" description="namespace for action to submit to" %>--%>
<%--<%@ attribute name="validate" description="Whether client side/remote validation should be performed. Only useful with theme xhtml/ajax" %>--%>
<%--<%@ attribute name="portletMode" description="The portlet mode to display after the form submit" %>--%>
<%--<%@ attribute name="windowState" description="The window state to display after the form submit" %>--%>
<%--<%@ attribute name="openTemplate" description="Set template to use for opening the rendered html." %>--%>
<%--<%@ attribute name="theme" description="The theme (other than default) to use for rendering the element" %>--%>
<%--<%@ attribute name="template"  description="The template (other than default) to use for rendering the element" %>--%>
<%@ attribute name="cssClass" description="The css class to use for element" %>
<%@ attribute name="cssStyle" description="The css style definitions for element to use" %>
<%@ attribute name="title" description="Set the html title attribute on rendered html element" %>
<%--<%@ attribute name="disabled" description="Set the html disabled attribute on rendered html element" %>--%>
<%--<%@ attribute name="label" description="Label expression used for rendering a element specific label" %>--%>
<%--<%@ attribute name="labelposition" description="define label position of form element (top/left)" %>--%>
<%--<%@ attribute name="requiredposition" description="define required position of required form element (left|right)" %>--%>
<%@ attribute name="name" description="The name to set for element" %>
<%--<%@ attribute name="required" description="If set to true, the rendered element will indicate that input is required" %>--%>
<%--<%@ attribute name="tabindex" description="Set the html tabindex attribute on rendered html element" %>--%>
<%--<%@ attribute name="value" description="Preset the value of input element." %>--%>
<%--<%@ attribute name="onclick" description="Set the html onclick attribute on rendered html element" %>--%>
<%--<%@ attribute name="ondblclick" description="Set the html ondblclick attribute on rendered html element" %>--%>
<%--<%@ attribute name="onmousedown" description="Set the html onmousedown attribute on rendered html element" %>--%>
<%--<%@ attribute name="onmouseup" description="Set the html onmouseup attribute on rendered html element" %>--%>
<%--<%@ attribute name="onmouseover" description="Set the html onmouseover attribute on rendered html element" %>--%>
<%--<%@ attribute name="onmousemove" description="Set the html onmousemove attribute on rendered html element" %>--%>
<%--<%@ attribute name="onmouseout" description="Set the html onmouseout attribute on rendered html element" %>--%>
<%--<%@ attribute name="onfocus" description="Set the html onfocus attribute on rendered html element" %>--%>
<%--<%@ attribute name="onblur" description="Set the html onblur attribute on rendered html element" %>--%>
<%--<%@ attribute name="onkeypress" description="Set the html onkeypress attribute on rendered html element" %>--%>
<%--<%@ attribute name="onkeydown" description="Set the html onkeydown attribute on rendered html element" %>--%>
<%--<%@ attribute name="onkeyup" description="Set the html onkeyup attribute on rendered html element" %>--%>
<%--<%@ attribute name="onselect" description="Set the html onselect attribute on rendered html element" %>--%>
<%--<%@ attribute name="onchange" description="Set the html onchange attribute on rendered html element" %>--%>
<%--<%@ attribute name="tooltip" description="Set the tooltip of this particular component" %>--%>
<%--<%@ attribute name="tooltipConfig" description="Set the tooltip configuration" %>--%>
<%@ attribute name="showPleaseWaitOnSubmit" type="java.lang.Boolean" description="Controls whether the pleaseWait popup window should appear upon submission of the form.  Useful for forms you wouldn't want submitted twice since it will disable the page." %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network"%>
<%@ taglib prefix="sss" uri="http://www.narrative.org/tags/struts"%>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>

<g:ref var="networkAction" className="org.narrative.network.shared.baseactions.NetworkAction" />
<g:ref var="networkContext" className="org.narrative.network.shared.context.NetworkContext" />
<g:ref var="networkRegistry" className="org.narrative.network.core.system.NetworkRegistry" />
<g:ref var="processOid" className="org.narrative.common.persistence.OID" />

<g:set var="actionName" object="${sss:actionName(action)}" className="java.lang.String" />
<g:ref var="formId" className="java.lang.String" />
<g:set var="originalFormId" object="${formId}" />
<g:set var="formId" object="${empty id ? actionName : id}" className="java.lang.String" scope="request" />
<g:set var="formName" object="${empty name ? formId : name}" />

<g:set var="extraOnSubmitJs">
    <c:if test="${showPleaseWaitOnSubmit}">
         pleaseWaitPopupWindow.show();
    </c:if>
</g:set>

<%-- bl: added this named anchor to facilitate displaying the top of the form in the event of an ajax form error. --%>
<a name="formTop_${formId}"></a>
<form ${g:attr('action',action)} ${g:attr('method',method)} ${g:attr('name',formName)} ${g:attr('id',formId)} ${g:condAttr('onsubmit',g:concat(extraOnSubmitJs, onsubmit))} ${g:condAttr('target',target)} ${isMultipart ? g:attr('enctype','multipart/form-data') : ''} ${g:condAttr('class',cssClass)} ${g:condAttr('style',cssStyle)} ${g:condAttr('title',title)}>
    <%-- bl: only include the formType for POST requests, not GET requests. --%>
    <c:if test="${fn:toLowerCase(method)=='post'}"> 
        <gfs:postValidationParams formId="${formId}" />
    </c:if>
    <%-- only output the errors if this was the form that was submitted --%>
    <c:if test="${formId==networkAction.formType}">
        <g:set var="throwableContextParam" object="<%=NetworkExceptionInterceptor.THROWABLE_CONTEXT_PARAM%>" className="java.lang.String" />
        <sss:set var="throwable" ognl="#${throwableContextParam}" className="java.lang.Throwable" />
        <c:if test="${not empty networkAction.actionErrors or not empty networkAction.fieldErrors or throwable!=null}">
            <gfs:formErrorMessages
                    formId="${formId}"
                    actionErrors="${networkAction.actionErrors}"
                    actionFieldErrors="${networkAction.fieldErrors}"
                    throwable="${throwable}" />
        </c:if>
    </c:if>
    <jsp:doBody />
</form>

<%-- bl: opting to remove this to avoid multiple please wait popups on the same page.  we're already including
     gct:pleaseWaitPopup in the wrapperWidgets, so it should be on every page already.  removing this will prevent
     getting a duplicate please wait popup window defined when returning forms via ajax (as we are doing for forwarding content. --%>
<%--<c:if test="${showPleaseWaitOnSubmit}">
    <gct:pleaseWaitPopup />
</c:if>--%>

<g:set var="formId" object="${originalFormId}" scope="request" />
