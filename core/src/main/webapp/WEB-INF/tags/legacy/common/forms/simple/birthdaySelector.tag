<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>
<%@ attribute name="name" required="true" description="name should be the parameter name of the birthday field" %>
<%@ attribute name="value" required="true" type="org.narrative.common.web.Birthday" %>
<%@ attribute name="size" type="java.lang.Integer" %>
<%@ attribute name="disabled" type="java.lang.Boolean" %>
<%@ attribute name="readonly" type="java.lang.Boolean" %>
<%@ attribute name="tabindex" type="java.lang.Integer" %>
<%@ attribute name="id" %>
<%@ attribute name="required" type="java.lang.Boolean" %>
<%@ attribute name="label" %>
<%@ attribute name="cssClass" %>
<%@ attribute name="cssStyle" %>
<%@ attribute name="cssStyleYear" %>
<%@ attribute name="cssClassYear" %>
<%@ attribute name="wrapperCssClass" %>
<%@ attribute name="wrapperCssStyle" %>
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
<%@ attribute name="onpaste" description="Set the html onpaste attribute on rendered html element" %>
<%@ attribute name="oncut" description="Set the html oncut attribute on rendered html element" %>
<%@ attribute name="oninput" description="Set the html oninput attribute on rendered html element" %>
<%@ attribute name="onselect" description="Set the html onselect attribute on rendered html element" %>
<%@ attribute name="onchange" description="Set the html onchange attribute on rendered html element" %>
<%@ attribute name="isUsingTopLabel" type="java.lang.Boolean" description="sets the labelPosition to top and the proper css class to the form element" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network"%>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="gtu" uri="http://legacy.narrative.org/tags/tagUtils" %>

<g:set var="birthdayId" object="${gtu:createInputId(id,formId,name)}"/>
<g:set var="newCssClass"><c:if test="${isUsingTopLabel}">noLabel </c:if> form_css ${cssClass}</g:set>

<gfs:controlwrapper
        name="${name}"
        id="${birthdayId}"
        required="${required}"
        label="${label}"
        isUsingTopLabel="${isUsingTopLabel}"
        cssClass="${wrapperCssClass} birthdayFields"
        cssStyle="${wrapperCssStyle}">

    <select ${g:attr('name',g:concat(name, '.month'))}
        ${disabled ? 'disabled="disabled"' : ''}
        ${g:condAttr('tabindex',tabindex)}
        ${g:condAttr('id',g:concat(birthdayId,'_month'))}
        ${g:condAttr('class',newCssClass)}
        ${g:condAttr('style',cssStyle)}
        ${g:condAttr('title',title)}
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
        <option value="-1"
                ${(empty value.month or value.month<0) ? g:attr('selected','selected') : ''}
                >${gn:wordlet('month')}</option>
        <g:forLoop begin="0" end="11" varStatus="month">
            <option value="${month.index}"
                ${month.index==value.month ? g:attr('selected','selected') : ''}
                    >${gn:wordlet(g:concat('monthName.', month.index))}</option>
        </g:forLoop>
    </select>

    <select ${g:attr('name',g:concat(name, '.dayOfMonth'))}
        ${disabled ? 'disabled="disabled"' : ''}
        ${empty tabindex ? '' : g:attr('tabindex',tabindex+1)}
        ${g:condAttr('id',g:concat(birthdayId,'_day'))}
        ${g:condAttr('class',newCssClass)}
        ${g:condAttr('style',cssStyle)}
        ${g:condAttr('title',title)}
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
        <option value="-1"
                ${(empty value.dayOfMonth or value.dayOfMonth<1) ? g:attr('selected','selected') : ''}
                >${gn:wordlet('day')}</option>
        <g:forLoop begin="1" end="31" varStatus="dayOfMonth">
            <option value="${dayOfMonth.index}"
                ${dayOfMonth.index==value.dayOfMonth ? g:attr('selected','selected') : ''}
                    >${dayOfMonth.index}</option>
        </g:forLoop>
    </select>
    
    <g:set var="noYearSpecified" object="${empty value.year or value.year<1}" />
    
    <c:if test="${noYearSpecified}">
        <g:set var="onfocus">${onfocus};if(!this.hasCleared){this.value='';this.hasCleared=true;}</g:set>
    </c:if>
    <g:set var="cssStyleText">${cssStyleYear};${cssStyle}</g:set>
    <g:set var="cssClassText">${cssClassYear}${' '}${newCssClass}</g:set>
    <g:set var="yearId">${birthdayId}_year</g:set>

    <gfs:textPlaceholder inputId="${yearId}" placeholder="${gn:wordlet('year')}">
        ${gtu:createInput("text",g:concat(name, '.year'),noYearSpecified ? gn:wordlet('year') : value.year,false,formId,size,4,null,disabled,readonly,false,(empty tabindex ? null : tabindex+2),yearId,cssClassText,
            cssStyleText,title,onclick,ondblclick,onmousedown,onmouseup,onmouseover,onmousemove,onmouseout,onfocus,onblur,onkeypress,
            onkeydown,onkeyup,onpaste,oncut,oninput,onselect,onchange,'off',null)}
    </gfs:textPlaceholder>
</gfs:controlwrapper>