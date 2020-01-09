<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ attribute name="useServletSpecificBaseUrl" type="java.lang.Boolean" %>

<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core" %>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gta" tagdir="/WEB-INF/tags/legacy/area" %>
<%@ taglib prefix="gtaa" tagdir="/WEB-INF/tags/legacy/area/admin" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="ge" tagdir="/WEB-INF/tags/legacy/eve" %>
<%@ taglib prefix="gmt" tagdir="/WEB-INF/tags/legacy/master" %>
<%-- bl: since the wrapper is used to generate error pages and error pages may need to be rendered
     for non-AreaActions, don't cast the action as an AreaAction. --%>
<g:ref var="action" className="org.narrative.network.shared.baseactions.NetworkAction" />

<g:ref var="networkRegistry" className="org.narrative.network.core.system.NetworkRegistry"/>
<g:ref var="networkContext" className="org.narrative.network.shared.context.NetworkContext"/>
<g:ref var="networkAction" className="org.narrative.network.shared.baseactions.NetworkAction"/>
<g:set var="areaContext" object="${gn:areaContext(pageContext)}" className="org.narrative.network.shared.context.AreaContext" />

<g:set var="clusterName" object="${networkRegistry.clusterId}" className="java.lang.String" />

<!-- bl: make sure for all cluster requests we include the cluster domain and customer name or cluster ID in the page title! -->
<g:ref var="wrapperCommon_title" className="java.lang.String" />
<g:set var="wrapperCommon_title" object="${wrapperCommon_title} | ${networkRegistry.clusterCpDomain} (${clusterName})" scope="request" />

<g:ref var="wrapperCommon_customAreaHeaderColumn" className="java.lang.String"/>
<g:ref var="wrapperCommon_bodyTitle" className="java.lang.String"/>

<gct:basicWrapper bodyStyle="margin:0px;padding:0px;" bodyOnclick="doHideMenus();">

    <div id="narrativeBody" class="primaryWrapper">
        <c:if test="${not networkContext.requestType.kycQueue}">
            <header id="page_header">
                <h1>
                    ${clusterName}
                    (${networkRegistry.clusterId})
                </h1>
            </header>
        </c:if>

        <g:set var="bodyHtml">
            <gct:wrapperCommonHeader
                    title="${wrapperCommon_bodyTitle}"
                    customHeaderColumn="${wrapperCommon_customAreaHeaderColumn}"
            />

            <jsp:doBody/>
        </g:set>

        <c:choose>
            <c:when test="${networkContext.requestType.kycQueue}">
                <div class="primaryContentBox">
                    ${bodyHtml}
                </div>
            </c:when>
            <c:otherwise>
                <table cellspacing="0" class="generic" id="areaWrapperContent">
                    <tr>

                        <td id="areaWrapperContentNav">
                            <gct:verticalMenuWrapper>
                                <gtaa:controlPanelVerticalNav/>
                            </gct:verticalMenuWrapper>
                        </td>

                        <%--This is the primary content body column--%>
                        <td class="primaryContentBox">
                            ${bodyHtml}
                            <div class="clear">&nbsp;</div>
                        </td>
                    </tr>
                </table>
            </c:otherwise>
        </c:choose>


        <c:if test="${action.tracing}">
            <gct:divPopup id="trace" title="Trace" width="900" height="500" isResizable="true" isModal="false">

                <g:set var="rootTrace" object="${action.rootTraceItemWithEndAll}" className="org.narrative.common.util.trace.TraceItem"/>
                <table border="1">
                    <th colspan="${rootTrace.maxDepth}">Depth</th>
                    <th>Detail</th>
                    <th>Duration (ms)</th>
                    <th>% Total</th>
                    <th>% Parent</th>
                    <gct:trace rootTraceItem="${rootTrace}"/>
                </table>
            </gct:divPopup>

            <script type="text/javascript">
                tracePopupWindow.show();
            </script>
        </c:if>
    </div>

    <gct:wrapperWidgets/>
</gct:basicWrapper>
