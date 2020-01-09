<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network"%>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>

<g:ref var="networkContext" className="org.narrative.network.shared.context.NetworkContext" />
<g:ref var="networkRegistry" className="org.narrative.network.core.system.NetworkRegistry" />

<g:ref var="wrapperCommon_isPageInErrorWithNoSession" className="java.lang.Boolean" />

<gct:doOnce id="pleaseWaitDivPopupWindow">
    <g:set var="pleaseWaitMessage">
        <div style="text-align:center;padding:10px">
            <gn:staticImage src="/images/cluster/loading.gif" />
            <div style="padding-top:15px">${gn:wordlet('tags.common.pleaseWaitPopup.pleaseWait')}</div>
        </div>
    </g:set>
    <gct:divPopup id="pleaseWait" title="${gn:wordlet('tags.common.pleaseWaitPopup.pleaseWait')}" width="200">
        ${pleaseWaitMessage}
    </gct:divPopup>
</gct:doOnce>