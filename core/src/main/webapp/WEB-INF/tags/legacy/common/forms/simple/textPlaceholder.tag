<%@ tag pageEncoding="UTF-8" %>

<%@ attribute name="inputId" required="true" type="java.lang.String" %>
<%@ attribute name="placeholder" type="java.lang.String" %>


<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="gtu" uri="http://legacy.narrative.org/tags/tagUtils" %>


<g:set var="inputHtml">
    <jsp:doBody />
</g:set>

<c:choose>
    <c:when test="${not empty placeholder}">
        <span class="fieldWithPlaceholder">
            <label id="${inputId}Placeholder" for="${inputId}" class="placeholder" style="display: none;">
                <span>${placeholder}</span>
            </label>
            ${inputHtml}
        </span>
        <script type="text/javascript">
            addOnloadHandler(function() {
                <%-- bl: add a 100ms delay so that if the browser pre-fills the fields, then we won't show the placeholder. --%>
                setTimeout(function() {
                    setupPlaceholder('${inputId}');
                }, 100);
            });
        </script>
    </c:when>
    <c:otherwise>
        ${inputHtml}
    </c:otherwise>
</c:choose>

