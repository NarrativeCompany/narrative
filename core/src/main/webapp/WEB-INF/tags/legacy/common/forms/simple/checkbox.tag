<%@ tag pageEncoding="UTF-8" body-content="empty" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="value" required="true" type="java.lang.Boolean" %>
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
<g:set var="checkboxId" object="${gtu:createInputId(id,formId,name)}"/>

<%-- bl: have a hidden field always submit the value of the checkbox, as opposed to the standard
     behavior of forms only submitting checkboxes when the checkbox is checked. --%>

<%-- bl: honor the disabled attribute.  if the checkbox is supposed to be disabled, then we certainly don't
     want the onclick event to trigger a change of the hidden form param.  thus, if it is disabled, disable
     the checkbox, but still allow for manual onclick events.  then, if it is not disabled, we'll include the
     onclick event to change the hidden form value, followed by any manual javascript for the onclick event. --%>
<g:set var="checkboxIdClean" object="${g:makeSafeJavascriptIdentifier(checkboxId)}" />
<g:set var="checkboxIdHidden" object="${checkboxIdClean}_hidden"/>

<g:set var="onclickVal">updateCheckboxHiddenInput('${checkboxIdClean}');${onclick}</g:set>

<%-- bl: copying checkbox.ftl from the simple theme so that we can have a hidden field always submit the value of the checkbox,
     as opposed to the standard behavior of forms only submitting checkboxes when the checkbox is checked. --%>

${gtu:createInput("hidden",name,value ? true : false,false,formId,null,null,null,null,null,null,null,checkboxIdHidden,null,
    null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null)}

<gfs:checkboxSimple
        name="${name}_checkbox"
        value="true"
        isChecked="${value}"
        afterLabelText="${afterLabelText}"
        cssClass="${cssClass}"
        cssStyle="${cssStyle}"
        disabled="${disabled}"
        id="${checkboxIdClean}"
        inline="${inline}"
        label="${label}"
        description="${description}"
        onblur="${onblur}"
        onchange="${onchange}"
        onclick="${onclickVal}"
        ondblclick="${ondblclick}"
        onfocus="${onfocus}"
        onkeydown="${onkeydown}"
        onkeypress="${onkeypress}"
        onkeyup="${onkeyup}"
        onmousedown="${onmousedown}"
        onmousemove="${onmousemove}"
        onmouseout="${onmouseout}"
        onmouseover="${onmouseover}"
        onmouseup="${onmouseup}"
        onselect="${onselect}"
        readonly="${readonly}"
        required="${required}"
        tabindex="${tabindex}"
        title="${title}"
        useStandardDisplay="${useStandardDisplay}"
        useTableLayout="${useTableLayout}"
        wrapperCssStyle="${wrapperCssStyle}"
        wrapperCssClass="${wrapperCssClass}"
        labelCssClass="${labelCssClass}"/>
