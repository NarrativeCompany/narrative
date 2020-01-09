<%@ tag pageEncoding="UTF-8" body-content="empty" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="value" required="true" type="org.narrative.common.web.DateTimeInput" %>
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
<%@ attribute name="onchange" %>
<%@ attribute name="onDateSelectFunc" %>

<%@ attribute name="allowQuarterIncrements" description="Allow 15 and 45 minute values in addition to 0 and 30" type="java.lang.Boolean" %>

<%@ attribute name="isUsingTopLabel" type="java.lang.Boolean" description="sets the labelPosition to top and the proper css class to the form element" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="gtu" uri="http://legacy.narrative.org/tags/tagUtils" %>

<g:set var="dateandtimepickerId" object="${gtu:createInputId(id,formId,name)}"/>
<g:set var="cssClass"><c:if test="${isUsingTopLabel or empty label}">noLabel </c:if>${cssClass}</g:set>

<gfs:controlwrapper
        name="${name}"
        id="${dateandtimepickerId}"
        required="${required}"
        label="${label}"
        cssStyle="${cssStyle}"
        isUsingTopLabel="${isUsingTopLabel}">

    <gfs:datepickerSimple
            name="${name}.dateInput"
            value="${value.dateInput}"
            size="${size}"
            disabled="${disabled}"
            readonly="${readonly}"
            tabindex="${tabindex}"
            id="${g:concat(dateandtimepickerId, '_date')}"
            cssClass="${cssClass}"
            title="${title}"
            onchange="${onchange}"
            onDateSelectFunc="${onDateSelectFunc}" />
    
    <%-- bl: since the timepicker is going to display right next to the datepicker, using a cssClass
         of noLabel to prevent extra padding between the two. --%>
    <gfs:timepickerSimple
            name="${name}.timeInput"
            value="${value.timeInput}"
            disabled="${disabled}"
            tabindex="${empty tabindex ? null : tabindex+1}"
            id="${g:concat(dateandtimepickerId, '_time')}"
            cssClass="noLabel"
            allowQuarterIncrements="${allowQuarterIncrements}"
            onchange="${onchange}" />

</gfs:controlwrapper>