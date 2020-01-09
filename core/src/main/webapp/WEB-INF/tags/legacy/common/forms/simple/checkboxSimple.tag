<%@ tag pageEncoding="UTF-8" body-content="empty" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="value" required="true" %>
<%@ attribute name="isChecked" required="true" type="java.lang.Boolean" %>
<%@ attribute name="disabled" type="java.lang.Boolean" %>
<%@ attribute name="readonly" type="java.lang.Boolean" %>
<%@ attribute name="useTableLayout" type="java.lang.Boolean" %>
<%@ attribute name="tabindex" type="java.lang.Integer" %>
<%@ attribute name="id" %>
<%@ attribute name="required" type="java.lang.Boolean" %>
<%@ attribute name="useStandardDisplay" type="java.lang.Boolean" %>
<%@ attribute name="label" %>
<%@ attribute name="description" %>
<%@ attribute name="afterLabelText" %>
<%@ attribute name="inline" %>
<%@ attribute name="cssClass" %>
<%@ attribute name="wrapperCssStyle" %>
<%@ attribute name="wrapperCssClass" %>
<%@ attribute name="labelCssClass" %>
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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>                          
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="gtu" uri="http://legacy.narrative.org/tags/tagUtils" %>

<g:ref var="networkAction" className="org.narrative.network.shared.baseactions.NetworkAction" />
<g:set var="controlFieldErrors" object="${networkAction.fieldErrors[name]}" className="java.util.Collection" />
<g:set var="checkboxId" object="${gtu:createInputId(id,formId,name)}"/>

<g:set var="onclickVal">
    updateCheckboxLabel('${checkboxId}');${onclick}
</g:set>

<g:set var="cssClass">${not useStandardDisplay? 'noLabel':''} noBorder ${cssClass}</g:set>
<g:set var="newCssStyle">
    border:none;background:transparent;${cssStyle}
</g:set>
<g:set var="checkboxHtml">
    ${gtu:createInput("checkbox",name,value,false,formId,null,null,null,disabled,readonly,isChecked,tabindex,checkboxId,cssClass,
    newCssStyle,title,onclickVal,ondblclick,onmousedown,onmouseup,onmouseover,onmousemove,onmouseout,onfocus,onblur,onkeypress,
    onkeydown,onkeyup,null,null,null,onselect,onchange,null,null)}
</g:set>


<c:choose>
    <c:when test="${useStandardDisplay}">
        <gfs:controlwrapper
                name="${name}"
                id="${id}"
                required="${required}"
                label="${label}"
                cssStyle="${wrapperCssStyle}"
                cssClass="${wrapperCssClass}">
            ${checkboxHtml}
        </gfs:controlwrapper>
        <div class="clear">&nbsp;</div>
    </c:when>
    <c:otherwise>
        <g:set var="checkboxLabelHtml">
            <label ${g:attr('for',checkboxId)} ${g:attr('id',g:concat(checkboxId,'_label'))} class="${not empty controlFieldErrors ? 'checkboxErrorLabel ' : (not useTableLayout?'checkboxLabel ':'')}${labelCssClass}">${label}</label>
        </g:set>
        <c:choose>
            <c:when test="${useTableLayout}">
                <div ${g:attr('id',g:concat('wwgrp_', checkboxId))} class="wwgrp ${wrapperCssClass}" style="${inline?"display:inline":""}${wrapperCssStyle}">
                    <div ${g:attr('id',g:concat('wwctrl_', checkboxId))} class="wwctrl${isChecked ? '_on' : ''}" style="${inline?"display:inline":""}">
                        <table class="generic">
                            <tr>
                                <td style="vertical-align:top" class="cBoxOrIcon">${checkboxHtml}</td>
                                <td style="vertical-align:top"${g:condAttr('class', labelCssClass)}>${checkboxLabelHtml}</td>
                                <c:if test="${not empty afterLabelText}">
                                    <td style="vertical-align:top">${afterLabelText}</td>
                                </c:if>
                            </tr>
                        </table>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div ${g:attr('id',g:concat('wwgrp_', checkboxId))} class="wwgrp ${wrapperCssClass}" style="${inline?"display:inline":""}${wrapperCssStyle}">
                    <div ${g:attr('id',g:concat('wwctrl_', checkboxId))} class="wwctrl${isChecked ? '_on' : ''}" style="${inline?"display:inline":""}">
                        ${checkboxHtml}
                        ${checkboxLabelHtml}
                        ${afterLabelText}
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>

<c:if test="${not empty description}">
    <div ${g:attr('id',g:concat('wwgrp_desc_', checkboxId))} class="checkboxOptionDescription"${g:condAttr('style', wrapperCssStyle)}>${description}</div>
</c:if>
