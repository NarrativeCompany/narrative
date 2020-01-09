<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ attribute name="extraHeadTag" %>

<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core" %>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>          
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>

<g:ref var="networkContext" className="org.narrative.network.shared.context.NetworkContext"/>
<g:ref var="networkRegistry" className="org.narrative.network.core.system.NetworkRegistry"/>
<g:set var="areaContext" object="${gn:areaContext(pageContext)}" className="org.narrative.network.shared.context.AreaContext"/>

<g:ref var="wrapperCommon_isPageInErrorWithNoSession" className="java.lang.Boolean"/>

<%-- bl: for basic request types, we should just use a relative URL --%>
<g:set var="staticBaseUrl" object="${wrapperCommon_isPageInErrorWithNoSession or networkContext==null ? networkRegistry.staticPath : networkContext.authRealm.staticBaseUrlForCurrentScheme}" className="java.lang.String" />
<g:set var="baseUrl" object="${wrapperCommon_isPageInErrorWithNoSession ? '' : networkContext.authRealm.baseUrlForCurrentScheme}" className="java.lang.String" />

<script type="text/javascript" src="${staticBaseUrl}/jscript/labjs/LAB.src.js"></script>
<script type="text/javascript" src="${staticBaseUrl}/jscript/prototype.js"></script>

<script type="text/javascript" src="${staticBaseUrl}/jscript/jquery.js"></script>
<script type="text/javascript" src="${staticBaseUrl}/jscript/jquery.standard.overrides.js"></script>

<script type="text/javascript" src="${staticBaseUrl}/jscript/scriptaculous/scriptaculous.js"></script>

<g:set var="yahooScriptMode" object="${not empty param['debug'] ? '-debug' : ''}" />
<script type="text/javascript" src="${staticBaseUrl}/jscript/yahoo/yahoo/yahoo${yahooScriptMode}.js"></script>
<script type="text/javascript" src="${staticBaseUrl}/jscript/yahoo/dom/dom${yahooScriptMode}.js"></script>
<script type="text/javascript" src="${staticBaseUrl}/jscript/yahoo/event/event${yahooScriptMode}.js"></script>
<%--<script type="text/javascript" src="${staticBaseUrl}/jscript/yahoo/animation/animation${yahooScriptMode}.js"></script>--%>
<%--<script type="text/javascript" src="${staticBaseUrl}/jscript/yahoo/connection/connection${yahooScriptMode}.js"></script>--%>
<script type="text/javascript" src="${staticBaseUrl}/jscript/yahoo/dragdrop/dragdrop${yahooScriptMode}.js"></script>
<script type="text/javascript" src="${staticBaseUrl}/jscript/yahoo/element/element${yahooScriptMode}.js"></script>
<script type="text/javascript" src="${staticBaseUrl}/jscript/yahoo/resize/resize${yahooScriptMode}.js"></script>
<script type="text/javascript" src="${staticBaseUrl}/jscript/yahoo/button/button${yahooScriptMode}.js"></script>
<script type="text/javascript" src="${staticBaseUrl}/jscript/yahoo/container/container${yahooScriptMode}.js"></script>
<script type="text/javascript" src="${staticBaseUrl}/jscript/yahoo/menu/menu${yahooScriptMode}.js"></script>
<script type="text/javascript" src="${staticBaseUrl}/jscript/yahoo/calendar/calendar${yahooScriptMode}.js"></script>

<%-- bl: these are the source versions of the files. there are also minified/compressed versions in the following two locations:
     /jscript/yahoo/container/assets/skins/sam/container.css
     /jscript/yahoo/assets/skins/sam/container.css
     both of those files contain the equivalent minified/concatenated versions of these two files. the same pattern follows for
     all other YUI modules.
     NOTE: the following file is for backward compatibility with "no skin" mode:
     /jscript/yahoo/container/assets/container.css
     --%>
<link rel="STYLESHEET" type="text/css" href="${staticBaseUrl}/jscript/yahoo/reset/reset.css"/>
<link rel="STYLESHEET" type="text/css" href="${staticBaseUrl}/jscript/yahoo/container/assets/container-core.css"/>
<link rel="STYLESHEET" type="text/css" href="${staticBaseUrl}/jscript/yahoo/container/assets/skins/sam/container-skin.css"/>
<link rel="STYLESHEET" type="text/css" href="${staticBaseUrl}/jscript/yahoo/button/assets/button-core.css"/>
<link rel="STYLESHEET" type="text/css" href="${staticBaseUrl}/jscript/yahoo/button/assets/skins/sam/button-skin.css"/>
<link rel="STYLESHEET" type="text/css" href="${staticBaseUrl}/jscript/yahoo/calendar/assets/calendar-core.css"/>
<link rel="STYLESHEET" type="text/css" href="${staticBaseUrl}/jscript/yahoo/calendar/assets/skins/sam/calendar-skin.css"/>
<link rel="STYLESHEET" type="text/css" href="${staticBaseUrl}/jscript/yahoo/menu/assets/menu-core.css"/>
<link rel="STYLESHEET" type="text/css" href="${staticBaseUrl}/jscript/yahoo/menu/assets/skins/sam/menu-skin.css"/>

<script type="text/javascript" src="${staticBaseUrl}/jscript/common.js"></script>
<script type="text/javascript" src="${staticBaseUrl}/jscript/ajax.js"></script>
<script type="text/javascript" src="${staticBaseUrl}/jscript/standard.js"></script>

<link rel="STYLESHEET" type="text/css" href="${staticBaseUrl}/css/cluster.css"/>

${extraHeadTag}

    <%-- bl: need to define this here in a dynamically generated page so that it is localized
         properly.  can't put it in common.js because it is a static file. --%>
    <%-- bl: in order to allow the full exception.jsp page to be rendered, only output these ajax error
         messages if a network context is set.  if a network context is not set, then the wordlet
         functions would result in a jsp error. --%>
    <c:choose>
        <c:when test="${gn:isNetworkContextSet()}">
            <g:set var="ajaxFailureErrorTitle" object="${gn:wordlet('error.title')}" />
            <g:set var="ajaxUnknownErrorReferenceId" object="${networkContext.actionProcess.processOid}-${networkRegistry.version}-${networkRegistry.clusterId}.${networkRegistry.servletName}-5" />
            <g:set var="ajaxFailureErrorMessage" object="${gn:wordlet('error.ajaxSystemNotResponding')}" />
            <g:set var="showDetailsText" object="${gn:wordlet('error.showDetails')}" />
        </c:when>
        <c:otherwise>
            <%-- if no network context is set, this may be an error page.  in any event, still maintain values for these
                 in this static scenario in order to ensure that our javascript will not break. --%>
            <g:set var="ajaxFailureErrorTitle" object="Error" />
            <g:set var="ajaxUnknownErrorReferenceId" object="${networkRegistry.version}-${networkRegistry.clusterId}.${networkRegistry.servletName}-6" />
            <g:set var="ajaxFailureErrorMessage" object="The system is not responding, so your request could not be completed at this time. Please try again later." />
            <g:set var="showDetailsText" object="show details" />
        </c:otherwise>
    </c:choose>

    <c:if test="${networkContext.authZone!=null}">
        <c:choose>
            <c:when test="${gn:isNetworkContextSet()}">
                <g:set var="ajaxExceptionErrorMessage" object="${gn:wordlet1Arg('error.unknownSystemErrorRefId', ajaxUnknownErrorReferenceId)}" />
            </c:when>
            <c:otherwise>
                <g:set var="ajaxExceptionErrorMessage" object="Your request could not be completed at this time due to an unknown system error. Reference ID: ${ajaxUnknownErrorReferenceId}" />
            </c:otherwise>
        </c:choose>
    </c:if>

    <script type="text/javascript" language="JavaScript">
        Narrative.isFirefox = ${networkContext.reqResp.clientAgentInformation.clientAgentType.firefox ? true : false};
        Narrative.isMacFirefox = ${networkContext.reqResp.clientAgentInformation.macFirefox ? true : false};
        Narrative.isIE = ${networkContext.reqResp.clientAgentInformation.clientAgentType.internetExplorer ? true : false};
        Narrative.isSafari2 = ${networkContext.reqResp.clientAgentInformation.clientAgentType.safari2 ? true : false};
        Narrative.isSupportsWysiwygEditor = ${networkContext.reqResp.clientAgentInformation.supportsWysiwygEditor ? true : false};
        Narrative.isUserLoggedIn = ${networkContext.loggedInUser ? true : false};

        Narrative.isDevServer = ${networkRegistry.localServer};
        Narrative.isDevOrQaServer = ${networkRegistry.localOrDevServer};

        Narrative.googleAnalyticsIds = $A();

        Narrative.ajaxFailureErrorTitle = '${g:escapeJavascriptLiteralString(ajaxFailureErrorTitle,false)}';
        Narrative.ajaxUnknownErrorReferenceId = '${g:escapeJavascriptLiteralString(ajaxUnknownErrorReferenceId,false)}';
        Narrative.ajaxExceptionErrorMessage = '${g:escapeJavascriptLiteralString(ajaxExceptionErrorMessage,false)}';
        Narrative.ajaxFailureErrorMessage = '${g:escapeJavascriptLiteralString(ajaxFailureErrorMessage,false)}';
        Narrative.showDetailsText = '${g:escapeJavascriptLiteralString(showDetailsText,false)}';

        <c:if test="${networkRegistry.localOrDevServer}">
            YAHOO.util.Event.throwErrors = true;
        </c:if>
    </script>
