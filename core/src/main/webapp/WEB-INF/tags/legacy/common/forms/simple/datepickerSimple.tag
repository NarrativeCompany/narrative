<%@ tag pageEncoding="UTF-8" body-content="empty" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="value" required="true" type="org.narrative.common.web.DateInput" %>
<%-- bl: intentionally not setting the type for the Integer fields or else values that are explicitly supplied
     will be converted to 0, which is not the desired behavior for any of them. --%>
<%@ attribute name="size" %>
<%@ attribute name="disabled" type="java.lang.Boolean" %>
<%@ attribute name="readonly" type="java.lang.Boolean" %>
<%@ attribute name="tabindex" %>
<%@ attribute name="id" %>
<%@ attribute name="cssClass" %>
<%@ attribute name="cssStyle" %>
<%@ attribute name="title" %>
<%@ attribute name="onlyShowDateYear" description="Set to true if you only want the year part of the date to show up." %>
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
<%@ attribute name="onDateSelectFunc" description="A function to call when a new date is selected via the date picker" %>
<%@ attribute name="anchorCalendarCorner" type="java.lang.String" description="Which corner of the calendar should be anchored to the target" %>
<%@ attribute name="anchorTargetCorner" type="java.lang.String" description="What corner of the target should the calendar be anchored to." %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="gtu" uri="http://legacy.narrative.org/tags/tagUtils" %>

<g:set var="datepickerId" object="${gtu:createInputId(id,formId,name)}"/>
<g:ref var="action" className="org.narrative.network.shared.baseactions.NetworkAction" />

<g:set var="defaultDateText" object="${gn:wordlet('tags.common.forms.simple.datepickerSimple.date')}" className="java.lang.String" />

<g:set var="cssClass">form_css datepickerInput ${cssClass}</g:set>

${gtu:createInput("text",g:concat(name,".dateString"),value==null or not value.valid ? defaultDateText : value.dateString,
    null,formId,size!=null?size:13,10,null,disabled,readonly,null,tabindex,datepickerId,cssClass,
    cssStyle,title,onclick,ondblclick,onmousedown,onmouseup,onmouseover,onmousemove,onmouseout,onfocus,onblur,onkeypress,
    onkeydown,onkeyup,onpaste,oncut,oninput,onselect,onchange,null,null)}

<c:if test="${not readonly and not disabled}">
    <script type="text/javascript">
        (function() {
            registerDatepicker(
                    '${datepickerId}',
                    '${g:escapeJavascriptLiteralString(defaultDateText, false)}',
                    ${empty onDateSelectFunc ? 'null' : onDateSelectFunc},
                    ${empty anchorCalendarCorner ? 'null' : g:getJavascriptStringValue(anchorCalendarCorner)},
                    ${empty anchorTargetCorner ? 'null' : g:getJavascriptStringValue(anchorTargetCorner)}
            );
        })();
    </script>
    <span id="dialog${datepickerId}" class="yui-skin-sam datepickerWrapper"></span>
</c:if>