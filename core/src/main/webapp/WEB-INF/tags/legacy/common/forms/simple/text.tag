<%@ tag pageEncoding="UTF-8" body-content="empty" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="value" required="true" %>
<%@ attribute name="enableDisabledHtmlInValue" description="true if the value contains disabled html that should be enabled prior to display for edit.  defaults to true, so specify false to disable this behavior." %>
<%@ attribute name="size" type="java.lang.Integer" %>
<%@ attribute name="maxlength" type="java.lang.Integer" %>
<%@ attribute name="disabled" type="java.lang.Boolean" %>
<%@ attribute name="readonly" type="java.lang.Boolean" %>
<%@ attribute name="tabindex" type="java.lang.Integer" %>
<%@ attribute name="id" %>
<%@ attribute name="required" type="java.lang.Boolean" %>
<%@ attribute name="label" %>
<%@ attribute name="cssClass" %>
<%@ attribute name="cssStyle" %>
<%@ attribute name="wrapperCssClass" %>
<%@ attribute name="wrapperCssStyle" %>
<%@ attribute name="labelCssStyle" %>
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
<%@ attribute name="autocomplete" %>
<%@ attribute name="placeholder" description="Creates a label that is positioned over the input when empty" %>
<%@ attribute name="description" type="java.lang.String" %>
<%@ attribute name="preInputHtml" %>
<%@ attribute name="postInputHtml" %>
<%@ attribute name="isUsingTopLabel" type="java.lang.Boolean" description="sets the labelPosition to top and the proper css class to the form element" %>
<%@ attribute name="preControlHtml" %>
<%@ attribute name="postControlHtml" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="gtu" uri="http://legacy.narrative.org/tags/tagUtils" %>

<g:set var="textId" object="${gtu:createInputId(id,formId,name)}"/>
<g:set var="cssClass"><c:if test="${isUsingTopLabel or empty label}">noLabel </c:if>form_css ${cssClass}</g:set>
<gfs:controlwrapper
        name="${name}"
        id="${textId}"
        required="${required}"
        label="${label}"
        cssClass="${wrapperCssClass}"
        cssStyle="${wrapperCssStyle}"
        isUsingTopLabel="${isUsingTopLabel}"
        preControlHtml="${preControlHtml}"
        postControlHtml="${postControlHtml}"
        labelCssStyle="${labelCssStyle}">

    <c:if test="${not empty description and isUsingTopLabel}">
        <div class="textInputDescription">
            ${description}
        </div>
    </c:if>
    ${preInputHtml}

    <gfs:textPlaceholder inputId="${textId}" placeholder="${placeholder}">
        ${gtu:createInput("text",name,value,enableDisabledHtmlInValue==null or enableDisabledHtmlInValue,formId,size,maxlength,null,disabled,readonly,false,tabindex,textId,cssClass,
            cssStyle,title,onclick,ondblclick,onmousedown,onmouseup,onmouseover,onmousemove,onmouseout,onfocus,onblur,onkeypress,
            onkeydown,onkeyup,onpaste,oncut,oninput,onselect,onchange, empty placeholder or not empty autocomplete ? autocomplete : 'off',null)}
    </gfs:textPlaceholder>

    ${postInputHtml}
    <c:if test="${not empty description and not isUsingTopLabel}">
        <div class="textInputDescriptionUnderInput">
            ${description}
        </div>
    </c:if>

</gfs:controlwrapper>