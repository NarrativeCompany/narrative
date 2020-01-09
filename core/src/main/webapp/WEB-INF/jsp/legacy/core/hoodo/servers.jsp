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

<g:ref var="action" className="org.narrative.network.core.cluster.actions.ServersAction" />

<gct:wrapperCommonParams title="Servers"/>
<ge:clusterWrapper>
    <div style="text-align:right;margin:-25px 0 10px 0;" class="medfont">
    <gfs:form action="${action.networkRegistry.clusterCpRelativePath}/servers" method="get">
        Refresh every <input name="refreshInterval" value="${action.refreshInterval}" size="5"/> seconds.
        <gfs:submit value="Update" />
    </gfs:form>
    </div>
    <table class="data_table" border="1" cellspacing="0" width="100%">
        <tr>
            <th width="100">Server Info</th>
            <th>Server Status</th>
        </tr>
        
        <g:forEach items="${action.networkRegistry.serverNames}" obj="serverName" className="java.lang.String">
            <tr>
                <td valign="top" width="100">
                    <b>${serverName}</b><br/>
                </td>

                <td>
                    <div id="stat_${serverName}"></div>
                </td>
            </tr>
        </g:forEach>

    </table>



    <script type="text/javascript">
        setTimeout('init()',0);
        function init() {
            <g:forEach items="${action.networkRegistry.serverNames}" obj="serverName" className="java.lang.String">
                serverStatus($('stat_${serverName}'), '${serverName}');
            </g:forEach>
            setTimeout('init()',${action.refreshInterval*1000});
        }

        function serverStatus(divToRepace, servletName) {
            var opt2 = {
                method: 'get'
            };
            var args = {
                replaceDiv : divToRepace,
                insertErrorIntoReplaceDiv : true
            };
            ajaxSubmit('${action.networkRegistry.clusterCpRelativePath}/serverStatus/servletName/' + servletName, opt2, args);
        }


    </script>



</ge:clusterWrapper>




