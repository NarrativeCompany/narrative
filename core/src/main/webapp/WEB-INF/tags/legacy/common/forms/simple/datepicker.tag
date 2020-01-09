<%@ tag pageEncoding="UTF-8" body-content="empty" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="value" required="true" type="org.narrative.common.web.DateInput" %>
<%@ attribute name="size" type="java.lang.Integer" %>
<%@ attribute name="disabled" type="java.lang.Boolean" %>
<%@ attribute name="readonly" type="java.lang.Boolean" %>
<%@ attribute name="tabindex" type="java.lang.Integer" %>
<%@ attribute name="id" %>
<%@ attribute name="required" type="java.lang.Boolean" %>
<%@ attribute name="label" %>
<%@ attribute name="cssClass" %>
<%@ attribute name="cssStyle" %>
<%@ attribute name="title" %>
<%@ attribute name="onclick" description="Set the html onclick attribute on rendered html element" %>
<%@ attribute name="ondblclick" description="Set the html ondblclick attribute on rendered html element" %>
<%@ attribute name="onmousedown" description="Set the html onmousedown attribute on rendered html element" %>
<%@ attribute name="onmouseup" description="Set the html onmouseup attribute on rendered html element" %>
<%@ attribute name="onmouseover" description="Set the html onmouseover attribute on rendered html element" %>
<%@ attribute name="onmousemove" description="Set the html onmousemove attribute on rendered html element" %>
<%@ attribute name="onmouseout" description="Set the html onmouseout attribute on rendered html element" %>
<%@ attribute name="onfocus" description="Set the html onfocus attribute on rendered html element" %>
<%@ attribute name="onblur" description="Set the html onblur attribute on rendered html element" %>
<%@ attribute name="onkeypress" description="Set the html onkeypress attribute on rendered html element" %>
<%@ attribute name="onkeydown" description="Set the html onkeydown attribute on rendered html element" %>
<%@ attribute name="onkeyup" description="Set the html onkeyup attribute on rendered html element" %>
<%@ attribute name="onselect" description="Set the html onselect attribute on rendered html element" %>
<%@ attribute name="onchange" description="Set the html onchange attribute on rendered html element" %>
<%@ attribute name="onDateSelectFunc" description="A function to call when a new date is selected via the date picker" %>

<%@ attribute name="format" description="The format string to use for the date picker" %>
<%@ attribute name="showstime" type="java.lang.Boolean" description="true if time should be selectable for this datepicker.  false if it should not (default)." %>

<%@ attribute name="isUsingTopLabel" type="java.lang.Boolean" description="sets the labelPosition to top and the proper css class to the form element" %>
<%@ attribute name="description" type="java.lang.String" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="gtu" uri="http://legacy.narrative.org/tags/tagUtils" %>

<g:set var="datepickerId" object="${gtu:createInputId(id,formId,name)}"/>
<g:ref var="action" className="org.narrative.network.shared.baseactions.NetworkAction" />

<g:set var="cssClass"><c:if test="${isUsingTopLabel or empty label}">noLabel </c:if>${cssClass}</g:set>

<gfs:controlwrapper
        name="${name}"
        id="${datepickerId}"
        required="${required}"
        label="${label}"
        isUsingTopLabel="${isUsingTopLabel}">

    <c:if test="${not empty description and isUsingTopLabel}">
        <div class="textInputDescription">
            ${description}
        </div>
    </c:if>

    <gfs:datepickerSimple
            name="${name}"
            value="${value}"
            size="${size}"
            disabled="${disabled}"
            readonly="${readonly}"
            tabindex="${tabindex}"
            id="${datepickerId}"
            cssClass="${cssClass}"
            cssStyle="${cssStyle}"
            title="${title}"
            onclick="${onclick}"
            ondblclick="${ondblclick}"
            onmousedown="${onmousedown}"
            onmouseup="${onmouseup}"
            onmouseover="${onmouseover}"
            onmousemove="${onmousemove}"
            onmouseout="${onmouseout}"
            onfocus="${onfocus}"
            onblur="${onblur}"
            onkeypress="${onkeypress}"
            onkeydown="${onkeydown}"
            onkeyup="${onkeyup}"
            onselect="${onselect}"
            onchange="${onchange}"
            onDateSelectFunc="${onDateSelectFunc}"
    />

    <c:if test="${not empty description and not isUsingTopLabel}">
        <div class="textInputDescriptionUnderInput">
            ${description}
        </div>
    </c:if>

</gfs:controlwrapper>