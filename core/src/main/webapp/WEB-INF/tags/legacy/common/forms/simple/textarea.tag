<%@ tag pageEncoding="UTF-8" body-content="empty" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="value" required="true" %>
<%@ attribute name="rows" required="true" %>
<%@ attribute name="cols" required="true" %>
<%@ attribute name="wrap" %>
<%@ attribute name="isRawHtml" description="true if the value is pure, unmodified html" %>
<%@ attribute name="disabled" type="java.lang.Boolean" %>
<%@ attribute name="readonly" type="java.lang.Boolean" %>
<%@ attribute name="tabindex" type="java.lang.Integer" %>
<%@ attribute name="id" %>
<%@ attribute name="required" type="java.lang.Boolean" %>
<%@ attribute name="label" %>
<%@ attribute name="cssClass" %>
<%@ attribute name="cssStyle" %>
<%@ attribute name="wrapperCssStyle" %>
<%@ attribute name="title" %>
<%@ attribute name="description" %>
<%@ attribute name="upperDescription" %>
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
<%@ attribute name="placeholder" type="java.lang.String" %>
<%@ attribute name="preControlHtml" %>
<%@ attribute name="postControlHtml" %>


<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="gtu" uri="http://legacy.narrative.org/tags/tagUtils" %>

<g:set var="textId" object="${gtu:createInputId(id,formId,name)}"/>
<g:set var="cssClass"><c:if test="${isUsingTopLabel or empty label}">noLabel </c:if> form_css ${cssClass}</g:set>

<gfs:controlwrapper
        name="${name}"
        id="${textId}"
        required="${required}"
        label="${label}"
        isUsingTopLabel="${isUsingTopLabel}"
        cssStyle="${wrapperCssStyle}"
        preControlHtml="${preControlHtml}"
        postControlHtml="${postControlHtml}"
        >
    <c:if test="${not empty upperDescription}">
        <div class="smallfont ${isUsingTopLabel ? '' : 'wwInfoRight'}">
            ${upperDescription}
        </div>
    </c:if>

    <gfs:textPlaceholder inputId="${textId}" placeholder="${placeholder}">
        <textarea name="${name}" rows="${rows}" cols="${cols}"
            ${g:condAttr('wrap',wrap)}
            ${disabled ? 'disabled="disabled"' : ''}
            ${readonly ? 'readonly="readonly"' : ''}
            ${g:condAttr('tabindex',tabindex)}
            ${g:condAttr('id',textId)}
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
            ${g:condAttr('onchange',onchange)}
            ${g:condAttr('class',cssClass)}
            ${g:condAttr('style',cssStyle)}><%-- bl: no longer going to enable disabled HTML in textarea values.  should keep these escaped
            so that in the event that there is HTML in the textarea value, it won't break the rest of the page.
            ${enableDisabledHtmlInValue==null or enableDisabledHtmlInValue ? g:enableDisabledHtml(value): value}

            jw: if we leave the html unescaped for pure html then the browser will actually try to render it to a limited degree.  What
                this has translated to in my experience is that script blocks get resolved, and then are not displayed within
                the text area.  By disabling the HTML everything displays as expected, and no HTML is actually resolved or rendered.
            --%>${isRawHtml ? g:disableHtml(value) : value}</textarea>
    </gfs:textPlaceholder>
        <c:if test="${not empty description}">
            <div class="smallfont ${isUsingTopLabel ? '' : 'wwInfoRight'}" style="padding-bottom:5px;">
                ${description}
            </div>
        </c:if>
</gfs:controlwrapper>