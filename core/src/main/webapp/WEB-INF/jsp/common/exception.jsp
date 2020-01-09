<%@ page import="org.narrative.common.util.posting.HtmlTextMassager"%>
<%@ page import="org.narrative.network.core.system.NetworkRegistry" %>
<%@ page contentType="text/html;charset=UTF-8" session="false" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="sss" uri="http://www.narrative.org/tags/struts"%>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="page" tagdir="/WEB-INF/tags/common/page" %>
<%@ taglib prefix="s_page" tagdir="/WEB-INF/tags/site/page" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/components" %>

<ss:ref var="networkRegistry" className="org.narrative.network.core.system.NetworkRegistry" />
<ss:ref var="processOid" className="org.narrative.common.persistence.OID" />

<ss:set var="exceptionRef" object="<%=exception%>" className="java.lang.Throwable" />

<%-- bl: since this request has bubbled up to tomcat, we can't use any wordlets
     or any other features that may require NetworkContext or database access. everything
     has to be static. using the "Danger Will Robinson" page just like we do for 50x
     errors in HAProxy. --%>

<ss:set var="extraMessage">
    <%
        if(NetworkRegistry.getInstance().isLocalOrDevServer()) {
    %>
        <br/><br/>
        <div>
            <b>The following stack trace is being displayed since this environment is configured as a QA environment.
            This message will never appear in a production environment (since they will not be configured as QA environments).</b>
        </div>
        <pre class="stack-trace">
            <%=HtmlTextMassager.stackTraceAsHtml(exception)%>
        </pre>
    <%
        }
    %>
</ss:set>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
"http://www.w3.org/TR/html4/strict.dtd">
<html lang="en">
    <head>
        <title>Error!</title>
        <link href='//fonts.googleapis.com/css?family=Oswald|Alfa+Slab+One:400,700' rel='stylesheet' type='text/css'>
        <style type="text/css">
            body {
                margin: 0;
                background-color: #e3e8ca;
                background-repeat: no-repeat;
                background-attachment: fixed;
                background-position: center top;
            }

            #error-headline {
                font-family: "Alfa Slab One";
                font-size: 56px;
                padding-bottom: 0px;
                padding-left: 20px;
            }

            #error-subhead {
                font-family: Oswald;
                font-size: 30px;
                padding-bottom: 10px;
                padding-left: 20px;
            }

            #error-box {
                background-color: rgb(51, 51, 51);
                background-color: rgba(51, 51, 51, 0.75);
                font-family: Oswald;
                font-size: 22px;
                color: #FFFFFF;
            }

            #error-description {
                font-family: Oswald;
                font-size: 16px;
                color: #FFFFFF;
                padding-left: 20px;
                padding-top: 7px;
                padding-bottom: 7px;
                padding-right: 20px;
            }

            a {
                color: #FFFFF0;
            }

        </style>

    </head>
    <body>
        <div id="error-headline">
            Danger, Will Robinson!
        </div>
        <div id="error-subhead">
            Something has gone horribly wrong.
        </div>
        <div id="error-box">
            <div id="error-description">
                <div>
                    The page you requested is not available.  Reference ID:
                    ${processOid}[${ss:exceptionHashCode(exceptionRef)}][${ss:exceptionRootCauseHashCode(exceptionRef)}]${networkRegistry.version}-${networkRegistry.clusterId}.${networkRegistry.servletName}-1
                </div>

                <div>
                    There's a chance that this is a temporary error. Please try
                    <a href="javascript:document.location.reload()">re-loading this page</a>.
                </div>

                ${extraMessage}
            </div>
        </div>
    </body>
</html>
