<%@ page import="org.narrative.network.shared.interceptors.NetworkExceptionInterceptor" %>
<?xml version="1.0" encoding="UTF-8"?>
<%@ page contentType="text/xml;charset=UTF-8" session="false" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network"%>
<%@ taglib prefix="sss" uri="http://www.narrative.org/tags/struts"%>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>

<%-- bl: exceptionHolder is put on the ActionContext in NetworkExceptionInterceptor. --%>
<g:set var="throwableContextParam" object="<%=NetworkExceptionInterceptor.THROWABLE_CONTEXT_PARAM%>" className="java.lang.String" />
<sss:set var="throwable" ognl="#${throwableContextParam}" className="java.lang.Throwable" />

<g:set var="appError" object="${throwable}" className="org.narrative.common.util.ApplicationError" setNullIfNotOfType="true" />

<g:ref var="action" className="org.narrative.network.shared.baseactions.NetworkAction" />
<g:ref var="networkContext" className="org.narrative.network.shared.context.NetworkContext" />

<g:set var="title">
    <c:choose>
        <c:when test="${appError!=null and not empty appError.title}">${appError.title}</c:when>
        <c:otherwise>${gn:wordlet('error.title')}</c:otherwise>
    </c:choose>
</g:set>

<g:set var="referenceId">${networkContext.actionProcess.processOid}[${g:exceptionHashCode(throwable)}][${g:exceptionRootCauseHashCode(throwable)}]${action.networkRegistry.version}-${action.networkRegistry.clusterId}.${action.networkRegistry.servletName}-2</g:set>

<g:set var="message">
    <c:choose>
        <c:when test="${appError!=null and not empty appError.message}">${appError.message}</c:when>
        <c:otherwise>
            ${gn:wordlet1Arg('error.unknownSystemErrorRefId', referenceId)}
        </c:otherwise>
    </c:choose>
    <br /><br />
    ${gn:wordlet1Arg('error.ajaxErrorClickToContinue','javascript:document.location.reload();')}
    
    <c:if test="${action.networkRegistry.localOrDevServer and appError==null}">
        <br /><br />
        <b>The following stack trace is being displayed since this environment is configured as a QA environment.
        This message will never appear in a production environment (since they will not be configured as QA environments).</b>
        <br /><br />
        <sss:set var="extraExceptionSystemLogInfo" ognl="#extraExceptionSystemLogInfo" />
        <c:if test="${not empty extraExceptionSystemLogInfo}">
            Extra exception system log info: ${extraExceptionSystemLogInfo}
        </c:if>
        <sss:set var="stackTraceHtml" ognl="#stackTraceHtml" />
        ${stackTraceHtml}
    </c:if>
</g:set>

<gct:ajaxErrorMessage title="${title}" message="${message}" />