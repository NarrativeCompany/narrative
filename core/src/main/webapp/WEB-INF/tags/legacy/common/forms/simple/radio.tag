<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="value" required="true" type="java.lang.Object" description="All of the currently selected items in the select list.  If multiple=true, must be a Collection or an array.  If multiple=false, must be an object of the type in the list collection." %>
<%@ attribute name="valueOfListKeyType" type="java.lang.Boolean" description="True if the value specified is of the list key type." %>
<%@ attribute name="list" required="true" type="java.lang.Object" description="Iterable source to populate from. If the list is a Map (key, value), the Map key will become the option &quot;value&quot; parameter and the Map value will become the option body." %>
<%@ attribute name="listKey" type="java.lang.String" description="Property of list objects to get field value from.  The key is the value submitted as the parameter value.  If unspecified, the object's toString() method will be used.  Must be a valid property name for the type of objects specified in the list parameter." %>
<%@ attribute name="listValue" type="java.lang.String" description="Property of list objects to get field content from.  The value is the value displayed to the user.  If unspecified, the object's toString() method will be used.  Must be a valid property name for the type of objects specified in the list parameter." %>
<%@ attribute name="extraKey" type="java.lang.Object" description="Key for an extra item in list. Must not be empty! &quot;'-1'&quot; and &quot;''&quot; is correct, &quot;&quot; is bad." %>
<%@ attribute name="extraValue" type="java.lang.String" description="Value expression for extra item in list.  Defaults to empty string." %>
<%@ attribute name="addExtraItemToTop" type="java.lang.Boolean" description="Defaults to true.  Means the extraKey and extraValue will be added to the top of the list.  If false, the extra item will be added to the bottom of the list." %>
<%@ attribute name="selectExtraValIfEmpty" type="java.lang.Boolean" description="Defaults to true. If false, the extra value won't be select if the value is empty" %>
<%@ attribute name="useIterableListItem" type="java.lang.Boolean" description="Set to true to use iterable list item for display" %>
<%@ attribute name="extraDataMap" type="java.util.Map" description="Extra data to display inline with each element (if the map entry exists corresponding to the radio button key)" %>
<%@ attribute name="disabled" type="java.lang.Boolean" %>
<%@ attribute name="disabledKeys" type="java.util.Collection" %>
<%@ attribute name="tabindex" %>
<%@ attribute name="id" %>
<%@ attribute name="required" type="java.lang.Boolean" %>
<%@ attribute name="doAltStyleDisplay" type="java.lang.Boolean" %>
<%@ attribute name="label" %>
<%@ attribute name="cssClass" %>
<%@ attribute name="cssStyle" %>
<%@ attribute name="title" %>
<%@ attribute name="propertyToPassToJS" description="a property of list object ot pass as parameter to javascript function" %>
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
<%@ attribute name="doLineBreak" description="This will add a br to the end of each radio"  %>
<%@ attribute name="doLineBreakEveryTwo" description="This will add a br to the end of each radio"  %>
<%@ attribute name="doLineBreakEveryThree"  %>
<%@ attribute name="isUsingTopLabel" type="java.lang.Boolean" description="sets the labelPosition to top and the proper css class to the form element" %>
<%@ attribute name="valueLabelCssClass" type="java.lang.String" description="optional class to apply to all labels used for values" %>
<%@ attribute name="preControlHtml" type="java.lang.String" %>
<%@ attribute name="postControlHtml" type="java.lang.String" %>
<%@ attribute name="description" type="java.lang.String" %>
<%@ attribute name="useTableForRadioDisplay" type="java.lang.Boolean" %>
<%@ attribute name="tableCssClass" type="java.lang.String" %>
<%@ attribute name="wrapperCssClass" %>
<%@ attribute name="wrapperCssStyle" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="gtu" uri="http://legacy.narrative.org/tags/tagUtils" %>

<g:set var="inputId" object="${gtu:createInputId(id,formId,name)}"/>
<g:set var="cssClass"><c:if test="${isUsingTopLabel or empty label}">noLabel </c:if>${cssClass} noBorder</g:set>

<gfs:controlwrapper
        id="${inputId}"
        name="${name}"
        required="${required}"
        label="${label}"
        cssClass="${wrapperCssClass}"
        cssStyle="${wrapperCssStyle}"
        isUsingTopLabel="${isUsingTopLabel}"
        preControlHtml="${preControlHtml}"
        postControlHtml="${postControlHtml}">

    <c:if test="${not empty description and isUsingTopLabel}">
        <div class="textInputDescription">
            ${description}
        </div>
    </c:if>

    <g:set var="isShowExtraOptionAtTop" object="${addExtraItemToTop==null or addExtraItemToTop}" />
    <g:set var="isUsingExtraOption" object="${not empty extraValue}" />
    <c:if test="${isUsingExtraOption}">
        <g:set var="extraOptionCssClasses">
            <c:choose>
                <c:when test="${isShowExtraOptionAtTop}">${cssClass} noBorder</c:when>
                <c:otherwise>noLabel noBorder</c:otherwise>
            </c:choose>
        </g:set>
        <g:set var="extraRadioId" object="${g:concat(inputId,extraKey)}" />
        <g:set var="extraOptionHtml">
            ${gtu:createInput("radio",name,extraKey,false,formId,null,null,null,disabled or g:contains(disabledKeys, extraKey),false,extraKey==value or ((selectExtraValIfEmpty==null or selectExtraValIfEmpty) and empty value),tabindex,extraRadioId,extraOptionCssClasses,
                g:concat('border:none;background:transparent;', cssStyle),title,onclick,ondblclick,onmousedown,onmouseup,onmouseover,onmousemove,onmouseout,onfocus,onblur,onkeypress,
                onkeydown,onkeyup,null,null,null,onselect,onchange,null,null)}
            <label ${g:attr('for',extraRadioId)} ${g:condAttr('class', valueLabelCssClass)}>
                ${extraValue}
            </label>
            <c:if test="${extraDataMap!=null and not empty extraDataMap[extraKey]}">${extraDataMap[extraKey]}</c:if>
        </g:set>

        <c:if test="${isShowExtraOptionAtTop}">
            <c:choose>
                <c:when test="${doAltStyleDisplay}">
                    <div class="radio-div">${extraOptionHtml}</div>
                </c:when>
                <c:otherwise>${extraOptionHtml}</c:otherwise>
            </c:choose>
        </c:if>
    </c:if>

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

        <%-- we should evaluate the bean property from the listKey on the value object and
             test that value against the current itemKey. otherwise, just do an equality check on the value
             against the current list item. --%>
        <g:set var="isChecked" object="${itemAsMapEntry!=null ? (itemKey==value) : (not empty listKey ? (valueOfListKeyType ? value==itemKey : (g:evaluateBeanProperty(value,listKey)==itemKey)) : (value==listItem))}" />

        <g:set var="radioId" object="${g:concat(inputId,itemKey)}" />
        <g:set var="cssClasses">
            <%-- bl: use noLabel for the first radio button.  for all of the others, use noLabel so that they don't have a
                 bunch of spacing between them. --%>
            <c:choose>
                <c:when test="${(status.first and not (isUsingExtraOption and isShowExtraOptionAtTop)) or useTableForRadioDisplay}">${cssClass} noBorder</c:when>
                <c:otherwise>${cssClass} noLabel noBorder</c:otherwise>
            </c:choose>
        </g:set>

        <g:set var="radioInput">
            ${gtu:createInput("radio",name,itemKey,false,formId,null,null,null,disabled or g:contains(disabledKeys, itemKey),false,isChecked,tabindex,radioId,cssClasses,
                g:concat('border:none;background:transparent;', cssStyle),title,onclick,ondblclick,onmousedown,onmouseup,onmouseover,onmousemove,onmouseout,onfocus,onblur,onkeypress,
                onkeydown,onkeyup,null,null,null,onselect,onchange,null,null)}
        </g:set>

        <g:set var="radioLabel">
            <label ${g:attr('for',radioId)}  ${g:condAttr('class', valueLabelCssClass)}>
                ${itemValue}
            </label>
        </g:set>

        <g:set var="radioDisplay">
            <c:choose>
                <c:when test="${useTableForRadioDisplay}">
                    <div class="divTable ${tableCssClass}">
                        <div class="divRow">
                            <div class="divCell radioInput">
                                ${radioInput}
                            </div>
                            <div class="divCell radioLabel">
                                ${radioLabel}
                            </div>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    ${radioInput}
                    ${radioLabel}
                </c:otherwise>
            </c:choose>
        </g:set>


        <g:set var="radioOptionHtml">
            <c:choose>
                <c:when test="${doAltStyleDisplay}">
                    <div class="radio-div">${radioDisplay}
                        <c:if test="${extraDataMap!=null and not empty extraDataMap[itemKey]}">
                            <br/>${extraDataMap[itemKey]}
                        </c:if>
                    </div>
                </c:when>
                <c:when test="${doLineBreakEveryTwo}">
                    <div style="float:left;width:50%;padding-bottom:5px;">${radioDisplay}</div>
                    <c:if test="${status.index%2==1}"><div class="clear">&nbsp;</div></c:if>
                </c:when>
                <c:when test="${doLineBreakEveryThree}">
                    <div style="float:left;width:32%;padding-bottom:5px;">${radioDisplay}</div>
                    <c:if test="${status.index%3==2}"><div class="clear">&nbsp;</div></c:if>
                </c:when>
                <c:otherwise>
                    ${radioDisplay}${' '}
                    <c:if test="${doLineBreak and (not status.last or g:exists(extraValue))}"><br/></c:if>
                    <c:if test="${extraDataMap!=null and not empty extraDataMap[itemKey]}">${extraDataMap[itemKey]}</c:if>
                </c:otherwise>
            </c:choose>
        </g:set>

        <c:choose>
            <c:when test="${doAltStyleDisplay}">
                ${radioOptionHtml}
            </c:when>

            <c:when test="${not isUsingTopLabel and not empty label and (doLineBreak or doLineBreakEveryTwo or doLineBreakEveryThree)}">
                <div class="brokenRadioOptions">${radioOptionHtml}</div>
            </c:when>

            <c:when test="${useTableForRadioDisplay}">
                ${radioOptionHtml}
            </c:when>

            <c:otherwise>
                <span class="radio-option">${radioOptionHtml}</span>
            </c:otherwise>
        </c:choose>
    </g:forEach>

    <c:if test="${isUsingExtraOption and not isShowExtraOptionAtTop}">
        <c:choose>
            <c:when test="${doAltStyleDisplay}">
                <div class="radio-div">${extraOptionHtml}</div>
            </c:when>
            <c:otherwise>${extraOptionHtml}</c:otherwise>
        </c:choose>
    </c:if>

    <c:if test="${not empty description and not isUsingTopLabel}">
        <div class="textInputDescriptionUnderInput">
            ${description}
        </div>
    </c:if>

</gfs:controlwrapper>