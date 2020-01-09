<%--
  User: jonmark
  Date: 8/13/12
  Time: 9:21 AM
--%>
<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ attribute name="id" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="inputId" type="java.lang.String" %>
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
<%@ attribute name="useIterableListItem" type="java.lang.Boolean" description="Set to true to use iterable list item for display" %>
<%@ attribute name="multiple" type="java.lang.Boolean" description="Creates a multiple select. The tag will pre-select multiple values if the values are passed as an Array (of appropriate types) via the value attribute. Passing a Collection may work too? Haven't tested this." %>
<%@ attribute name="disabledOptions" type="java.lang.Object" %>

<%--
    jw: This label will be used to group all options except for the extraValue option when presented at the top of the
        list.  Then that option will appear outside of the group.  This allows us to have empty or default options.
--%>
<%@ attribute name="groupLabelForListOptions" type="java.lang.String" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core" %>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network"%>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="gmt" tagdir="/WEB-INF/tags/legacy/master" %>
<%@ taglib prefix="gta" tagdir="/WEB-INF/tags/legacy/area" %>
<%@ taglib prefix="gtu" uri="http://legacy.narrative.org/tags/tagUtils" %>

<c:if test="${empty inputId}">
    <g:set var="inputId" object="${gtu:createInputId(id,formId,name)}"/>
</c:if>

<g:set var="isUsingExtraOption" object="${not empty extraValue or not empty extraValue2 or not empty extraValue3}" />
<c:if test="${isUsingExtraOption}">
    <g:set var="emptyOptionHtml">
        <option value="" ${empty value?'selected="selected"':''}></option>
    </g:set>
    <g:set var="extraOptionHtml">
        <c:if test="${not empty extraValue}">
            <option ${g:attr('value',extraKey)} ${extraKey eq value?'selected="selected"':''}>${extraValue}</option>
        </c:if>
        <c:if test="${not empty extraValue2}">
            <option ${g:attr('value',extraKey2)} ${extraKey2 eq value?'selected="selected"':''}>${extraValue2}</option>
        </c:if>
        <c:if test="${not empty extraValue3}">
            <option ${g:attr('value',extraKey3)} ${extraKey3 eq value?'selected="selected"':''}>${extraValue3}</option>
        </c:if>
    </g:set>

    <c:if test="${addExtraItemToTop==null or addExtraItemToTop}">
        ${extraOptionHtml}
        <c:if test="${emptyOption}">
            ${emptyOptionHtml}
        </c:if>
    </c:if>
</c:if>

<g:set var="options">
    <g:forEach items="${list}" obj="listItem" className="java.lang.Object" varStatus="status">
        <g:set var="currItem" object="${status.index+1}" />
        <g:set var="itemAsMapEntry" object="${listItem}" className="java.util.Map.Entry" setNullIfNotOfType="true" />
        <g:set var="itemKey" object="${itemAsMapEntry!=null ? itemAsMapEntry.key : (not empty listKey ? g:evaluateBeanProperty(listItem,listKey) : listItem)}" className="java.lang.Object" />
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

        <g:set var="isItemSelected">
            <c:choose>
                <c:when test="${multiple}">
                    <%-- bl: for multiple selects, require that the items in the specified list be the same
                         object as the object in the value collection.  in other words, in order for this to work
                         the objects in value must be equal to the list items according to Object.equals() --%>
                    <c:choose>
                        <c:when test="${not empty value}">
                            ${g:contains(value,listItem)}
                        </c:when>
                        <c:otherwise>false</c:otherwise>
                    </c:choose>
                </c:when>
                <c:otherwise>
                    <%-- in the event that this is not a multiple select, we should evaluate the bean property
                         from the listKey on the value object and test that value against the current itemKey.
                         we could just test for object equality of the value with the listItem, but this is
                         more thorough and supports two distinct object references with the same key matching
                         as opposed to just objects with the same reference. --%>
                    ${itemAsMapEntry!=null ? (itemKey==value) : (not empty listKey ? (valueOfListKeyType ? value==itemKey : (g:evaluateBeanProperty(value,listKey)==itemKey)) : (value==listItem))}
                </c:otherwise>
            </c:choose>
        </g:set>

        <g:set var="disabled" object="${g:contains(disabledOptions, listItem)}" className="java.lang.Boolean" />
        <option value="${itemKey}" ${isItemSelected ? 'selected="selected"' : ''}${g:condAttr('disabled', disabled ? 'true' : null)} id="${inputId}_${itemKey}">${itemValue}</option>
    </g:forEach>

    <c:if test="${isUsingExtraOption and addExtraItemToTop!=null and not addExtraItemToTop}">
        <c:if test="${emptyOption}">
            ${emptyOptionHtml}
        </c:if>
        ${extraOptionHtml}
    </c:if>
</g:set>

<c:choose>
    <c:when test="${not empty groupLabelForListOptions and not empty options}">
        <optgroup label="${groupLabelForListOptions}">
            ${options}
        </optgroup>
    </c:when>
    <c:otherwise>
        ${options}
    </c:otherwise>
</c:choose>