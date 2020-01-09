<%@ tag pageEncoding="UTF-8" body-content="empty"%>
<%@ attribute name="type" %>
<%@ attribute name="name" %>
<%@ attribute name="value" %>
<%@ attribute name="align" %>
<%@ attribute name="src" description="If type=='image', then the src of the image for the image submit button." %>
<%@ attribute name="buttonType" type="java.lang.String" description="Set this to 'push' to have it treated as a 'regular' button that won't cause the parent form (if any) to be submitted on click." %>
<%@ attribute name="disabled" type="java.lang.Boolean" %>
<%@ attribute name="tabindex" %>
<%@ attribute name="id" %>
<%@ attribute name="label" %>
<%@ attribute name="cssClass" %>
<%@ attribute name="wrapperCssClass" %>
<%@ attribute name="wrapperCssStyle" %>
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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="gtu" uri="http://legacy.narrative.org/tags/tagUtils" %>

<g:ref var="networkContext" className="org.narrative.network.shared.context.NetworkContext" />

<g:set var="nameForId" object="${not empty name ? name : g:concat('formButtonName', g:seq())}" />

<g:set var="inputId" object="${gtu:createInputId(id,formId,nameForId)}"/>

<gfs:buttonSimple
        type="${type}"
        name="${name}"
        value="${value}"
        align="${align}"
        src="${src}"
        disabled="${disabled}"
        tabindex="${tabindex}"
        id="${id}"
        label="${label}"
        cssClass="${cssClass}"
        wrapperCssClass="${wrapperCssClass}"
        wrapperCssStyle="${wrapperCssStyle}"
        cssStyle="${cssStyle}"
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
        onchange="${onchange}"
        inputId="${inputId}"
/>

<script type="text/javascript">
    ${inputId}Button = new YAHOO.widget.Button('${inputId}', {
        <%-- bl: the default type of button is 'submit', which causes a parent form to be submitted.
             you can use 'push' as the buttonType if you don't want YUI to auto-submit the parent form. --%>
        <c:if test="${not empty buttonType}">
        'type' : '${buttonType}',
        </c:if>
        'onclick' : {
            'fn' : function(event) {
                ${onclick}
            }
        }
    });
</script>
