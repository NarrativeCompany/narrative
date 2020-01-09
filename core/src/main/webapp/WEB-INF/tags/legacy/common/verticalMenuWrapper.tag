<%@ tag pageEncoding="UTF-8" %>

<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core" %>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<g:ref var="action" className="org.narrative.network.shared.baseactions.NetworkAction"/>
<g:ref var="networkContext" className="org.narrative.network.shared.context.NetworkContext" />
<g:set var="areaContext" object="${gn:areaContext(pageContext)}" className="org.narrative.network.shared.context.AreaContext" />

<gct:doOnce id="menuExpandCollapseJavascript">
    <script type="text/javascript">
        expandCollapseVerticalMenu = function(event, id, currentCssClasses, isLast) {
            var isExpand = $('expandCollapseImg' + id).src.endsWith('${gn:staticImageUrlMetaData(pageContext, "/images/cluster/arrow-right-collapsed-dkgrey-18px.png").url}');
            event.cancelBubble = true;
            $('subMenuBar_' + id).className = 'subMenuBar' + (isExpand ? 'On' : '');
            $('expandCollapseLink' + id).title = isExpand ? '${gn:wordlet('tags.common.verticalMenuWrapper.clickToCollapseMenu')}' : '${gn:wordlet('tags.common.verticalMenuWrapper.clickToExpandMenu')}';
            $('expandCollapseImg' + id).src = isExpand ? '${gn:staticImageUrlMetaData(pageContext, "/images/cluster/arrow-down-expanded-dkgrey-18px.png").url}' : '${gn:staticImageUrlMetaData(pageContext, "/images/cluster/arrow-right-collapsed-dkgrey-18px.png").url}';
        };
    </script>
</gct:doOnce>

<nav class="vertical_nav">
    <gct:roundedDiv
            disablePadding="${true}"
            altCssClass="communityVerticalMenu"
            forceAllowCorners="${true}"
            corners="none">
          <jsp:doBody/>
    </gct:roundedDiv>
</nav>
