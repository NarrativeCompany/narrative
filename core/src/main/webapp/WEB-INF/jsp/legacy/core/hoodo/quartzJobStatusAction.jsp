<%@ page contentType="text/html;charset=UTF-8" session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core" %>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network" %>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gma" tagdir="/WEB-INF/tags/legacy/master/admin" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="ge" tagdir="/WEB-INF/tags/legacy/eve" %>

<g:ref var="action" className="org.narrative.network.core.cluster.actions.server.QuartzJobStatusAction"/>

<gct:wrapperCommonParams title="Thread Bucket Status" />

<ge:clusterWrapper useServletSpecificBaseUrl="${true}">
    <style type="text/css">
        table.smallPadding td {
            padding:1px;
        }

        table.largePadding td {
            padding:5px;
        }
    </style>

    <gma:serverToolbar isQuartzJobs="true"/>

    <table class="largePadding" cellpadding="5" cellspacing="0" style="width:100%;">
      <tr><td colspan="4"><b>Local Jobs</b></td></tr>
      <tr><td>Task</td><td>Status</td><td>Started</td><td>Next Run</td><td>Last Run</td></tr>
      <g:forEach items="${action.localQuartzJobsQueue}" obj="job" className="org.narrative.network.core.cluster.actions.server.QuartzJobStatusAction.QuartzJobInfo" varStatus="_status">
          <tr>
              <td>
                  <g:set var="triggerNameStr">
                      <span title="Job: ${job.jobName}">${job.triggerName} (${job.triggerGroupName})</span>
                  </g:set>
                  <c:choose>
                      <c:when test="${job.running}">
                          <b>${triggerNameStr}</b>
                      </c:when>
                      <c:otherwise>
                          ${triggerNameStr}
                      </c:otherwise>
                  </c:choose>
              </td>
              <td>${job.jobStatus}</td>
              <td>${gn:dateFormatShortDatetime(job.fireTime)}</td>
              <td>${gn:dateFormatShortDatetime(job.nextFireTime)}</td>
              <td>${gn:dateFormatShortDatetime(job.previousFireTime)}</td>
          </tr>
      </g:forEach>
    </table>
    <br/>
    <br/>
    <br/>
    <table class="largePadding" cellpadding="5" cellspacing="0" style="width:100%;">
      <tr><td colspan="4"><b>Global Jobs</b></td></tr>
      <tr><td>Task</td><td>Status</td><td>Started</td><td>Next Run</td><td>Last Run</td></tr>
      <g:forEach items="${action.globalQuartzJobsQueue}" obj="job" className="org.narrative.network.core.cluster.actions.server.QuartzJobStatusAction.QuartzJobInfo" varStatus="_status">
          <tr>
              <td>
                  <g:set var="triggerNameStr">
                      <span title="Job: ${job.jobName}">${job.triggerName} (${job.triggerGroupName})</span>
                  </g:set>
                  <c:choose>
                      <c:when test="${job.running}">
                          <b>${triggerNameStr}</b>
                      </c:when>
                      <c:otherwise>
                          ${triggerNameStr}
                      </c:otherwise>
                  </c:choose>
              </td>
              <td>${job.jobStatus}</td>
              <td>${gn:dateFormatShortDatetime(job.fireTime)}</td>
              <td>${gn:dateFormatShortDatetime(job.nextFireTime)}</td>
              <td>${gn:dateFormatShortDatetime(job.previousFireTime)}</td>
          </tr>
      </g:forEach>
    </table>

</ge:clusterWrapper>