<%@ tag pageEncoding="UTF-8" %>

<%@ attribute name="bodyStyle" %>
<%@ attribute name="bodyClass" %>
<%@ attribute name="htmlClass" %>
<%@ attribute name="bodyOnclick" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network"%>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>

<%@ taglib prefix="page" tagdir="/WEB-INF/tags/common/page" %>

<g:ref var="networkRegistry" className="org.narrative.network.core.system.NetworkRegistry"/>

<g:ref var="networkContext" className="org.narrative.network.shared.context.NetworkContext" />
<g:ref var="processOid" className="org.narrative.common.persistence.OID" />

<g:set var="areaContext" object="${gn:areaContext(pageContext)}" className="org.narrative.network.shared.context.AreaContext" />

<g:ref var="wrapperCommon_extraHeadTag" className="java.lang.String" />
<g:ref var="wrapperCommon_title" className="java.lang.String" />



<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"${g:condAttr('class', htmlClass)}>
<page:serverVersionInfoComment />

<%-- bl: only include the stress tester fields on QA servers to prevent these from being displayed on production environments --%>
<c:if test="${networkRegistry.localOrDevServer}">
<g:ref var="action" className="org.narrative.network.shared.baseactions.NetworkAction" />
<!--
PROCESSOID: ${processOid}
PRIMARYROLEOID: ${networkContext.hasPrimaryRole ? networkContext.primaryRole.oid : null}
<c:if test="${areaContext!=null}">
SITEOID: ${areaContext.area.oid}</c:if>
<c:if test="${networkContext.loggedInUser}">
USEROID: ${networkContext.user.oid}
</c:if>
-->
</c:if>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <title>${wrapperCommon_title}</title>
    <gct:head extraHeadTag="${wrapperCommon_extraHeadTag}" />
</head>

<body style="${bodyStyle}" class="${bodyClass}" ${g:condAttr('onclick',bodyOnclick)}>

<c:if test="${networkRegistry.localServer and not empty param['jscriptPrompt']}">
    <a href="javascript:void(0);" onclick="var stuff=prompt('jscript','');if(stuff)eval(stuff);">Do stuff</a>
</c:if>
<c:if test="${networkRegistry.localServer and not empty param['debug']}">
    <div id="logoutput" style="width:100%;height:200px;overflow:scroll;text-align:left;color:black;background-color:white;display:none;"></div>
    <a href="javascript:void(0);" onclick="$('logoutput').innerHTML='';">Clear</a>
    <script type="text/javascript">
        if(typeof console=='undefined') {
            Narrative.console = {};
            Narrative.console.log = function(log) {
                var outputElt = $('logoutput');
                outputElt.innerHTML += new Date().getTime() + log + '<br />';
                outputElt.scrollTop = outputElt.scrollHeight;
            };
            Element.show('logoutput');
        } else {
            Narrative.console = console;
        }
        <%--function doStuffWithObj(d,x) {
            var e = null;
            try {
                e = d[x];
            } catch(ex) {
                e = 'errorWithObj';
            }
            if(typeof e == 'function') {
                Narrative.console.log('func: ' + x + ': ' + e);
            } else {
                Narrative.console.log(x + ': ' + e);
            }
        }
        addOnloadHandler(function() {
            Narrative.console.log('document properties:');
            for(var x in document) {
                doStuffWithObj(document, x);
            }
            Narrative.console.log('============================');
            Narrative.console.log('window properties:');
            for(var x in window) {
                doStuffWithObj(window, x);
            }
        });--%>
    </script>
</c:if>

    <gct:endOfBodyContentContainer>
        <jsp:doBody />

        <gct:pleaseWaitPopup />
    </gct:endOfBodyContentContainer>
</body>
</html>
