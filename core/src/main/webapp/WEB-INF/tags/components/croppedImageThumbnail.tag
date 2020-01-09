<%--
  User: jonmark
  Date: 7/24/15
  Time: 11:00 AM
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" %>

<%@ attribute name="id" type="java.lang.String" %>
<%@ attribute name="imageUrl" required="true" type="java.lang.String" %>
<%@ attribute name="cssClasses" type="java.lang.String" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/components" %>
<%@ taglib prefix="page" tagdir="/WEB-INF/tags/common/page" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>
<%@ taglib prefix="s_page" tagdir="/WEB-INF/tags/site/page" %>

<div${ss:condAttr('id', id)} class="cropped-image-box ${cssClasses}">
    <div${ss:condAttr('id', empty id ? null : ss:concat(id, '_bgImage'))} style="background-image:url('${imageUrl}');" class="cropped-image-container">
            <div class="cropped-image-contents cropped-image-thumbnail-contents">

            </div>
    </div>
</div>
