<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>
<%@ attribute name="id" required="true" description="id for referencing element. For UI and form tags it will be used as HTML id attribute" %>
<%@ attribute name="name" required="true" description="The name to set for element" %>
<%@ attribute name="required" required="true" type="java.lang.Boolean"%>
<%@ attribute name="label" required="true" %>
<%@ attribute name="isUsingTopLabel" type="java.lang.Boolean" description="sets the labelPosition to top and the proper css class to the form element" %>
<%@ attribute name="cssClass" %>
<%@ attribute name="cssStyle" %>
<%@ attribute name="labelCssClass" %>
<%@ attribute name="labelCssStyle" %>
<%@ attribute name="preControlHtml" %>
<%@ attribute name="postControlHtml" %>



<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network"%>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>

<g:ref var="networkAction" className="org.narrative.network.shared.baseactions.NetworkAction" />
<%-- bl: struts already inserts a property called fieldErrors into the pageContext.  so, can't
     use fieldErrors as the variable name here. --%>
<g:set var="controlFieldErrors" object="${networkAction.formType==formId ? networkAction.fieldErrors[name] : null}" className="java.util.Collection" />

<div id="wwgrp_${id}" class="wwgrp ${cssClass}" ${g:condAttr('style',cssStyle)}>

    <c:if test="${not empty label}">
        <g:set var="labelDivId" object="wwlbl_${id}" />
        <g:set var="labelDivClass" object="wwlbl${isUsingTopLabel?'_top': ''}" />
        <g:set var="labelHtml">
            <gfs:label labelFor="${id}" label="${label}" fieldName="${name}" isRequired="${required}" cssStyle="${labelCssStyle}" cssClass="${labelCssClass}" />
        </g:set> 
        <c:choose>
            <c:when test="${isUsingTopLabel}">
                <div ${g:attr('id',labelDivId)} class="${labelDivClass}">
                    ${labelHtml}
                </div>
                <%-- bl: brutal hack. this div clear design is terrible. we need to get rid of it at some point.
                     having it here was causing issues with CSS styling, so I'm just removing it
                     for now based on the RequestType. gross, but it will work, and we should get rid of this poor
                     design in the future anyway. --%>
                <div class="${networkAction.networkContext.reqResp.clientAgentInformation.clientAgentType.internetExplorer?'clear20':'clear5'}" style="">&nbsp;</div>
            </c:when>
            <c:otherwise>
                <span ${g:attr('id',labelDivId)} class="${labelDivClass}">
                    ${labelHtml}
                </span>
            </c:otherwise>
        </c:choose>
    </c:if>

    <g:set var="classSuffix" object="${not empty controlFieldErrors ? '_error' : ''}" />
    <c:choose>
        <c:when test="${isUsingTopLabel}">
            <div ${g:attr('id',g:concat('wwctrl_',id))} class="wwctrl${classSuffix}">
                ${preControlHtml}<jsp:doBody />${postControlHtml}
            </div>
        </c:when>
        <c:otherwise>
            <span ${g:attr('id',g:concat('wwctrl_',id))} class="wwctrl${classSuffix}">
                ${preControlHtml}<jsp:doBody />${postControlHtml}
            </span>
        </c:otherwise>
    </c:choose>
</div>
<div class="clear">&nbsp;</div>