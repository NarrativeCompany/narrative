<%@ tag pageEncoding="UTF-8" %>

<%@ attribute name="rootTraceItem" type="org.narrative.common.util.trace.TraceItem" required="true" %>


<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core" %>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<g:set var="maxDepth" object="${rootTraceItem.maxDepth}"/>
<g:forEach items="${rootTraceItem.hierarchyAsList}" obj="traceItem" className="org.narrative.common.util.trace.TraceItem">
    <g:set var="depth" object="${traceItem.curDepth}"/>
    <g:set var="seq" object="${g:seq()}"/>
    <g:set var="query" object="${traceItem}" className="org.narrative.common.persistence.hibernate.GQueryStats"  setNullIfNotOfType="true"/>
    <g:set var="proc" object="${traceItem}" className="org.narrative.common.util.processes.TraceProcessHistory"  setNullIfNotOfType="true"/>
    <tr>
        <g:forLoop begin="1" end="${depth}" varStatus="stat">
            <td bgcolor="grey">&nbsp</td>
        </g:forLoop>
        <c:if test="${maxDepth - depth > 0}">
            <td colspan="${maxDepth - depth}">&nbsp;&nbsp;</td>
        </c:if>
        <td>
            <c:choose>
                <c:when test="${query != null}">
                    <a href="#" onclick="toggleView('selectDiv${seq}')">select...</a><div id="selectDiv${seq}" style="display:none;">${query.select}</div>
                        ${query.rest}
                    <c:if test="${not empty query.params}">
                        <br/>
                        <a href="#" onclick="toggleView('paramsDiv${seq}')">params...</a>
                        <div id="paramsDiv${seq}" style="display:none;">
                            <g:forEach items="${query.params}" obj="obj" className="java.lang.Object">
                                <li>${obj}</li>
                            </g:forEach>
                        </div>
                    </c:if>
                </c:when>
                <c:when test="${proc != null}">
                    ${proc.procName}
                </c:when>
            </c:choose>
        </td>
        <td align="right">${traceItem.durationMSFormatted}</td>
        <td align="right">${traceItem.totalDurationRatioFormatted}</td>
        <td align="right">${traceItem.parentDurationRatioFormatted}</td>
    </tr>


</g:forEach>



