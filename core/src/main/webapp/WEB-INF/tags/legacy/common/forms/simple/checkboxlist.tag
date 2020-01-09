<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="value" required="true" type="java.lang.Object" description="All of the currently checked checkboxes.  Must be a Collection or an array." %>
<%@ attribute name="valueOfListKeyType" type="java.lang.Boolean" description="True if the value specified is a collection of the list key type." %>
<%@ attribute name="list" required="true" type="java.lang.Object" description="The list of checkboxes.  Must be an array, collection, or map." %>
<%@ attribute name="listKey" type="java.lang.String" description="The key to use for the checkboxes.  The key is the value submitted as the parameter value.  If unspecified, the object's toString() method will be used.  Must be a valid property name for the type of objects specified in the list parameter." %>
<%@ attribute name="listValue" type="java.lang.String" description="The value to use for the checkboxes.  The value is the value displayed to the user.  If unspecified, the object's toString() method will be used.  Must be a valid property name for the type of objects specified in the list parameter." %>
<%@ attribute name="descriptionValue" type="java.lang.String" description="The value to use for checkbox descriptions (below the checkbox)." %>
<%@ attribute name="useIterableListItem" type="java.lang.Boolean" description="Set to true to use iterable list item for display" %>
<%@ attribute name="disabled" type="java.lang.Boolean" %>
<%@ attribute name="setDisabledExceptForListKey" type="java.lang.Object" description="Set all checkboxes in the list disabled except for the checkbox with this value." %>
<%@ attribute name="disableCheckboxesWithValues" type="java.lang.Object" description="Set checkboxes in the supplied list of values as disabled." %>
<%@ attribute name="readonly" type="java.lang.Boolean" %>
<%@ attribute name="tabindex" type="java.lang.Integer" %>
<%@ attribute name="id" %>
<%@ attribute name="horizontal" description="Causes the checkboxes to be presented horizontally, one after the other.  Two spaces after each one, and each item is grouped with its checkbox so that when they line break they line break together." %>
<%@ attribute name="breakEveryTwoHorizontalInputs" type="java.lang.Boolean" %>
<%@ attribute name="required" type="java.lang.Boolean" %>
<%@ attribute name="label" %>
<%@ attribute name="nolabel" %>
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
<%@ attribute name="isUsingTopLabel" type="java.lang.Boolean" description="sets the labelPosition to top and the proper css class to the form element" %>
<%@ attribute name="extraListItems" type="java.lang.String" %>
<%@ attribute name="showExtraListItemsAtBottom" type="java.lang.Boolean" %>
<%@ attribute name="useKeyInIdInsteadOfIndex" type="java.lang.Boolean" %>
<%@ attribute name="hideCheckboxesWithValues" type="java.lang.Object" %>
<%@ attribute name="indentedValues" type="java.util.Collection" %>
<%@ attribute name="description" type="java.lang.String" %>


<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gtu" uri="http://legacy.narrative.org/tags/tagUtils" %>

<g:set var="checkboxListId"  object="${gtu:createInputId(id,formId,name)}"/>
<g:set var="cssClass"><c:if test="${isUsingTopLabel or empty label}">noLabel </c:if>${cssClass}</g:set>


<gfs:controlwrapper
        name="${name}"
        id="${checkboxListId}"
        required="${required}"
        label="${label}"
        isUsingTopLabel="${isUsingTopLabel}">

    <c:if test="${not empty description and isUsingTopLabel}">
        <div class="textInputDescription">
            ${description}
        </div>
    </c:if>

    <g:set var="checkboxes">
        <c:if test="${not empty extraListItems and not showExtraListItemsAtBottom}">
            ${extraListItems}
        </c:if>

        <g:forEach items="${list}" obj="listItem" className="java.lang.Object" varStatus="status">
            <gfs:checkboxlistitem
                    name="${name}"
                    value="${value}"
                    valueOfListKeyType="${valueOfListKeyType}"
                    listItem="${listItem}"
                    listKey="${listKey}"
                    listValue="${listValue}"
                    descriptionValue="${descriptionValue}"
                    useIterableListItem="${useIterableListItem}"
                    disabled="${disabled or g:contains(disableCheckboxesWithValues, listItem)}"
                    setDisabledExceptForListKey="${setDisabledExceptForListKey}"
                    readonly="${readonly}"
                    tabindex="${tabindex}"
                    id="${checkboxListId}-${not useKeyInIdInsteadOfIndex ? (status.index+1) : not empty listKey ? g:evaluateBeanProperty(listItem,listKey) : listItem}"
                    horizontal="${horizontal}"
                    required="${required}"
                    cssClass="${cssClass}"
                    cssStyle="${cssStyle}"
                    wrapperCssStyle="${g:contains(hideCheckboxesWithValues, listItem) ? 'display: none' : null}"
                    wrapperCssClass="${g:contains(indentedValues, listItem) ? 'indentedCheckboxWrapper' : null}"
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
                    onchange="${onchange}">
                <jsp:doBody/>
            </gfs:checkboxlistitem>
            <c:if test="${horizontal and breakEveryTwoHorizontalInputs and status.index mod 2 eq 1}"><div class="clear">&nbsp;</div></c:if>
        </g:forEach>

        <c:if test="${not empty extraListItems and showExtraListItemsAtBottom}">
            ${extraListItems}
        </c:if>
    </g:set>

    <c:choose>
        <c:when test="${horizontal}">
            <div class="${not isUsingTopLabel ? 'wwInfoRight ' : ''}horizontalCheckboxListItems">
                ${checkboxes}
            </div>
            <div class="clear">&nbsp;</div>
        </c:when>
        <c:otherwise>
            ${checkboxes}
        </c:otherwise>
    </c:choose>

    <c:if test="${not empty description and not isUsingTopLabel}">
        <div class="textInputDescriptionUnderInput">
            ${description}
        </div>
    </c:if>

</gfs:controlwrapper>