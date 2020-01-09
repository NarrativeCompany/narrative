<%@ page contentType="text/html;charset=UTF-8" session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core" %>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network" %>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gma" tagdir="/WEB-INF/tags/legacy/master/admin" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="ge" tagdir="/WEB-INF/tags/legacy/eve" %>

<g:ref var="action" className="org.narrative.network.core.cluster.actions.server.ThreadBucketStatusAction"/>

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

    <gma:serverToolbar isThreadBucketStatus="true"/>

    <g:forEach items="${action.threadBuckets}" obj="threadBucket" className="org.narrative.network.core.system.ThreadBucket" varStatus="tbStatus">
        <table class="largePadding" cellpadding="5" cellspacing="0">
          <tr><td colspan="3"><b>${threadBucket.name} ThreadManager</b></td></tr>
          <tr><td colspan="3"><b>Current Number of Threads: ${threadBucket.threadPoolExecutor.poolSize}</b></td></tr>
          <tr><td colspan="3"><b>Max Number of Threads: ${threadBucket.threadPoolExecutor.maximumPoolSize}</b></td></tr>
          <tr><td colspan="3"><b>Current Task Count: ${threadBucket.threadPoolExecutor.activeCount + fn:length(threadBucket.threadPoolExecutor.queue)}</b></td></tr>

          <tr><td>Bucket number</td><td>Current running task</td><td>Running for</td></tr>
          <g:set var="currentTaskCount" object="${0}" />
          <g:forEach items="${threadBucket.threadPoolExecutor.queue}" obj="taskThread" className="java.lang.Runnable" varStatus="threadStatus">
          <g:set var="currentTaskCount" object="${currentTaskCount+1}" />
              <tr>
                <td>
                  ${threadStatus.index}
                </td>
                <td>
                  ${taskThread}
                </td>
                <td>
                  Not Implemented<%--${taskThread.currentTaskRunTime}ms--%>
                </td>
              </tr>
          </g:forEach>
        </table>
        <c:if test="${not tbStatus.last}"><br /><br /><br /><br /></c:if>
    </g:forEach>

    <br/><br/>
    <h1>Old School Thread Buckets</h1>

    <g:forEach items="${action.oldThreadBuckets}" obj="threadBucket" className="org.narrative.common.util.ThreadBucket" varStatus="tbStatus">
        <table class="largePadding" cellpadding="5" cellspacing="0">
          <tr><td colspan="3"><b>${threadBucket.name} ThreadBucket</b></td></tr>
          <tr><td colspan="3">Incomplete tasks: ${threadBucket.incompleteTaskCount}</td></tr>
          <tr><td>Bucket number</td><td>Current running task</td><td>Debug Info</td><td>Running for</td></tr>
          <g:set var="currentTaskCount" object="${0}" />
          <g:forEach items="${threadBucket.taskThreads}" obj="taskThread" className="org.narrative.common.util.ThreadBucket.TaskThread" varStatus="threadStatus">
              <c:if test="${not empty taskThread.currentTask.name}">
                  <g:set var="currentTaskCount" object="${currentTaskCount+1}" />
                  <tr>
                    <td>
                      ${threadStatus.index}
                    </td>
                    <td>
                      ${taskThread.currentTask.name}
                    </td>
                    <td>
                      ${taskThread.currentTask.debugInfo}
                    </td>
                    <td>
                      ${taskThread.currentTaskRunTime}ms
                    </td>
                  </tr>
              </c:if>
          </g:forEach>
          <c:if test="${currentTaskCount==0}">
              <tr><td colspan="3">No currently running tasks</td></tr>
          </c:if>
        </table>
        <c:if test="${not tbStatus.last}"><br /><br /><br /><br /></c:if>
    </g:forEach>

</ge:clusterWrapper>