<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="value" required="true" type="java.lang.Object" description="All of the currently checked checkboxes.  Must be a Collection or an array." %>
<%@ attribute name="valueOfListKeyType" type="java.lang.Boolean" description="True if the value specified is a collection of the list key type." %>
<%@ attribute name="listItem" required="true" type="java.lang.Object" description="Item for this checkbox item in group of checkboxes" %>
<%@ attribute name="itemKeyValue" required="false" type="java.lang.Object" description="Item key value for this checkbox item.  This is an override for the value submitted to the server." %>
<%@ attribute name="listKey" type="java.lang.String" description="The key to use for the checkboxes.  The key is the value submitted as the parameter value.  If unspecified, the object's toString() method will be used.  Must be a valid property name for the type of objects specified in the list parameter." %>
<%@ attribute name="listValue" type="java.lang.String" description="The value to use for the checkboxes.  The value is the value displayed to the user.  If unspecified, the object's toString() method will be used.  Must be a valid property name for the type of objects specified in the list parameter." %>
<%@ attribute name="descriptionValue" type="java.lang.String" description="The value to use for checkbox descriptions (below the checkbox)." %>
<%@ attribute name="description" type="java.lang.String" description="The literal value to use for checkbox description (below the checkbox)." %>
<%@ attribute name="useIterableListItem" type="java.lang.Boolean" description="Set to true to use iterable list item for display" %>
<%@ attribute name="disabled" type="java.lang.Boolean" %>
<%@ attribute name="setDisabledExceptForListKey" type="java.lang.Object" description="Set all checkboxes in the list disabled except for the checkbox with this value." %>
<%@ attribute name="readonly" type="java.lang.Boolean" %>
<%@ attribute name="tabindex" type="java.lang.Integer" %>
<%@ attribute name="valueIsBoolean" type="java.lang.Boolean" description="Value will be a boolean to say checked or not" %>
<%@ attribute name="id" required="true" type="java.lang.String" description="id for this single item in checkboxlist" %>
<%@ attribute name="horizontal" %>
<%@ attribute name="required" type="java.lang.Boolean" %>
<%@ attribute name="label" %>
<%@ attribute name="nolabel" %>
<%@ attribute name="cssClass" %>
<%@ attribute name="cssStyle" %>
<%@ attribute name="wrapperCssStyle" %>
<%@ attribute name="wrapperCssClass" type="java.lang.String" %>
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


<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gtu" uri="http://legacy.narrative.org/tags/tagUtils" %>

<g:set var="itemAsMapEntry" object="${listItem}" className="java.util.Map.Entry" setNullIfNotOfType="true" />
<g:set var="itemKey" object="${itemKeyValue!=null ? itemKeyValue : (itemAsMapEntry!=null ? itemAsMapEntry.key : (not empty listKey ? g:evaluateBeanProperty(listItem,listKey) : listItem))}" className="java.lang.Object" />

<g:set var="itemValue" className="java.lang.String">
    <c:choose>
        <c:when test="${useIterableListItem}">
            <g:set var="iterableListItemObjectForDisplay" object="${listItem}" scope="request"/>
            <jsp:doBody/>
        </c:when>
        <c:when test="${itemAsMapEntry!=null}">
            ${itemAsMapEntry.value}
        </c:when>
        <c:when test="${not empty listValue}">
            ${g:evaluateBeanProperty(listItem,listValue)}
        </c:when>
        <c:otherwise>
            ${listItem}
        </c:otherwise>
    </c:choose>
</g:set>

<g:set var="onclickVal">
    setLabelIsChecked('wwctrl_${g:escapeJavascriptLiteralString(id, false)}', this.checked);${onclick}
</g:set>

<%-- bl: for checkbox lists, require that the items in the specified list be the same
     object as the object in the value collection.  in other words, in order for this to work
     the objects in value must be equal to the list items according to Object.equals().
     nb. test for an empty value since value doesn't always have to be supplied. it can be supplied
     as null or the empty string, in which case contains will break. --%>
<c:if test="${not empty value}">
    <c:choose>
        <c:when test="${valueIsBoolean}">
            <g:set var="isChecked" object="${value}"/>
        </c:when>
        <c:when test="${itemAsMapEntry!=null or valueOfListKeyType}">
            <g:set var="isChecked" object="${g:contains(value,itemKey)}"/>
        </c:when>
        <c:otherwise>
            <g:set var="isChecked" object="${g:contains(value,listItem)}"/>
        </c:otherwise>
    </c:choose>
</c:if>

<g:set var="disabled" object="${disabled or (setDisabledExceptForListKey!=null and itemKey!=setDisabledExceptForListKey)}" className="java.lang.Boolean" />

<div ${g:attr('id',g:concat('wwgrp_',id))} style="${cssStyle}${empty cssStyle or empty wrapperCssStyle ? '' : ' '}${wrapperCssStyle}" class="wwgrp${empty wrapperCssClass ? '' : ' '}${wrapperCssClass}">
    <div ${g:attr('id',g:concat('wwctrl_',id))} style="${cssStyle}" class="wwctrl${isChecked ? '_on' : ''}">
        ${gtu:createInput("checkbox",name,itemKey,false,formId,null,null,null,disabled,readonly,isChecked,tabindex,id,g:concat('noBorder ', cssClass),
                cssStyle,title,onclickVal,ondblclick,onmousedown,onmouseup,onmouseover,onmousemove,onmouseout,onfocus,onblur,onkeypress,
                onkeydown,onkeyup,null,null,null,onselect,onchange,null,null)}
        <%--bk: Why are we escaping the itemValue?--%>
        <c:if test="${not nolabel}">
            <label ${g:attr('for',id)} id="${id}_label" class="visualIEFloatFix checkboxLabel">${itemValue}${horizontal ? '&nbsp;&nbsp;' : null}</label>
        </c:if>

        <c:if test="${not empty descriptionValue or not empty description}">
            <div class="checkboxListItemDescription">
                ${not empty description ? description : g:evaluateBeanProperty(listItem,descriptionValue)}
            </div>
        </c:if>
    </div>
</div>