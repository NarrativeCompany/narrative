
<%--
  Created by IntelliJ IDEA.
  User: barry
  Date: Dec 13, 2005
  Time: 11:53:41 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network"%>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gma" tagdir="/WEB-INF/tags/legacy/master/admin" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="ge" tagdir="/WEB-INF/tags/legacy/eve" %>

<g:ref var="action" className="org.narrative.network.core.cluster.actions.server.ProcessStatusAction" />

<gct:wrapperCommonParams title="Current Processes">
    <jsp:attribute name="extraHeadTag">
        <style type="text/css">
            <%-- bl: adding this so that we get scrollbars in the recent outliers popup windows --%>
            .yui-panel .bd {
                overflow: auto !important;
            }
        </style>
    </jsp:attribute>
</gct:wrapperCommonParams>
<ge:clusterWrapper useServletSpecificBaseUrl="${true}">
    <gma:serverToolbar isProcesses="true"/>
   <br/>
    <h3>Server Status</h3>
    <table class="data_table" border="1" cellspacing="0" width="100%">
        <tr>
            <td>
                <table>
                    <tr>
                        <td width="200"><b>WebApp:</b></td>
                        <td>${action.networkRegistry.webapp}</td>
                    </tr>
                </table>

                <gma:serverStatusDetailsTable serverStatus="${action.serverStatus}" />
            </td>
            <td>
                <gma:serverStatusStatsTable serverStatus="${action.serverStatus}" />
            </td>
        </tr>
    </table>
    <br/>
    <h3>Current Processes</h3>
    <table id="processTable">
        <tr>
            <th>[ID]</th>
            <th>[Time(ms)]</th>
            <th>[Type]</th>
            <th>[Owner]</th>
            <th>[Area]</th>
            <th>[Name]</th>
            <th>[Process Info]</th>
            <th>[Status]</th>
        </tr>
        <g:forEach items="${action.processList}" obj="process" className="org.narrative.common.util.processes.GenericProcess">
            <g:forEach items="${process.processList}" obj="subProcess" className="org.narrative.common.util.processes.GenericProcess" varStatus="processStatus">
                <tr${not processStatus.first ? ' style="display:none;"' : ' style="font-weight:bold;"'} class="process${process.processOid}">
                    <td>
                        <c:choose>
                            <c:when test="${processStatus.first and not processStatus.last}">
                                [<a href="javascript:viewProcessStack('${subProcess.processOid}');">${subProcess.processOid}</a>]
                            </c:when>
                            <c:otherwise>
                                [${subProcess.processOid}]
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>[${subProcess.totalRunningTime}]</td>
                    <td>${subProcess.type}</td>
                    <td>${subProcess.owner}</td>
                    <td>${subProcess.areaName}</td>
                    <td>${subProcess.name}</td>
                    <td>
                        <%-- generating the info string may have a bit of overhead (StringBuilders, etc.), so
                             once we fetch the value once, keep a reference to the string. --%>
                        <g:set var="processInfo" object="${g:disableHtml(subProcess.info)}" />
                        ${processInfo}
                        <c:if test="${not empty subProcess.statusMessage}">
                            <c:if test="${not empty processInfo}"><br /><br /></c:if>
                            Status: ${subProcess.statusMessage}
                        </c:if>
                    </td>
                    <td>${subProcess.status}</td>
                </tr>
            </g:forEach>
        </g:forEach>
    </table>
    
    <script type="text/javascript" language="JavaScript">
        function viewProcessStack(processOid) {
            var processElements = Narrative.getElementsByClassName('process' + processOid, 'processTable');
            for(var i=0; i<processElements.length; i++) {
                Element.Methods.show(processElements[i]);
            }
        }
    </script>
    
    <br/>

    <gma:processHistory allHistory="${action.allHistory}" />

</ge:clusterWrapper>



