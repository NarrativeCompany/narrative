<%@ attribute name="name" required="true" %>
<%@ attribute name="value" required="true" type="java.lang.Object" description="All of the currently selected items in the select list.  If multiple=true, must be a Collection or an array.  If multiple=false, must be an object of the type in the list collection." %>
<%@ attribute name="valueOfListKeyType" type="java.lang.Boolean" description="True if the value specified is of the list key type." %>
<%@ attribute name="list" required="true" type="java.lang.Object" description="Iterable source to populate from. If the list is a Map (key, value), the Map key will become the option &quot;value&quot; parameter and the Map value will become the option body." %>
<%@ attribute name="listKey" type="java.lang.String" description="Property of list objects to get field value from.  The key is the value submitted as the parameter value.  If unspecified, the object's toString() method will be used.  Must be a valid property name for the type of objects specified in the list parameter." %>
<%@ attribute name="listValue" type="java.lang.String" description="Property of list objects to get field content from.  The value is the value displayed to the user.  If unspecified, the object's toString() method will be used.  Must be a valid property name for the type of objects specified in the list parameter." %>
<%@ attribute name="extraKey" type="java.lang.String" description="Key for an extra item in list. An empty extraKey is allowed.  extraValue controls if this field will be used or not." %>
<%@ attribute name="extraValue" type="java.lang.String" description="Value expression for extra item in list.  Defaults to empty string." %>
<%@ attribute name="extraKey2" type="java.lang.String" description="Key for an extra item in list. An empty extraKey is allowed.  extraValue controls if this field will be used or not." %>
<%@ attribute name="extraValue2" type="java.lang.String" description="Value expression for extra item in list.  Defaults to empty string." %>
<%@ attribute name="extraKey3" type="java.lang.String" description="Key for an extra item in list. An empty extraKey is allowed.  extraValue controls if this field will be used or not." %>
<%@ attribute name="extraValue3" type="java.lang.String" description="Value expression for extra item in list.  Defaults to empty string." %>
<%@ attribute name="addExtraItemToTop" type="java.lang.Boolean" description="Defaults to true.  Means the extraKey and extraValue will be added to the top of the list.  If false, the extra item will be added to the bottom of the list." %>
<%@ attribute name="emptyOption" type="java.lang.Boolean" description="Whether or not to add an empty (--) option between the extra item option and the rest of the list items." %>
<%@ attribute name="useBodyForListItems" type="java.lang.Boolean" description="Set to true to use iterable list item for display" %>
<%@ attribute name="useIterableListItem" type="java.lang.Boolean" description="Set to true to use iterable list item for display" %>
<%@ attribute name="size" type="java.lang.Integer" description="Size of the element box (# of elements to show)" %>
<%@ attribute name="multiple" type="java.lang.Boolean" description="Creates a multiple select. The tag will pre-select multiple values if the values are passed as an Array (of appropriate types) via the value attribute. Passing a Collection may work too? Haven't tested this." %>
<%@ attribute name="disabled" type="java.lang.Boolean" %>
<%@ attribute name="tabindex" %>
<%@ attribute name="id" %>
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
<%@ attribute name="disabledOptions" type="java.lang.Object" %>


<%--
    jw: This label will be used to group all options except for the extraValue option when presented at the top of the
        list.  Then that option will appear outside of the group.  This allows us to have empty or default options.
--%>
<%@ attribute name="groupLabelForListOptions" type="java.lang.String" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="gtu" uri="http://legacy.narrative.org/tags/tagUtils" %>

<g:set var="inputId" object="${gtu:createInputId(id,formId,name)}"/>
<g:set var="cssClass">form_css ${cssClass}</g:set>

<select ${g:attr('name',name)}
    ${g:condAttr('size',size)}
    ${g:condAttr('disabled', disabled ? 'disabled' : '')}
    ${g:condAttr('tabindex',tabindex)}
    ${g:condAttr('id',inputId)}
    ${g:condAttr('class',cssClass)}
    ${g:condAttr('style',cssStyle)}
    ${g:condAttr('title',title)}
    ${g:condAttr('multiple', multiple ? 'multiple' : '')}
    ${g:condAttr('onclick',onclick)}
    ${g:condAttr('ondblclick',ondblclick)}
    ${g:condAttr('onmousedown',onmousedown)}
    ${g:condAttr('onmouseup',onmouseup)}
    ${g:condAttr('onmouseover',onmouseover)}
    ${g:condAttr('onmousemove',onmousemove)}
    ${g:condAttr('onmouseout',onmouseout)}
    ${g:condAttr('onfocus',onfocus)}
    ${g:condAttr('onblur',onblur)}
    ${g:condAttr('onkeypress',onkeypress)}
    ${g:condAttr('onkeydown',onkeydown)}
    ${g:condAttr('onkeyup',onkeyup)}
    ${g:condAttr('onselect',onselect)}
    ${g:condAttr('onchange',onchange)} >

    <c:choose>
        <c:when test="${useBodyForListItems}">
            <%-- allow extra items to be specified for things like "All X" or whatever the caller decides --%>
            <gfs:selectSimpleOptions
                    id="${id}"
                    name="${name}"
                    inputId="${inputId}"
                    value="${value}"
                    list="${list}"
                    listKey="${listKey}"
                    listValue="${listValue}"
                    extraKey="${extraKey}"
                    extraValue="${extraValue}"
                    extraKey2="${extraKey2}"
                    extraValue2="${extraValue2}"
                    extraKey3="${extraKey3}"
                    extraValue3="${extraValue3}"
                    addExtraItemToTop="${addExtraItemToTop}"
                    emptyOption="${emptyOption}"
                    groupLabelForListOptions="${groupLabelForListOptions}"
            />
            <jsp:doBody />
        </c:when>
        <c:otherwise>
            <gfs:selectSimpleOptions
                    id="${id}"
                    name="${name}"
                    inputId="${inputId}"
                    value="${value}"
                    valueOfListKeyType="${valueOfListKeyType}"
                    list="${list}"
                    listKey="${listKey}"
                    listValue="${listValue}"
                    extraKey="${extraKey}"
                    extraValue="${extraValue}"
                    extraKey2="${extraKey2}"
                    extraValue2="${extraValue2}"
                    extraKey3="${extraKey3}"
                    extraValue3="${extraValue3}"
                    addExtraItemToTop="${addExtraItemToTop}"
                    emptyOption="${emptyOption}"
                    useIterableListItem="${useIterableListItem}"
                    multiple="${multiple}"
                    groupLabelForListOptions="${groupLabelForListOptions}"
                    disabledOptions="${disabledOptions}">
                <jsp:doBody />
            </gfs:selectSimpleOptions>
        </c:otherwise>
    </c:choose>

</select>