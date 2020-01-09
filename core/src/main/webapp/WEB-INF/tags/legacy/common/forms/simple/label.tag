<%@ tag pageEncoding="UTF-8" body-content="empty"%>

<%@attribute name="labelFor" required="true" description="the ID of the element that this label is for" %>
<%@attribute name="label" required="true" description="the label to use for this element" %>
<%@attribute name="fieldName" required="true" description="the name of the field that this label is for.  this is used for determining if there are any field errors for the field, which determines the style of the label." %>
<%@attribute name="isRequired" type="java.lang.Boolean" description="defaults to true.  true if this is a required field.  false if it is not.  this will determine the styling of the label." %>
<%@attribute name="excludeCssClass" type="java.lang.Boolean" description="defaults to false.  true if you'd like to exclude the default css class" %>
<%@attribute name="cssClass" type="java.lang.String" description="any additional css styling to include" %>
<%@attribute name="cssStyle" type="java.lang.String" description="any additional css styling to include" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="gta" tagdir="/WEB-INF/tags/legacy/area" %>

<g:ref var="action" className="org.narrative.network.shared.baseactions.NetworkAction" />

<g:set var="fieldErrors" object="${action.formType==formId ? action.fieldErrors[fieldName] : null}" className="java.util.Collection" />

<g:set var="cssClass">
    ${not empty fieldErrors ? 'errorLabel' : (isRequired==null || isRequired ? 'requiredLabel' : 'label')}${' '}${cssClass}
</g:set>

<label for="${labelFor}" id="${labelFor}_label"${g:condAttr('style', cssStyle)} ${excludeCssClass ? '' : g:attr('class', cssClass)}>
    ${label}

    <c:if test="${isRequired}">
        <gfs:requiredLabelMarker />
    </c:if>
</label>