<%@ page import="org.narrative.network.shared.interceptors.NetworkExceptionInterceptor" %>
<%@ page contentType="text/html;charset=UTF-8" session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="sss" uri="http://www.narrative.org/tags/struts"%>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="page" tagdir="/WEB-INF/tags/common/page" %>
<%@ taglib prefix="s_page" tagdir="/WEB-INF/tags/site/page" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/components" %>

<%@ taglib prefix="ge" tagdir="/WEB-INF/tags/legacy/eve" %>

<%-- bl: exceptionHolder is put on the ActionContext in NetworkExceptionInterceptor. --%>
<ss:set var="throwableContextParam" object="<%=NetworkExceptionInterceptor.THROWABLE_CONTEXT_PARAM%>" className="java.lang.String" />
<sss:set var="throwable" ognl="#${throwableContextParam}" className="java.lang.Throwable" />
<ss:set var="appError" object="${throwable}" className="org.narrative.common.util.ApplicationError" setNullIfNotOfType="true" />
<%-- bl: special handling for access violations to display the rights that were "violated" --%>
<ss:set var="accessViolation" object="${appError}" className="org.narrative.network.shared.security.AccessViolation" setNullIfNotOfType="true" />
<ss:set var="securableThatFailed" object="${accessViolation.securable}" className="org.narrative.network.shared.security.Securable" />

<ss:ref var="action" className="org.narrative.network.shared.baseactions.NetworkAction" />
<ss:ref var="networkContext" className="org.narrative.network.shared.context.NetworkContext" />
<ss:set var="areaContext" object="${h:areaContext(pageContext)}" className="org.narrative.network.shared.context.AreaContext" />

<ss:set var="title">
    <c:choose>
        <c:when test="${appError!=null and not empty appError.title}">
            ${appError.title}
        </c:when>
        <c:otherwise>
            ${h:wordlet('error.title')}
        </c:otherwise>
    </c:choose>
</ss:set>

<ge:clusterWrapper>
    <c:choose>
        <c:when test="${appError!=null and not empty appError.message}">
            <c:if test="${not empty appError.title}">
                <h1>${appError.title}</h1>
            </c:if>

            <div>
                ${appError.message}
            </div>

            <c:if test="${securableThatFailed!=null}">
                <div class="permission-error">
                    ${h:wordlet('jsp.common.s_exception.youNeedTheFollowingRight')}
                    <ul>
                        <li>${securableThatFailed.nameForDisplay}</li>
                    </ul>
                </div>
            </c:if>
        </c:when>
        <c:otherwise>
            <ss:set var="referenceId">${networkContext.actionProcess.processOid}[${ss:exceptionHashCode(throwable)}][${ss:exceptionRootCauseHashCode(throwable)}]${action.networkRegistry.version}-${action.networkRegistry.clusterId}.${action.networkRegistry.servletName}</ss:set>
            <ss:set var="unexpectedError" object="${throwable}" className="org.narrative.common.util.UnexpectedError" setNullIfNotOfType="true" />
            ${h:wordlet1Arg('error.pageYouRequestedNotAvailable', referenceId)}
        </c:otherwise>
    </c:choose>

    <c:if test="${action.networkRegistry.localOrDevServer and appError==null}">
        <div>
            <b>The following stack trace is being displayed since this environment is configured as a QA environment.
            This message will never appear in a production environment (since they will not be configured as QA environments).</b>
        </div>
        <sss:set var="extraExceptionSystemLogInfo" ognl="#extraExceptionSystemLogInfo" />
        <c:if test="${not empty extraExceptionSystemLogInfo}">
            <div>
                Extra exception system log info: ${extraExceptionSystemLogInfo}
            </div>
        </c:if>
        <sss:set var="stackTraceHtml" ognl="#stackTraceHtml" />
        <pre class="stack-trace">
            ${stackTraceHtml}
        </pre>
    </c:if>
</ge:clusterWrapper>
