<%--
  Created by IntelliJ IDEA.
  User: Paul
  Date: Feb 6, 2006
  Time: 10:24:23 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" session="false" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="gmt" tagdir="/WEB-INF/tags/legacy/master" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network"%>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="gma" tagdir="/WEB-INF/tags/legacy/master/admin" %>
<%@ taglib prefix="ge" tagdir="/WEB-INF/tags/legacy/eve" %>

<g:ref var="action" className="org.narrative.network.core.cluster.actions.CreateHQLQueryResultsAction" />
<g:ref var="networkContext" className="org.narrative.network.shared.context.NetworkContext" />

<gct:wrapperCommonParams title="HQL Query"/>
<ge:clusterWrapper>
    <div class="medfont">
    <gfs:form action="${action.networkRegistry.clusterCpRelativePath}/hqlQuery!query" method="post">
        Partition:
        <select name="partition">
            <g:forEach items="${action.partitions}" obj="entry" className="java.util.Map.Entry">
                <option value="${entry.key}" ${g:isEqual(entry.key,action.partition)?"selected":""}>${entry.value}</option>
            </g:forEach>
        </select>
        <gfs:textarea isUsingTopLabel="${true}" value="${action.hql}" name="hql" cols="80" rows="5" label="Query" isRawHtml="true" />
        <gfs:select name="objectClass" value="${action.objectClass.name}" list="${action.objectTypes}" label="Object Class" />
        <gfs:text name="objectOid" value="${action.objectOid}" label="Object OID" />
        <gfs:text value="${action.rowsPerPage}" name="rowsPerPage" label="Rows Per Page" size="4" />
        <gfs:checkbox name="explain" value="${action.explain}" label="Explain"/>
        <gfs:submit value="Send Query"/>
    </gfs:form>
    <br/><br/>
    <c:choose>
        <c:when test="${action.explain and not empty action.explainResults}">
            <g:forEach items="${action.explainResults}" obj="entry" className="java.util.Map.Entry">
                <g:set var="partition" object="${entry.key}" className="org.narrative.network.core.cluster.partition.Partition" />
                <g:set var="explainResults" object="${entry.value}" className="java.util.List" />
                <h2>${partition.displayName}</h2>
                <g:forEach items="${explainResults}" obj="exp" className="org.narrative.network.core.cluster.actions.CreateHQLQueryResultsAction.ExplainResults">
                    <b>${exp.sql}</b><br/>
                    <table border="1">
                        <g:forEach items="${action.colNames}" obj="colName" className="java.lang.String">
                            <th>${colName}</th>
                        </g:forEach>
                        <g:forEach items="${exp.explain}" obj="expRow" className="java.util.List">
                            <tr>
                                <g:forEach items="${expRow}" obj="expCol" className="java.lang.Object">
                                    <td>${expCol}</td>
                                </g:forEach>
                            </tr>
                        </g:forEach>
                    </table>
                </g:forEach>
                <br /><br />
            </g:forEach>
        </c:when>
        <c:otherwise>
            ${action.html}
        </c:otherwise>
    </c:choose>
    </div>
</ge:clusterWrapper>