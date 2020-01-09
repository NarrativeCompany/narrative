<%@ tag pageEncoding="UTF-8" body-content="empty" %>
<%@ attribute name="type" required="true" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="value" required="true" %>
<%@ attribute name="enableDisabledHtmlInValue" description="true if the value contains disabled html that should be enabled prior to display for edit" %>
<%-- bl: don't specify size, maxlength, or tagindex as java.lang.Integer or else they
     will be converted to 0 (since "" gets coerced to 0).  that will result in size="0"
     maxlength="0" and tabindex="0" which is not desired if nothing was specified.  in general,
     the input.tag should only be use as an "internal" tag for use by the other form tags
     (think package-level access).  --%> 
<%@ attribute name="size" %>
<%@ attribute name="maxlength" %>
<%@ attribute name="accept" description="File-specific input attribute.  HTML accept attribute to indicate accepted file mimetypes" %>
<%@ attribute name="disabled" type="java.lang.Boolean" %>
<%@ attribute name="readonly" type="java.lang.Boolean" %>
<%@ attribute name="isChecked" type="java.lang.Boolean" %>
<%@ attribute name="tabindex" %>
<%@ attribute name="id" %>
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
<%@ attribute name="onpaste" description="Set the html onpaste attribute on rendered html element" %>
<%@ attribute name="oncut" description="Set the html oncut attribute on rendered html element" %>
<%@ attribute name="oninput" description="Set the html oninput attribute on rendered html element" %>
<%@ attribute name="onselect" description="Set the html onselect attribute on rendered html element" %>
<%@ attribute name="onchange" description="Set the html onchange attribute on rendered html element" %>


<%@ taglib prefix="gtu" uri="http://legacy.narrative.org/tags/tagUtils" %>
${gtu:createInput(type,name,value,enableDisabledHtmlInValue,formId,size,maxlength,accept,disabled,readonly,isChecked,tabindex,id,cssClass,
    cssStyle,title,onclick,ondblclick,onmousedown,onmouseup,onmouseover,onmousemove,onmouseout,onfocus,onblur,onkeypress,
    onkeydown,onkeyup,onpaste,oncut,oninput,onselect,onchange,null,null)}
