<%--
  User: brian
  Date: 7/15/11
  Time: 12:11 PM
--%>
<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ attribute name="formId" required="true" type="java.lang.String" %>
<%@ attribute name="actionErrors" required="false" type="java.util.Collection" %>
<%@ attribute name="actionFieldErrors" required="false" type="java.util.Map" %>
<%@ attribute name="throwable" required="false" type="java.lang.Throwable" %>
<%@ attribute name="cssStyle" description="The css style definitions for element to use" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core" %>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network"%>
<%@ taglib prefix="sss" uri="http://www.narrative.org/tags/struts"%>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>

<g:ref var="action" className="org.narrative.network.shared.baseactions.NetworkAction"/>
<g:ref var="networkContext" className="org.narrative.network.shared.context.NetworkContext" />
<g:ref var="networkRegistry" className="org.narrative.network.core.system.NetworkRegistry" />

<div id="errorMessageContainer_${formId}" class="errorMessageContainer"${g:condAttr('style', cssStyle)}>
    <div id="errorMessageContainerTitle_${formId}" class="errorMessageContainerTitle" ${g:condAttr('style', action.networkContext.reqResp.post ? '' : 'display: none;')}>
        ${gn:wordlet('tags.common.forms.simple.form.formSubmissionErrors')}
    </div>
    <ul id="errorMessages_${formId}">
        <c:if test="${not empty actionErrors}">
            <g:forEach items="${actionErrors}" obj="actionError" className="java.lang.String" varStatus="status">
                <li class="errorMessageLi" id="${formId}_action_error_${status.index}_li"><span class="errorMessage">${actionError}</span></li>
            </g:forEach>
        </c:if>
        <c:if test="${not empty actionFieldErrors}">
            <g:forEach items="${actionFieldErrors}" obj="fieldError" className="java.util.Map.Entry">
                <%-- bl: the value of the map entries are a collection of Strings, so iterate through them as well. --%>
                <g:forEach items="${fieldError.value}" obj="fieldErrorMessage" className="java.lang.String" varStatus="status">
                    <li class="errorMessageLi" id="${formId}_error_${status.index}_li"><span class="errorMessage">${fieldErrorMessage}</span></li>
                </g:forEach>
            </g:forEach>
        </c:if>
        <c:if test="${throwable!=null}">
            <g:set var="appError" object="${throwable}" className="org.narrative.common.util.ApplicationError" setNullIfNotOfType="true" />
            <li class="errorMessageLi" id="${formId}_throwable_error_li">
                <span class="errorMessage">
                    <c:choose>
                        <c:when test="${appError!=null and not empty appError.message}">
                            ${appError.message}
                        </c:when>
                        <c:otherwise>
                            <g:set var="referenceId">${networkContext.actionProcess.processOid}[${g:exceptionHashCode(throwable)}][${g:exceptionRootCauseHashCode(throwable)}]${networkRegistry.version}-${networkRegistry.clusterId}.${networkRegistry.servletName}-3</g:set>
                            ${gn:wordlet('tags.common.forms.simple.form.unfortunatelyErrorOccuredDuringProcessing')}
                            <br /><br />
                            ${gn:wordlet1Arg('tags.common.forms.simple.form.forDiagnosticPurposesErrorReference', referenceId)}
                        </c:otherwise>
                    </c:choose>
                    <c:if test="${networkRegistry.localOrDevServer}">
                        &#160;&#160;<a href="javascript:void(0);" onclick="stackTraceErrorPopupWindow.show(this);">${gn:wordlet('tags.common.forms.simple.form.stackTrace')}</a>
                        <gct:divPopup id="stackTraceError" title="${appError!=null and not empty appError.title ? appError.title : 'Error'}" width="800" height="500" isResizable="true">
                            <b>The following stack trace (and the stack trace link you just clicked) is being displayed since this environment is configured as a QA environment.
                            This message will never appear in a production environment (since they will not be configured as QA environments).</b>
                            <br /><br />
                            <sss:set var="extraExceptionSystemLogInfo" ognl="#extraExceptionSystemLogInfo" />
                            <c:if test="${not empty extraExceptionSystemLogInfo}">
                                Extra exception system log info: ${extraExceptionSystemLogInfo}
                            </c:if>
                            <sss:set var="stackTraceHtml" ognl="#stackTraceHtml" />
                            ${stackTraceHtml}
                        </gct:divPopup>
                    </c:if>
                </span>
            </li>
        </c:if>
    </ul>
</div>