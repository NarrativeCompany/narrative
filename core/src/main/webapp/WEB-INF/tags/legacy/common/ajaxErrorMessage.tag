<%@ tag pageEncoding="UTF-8" body-content="empty" %>
<%@ attribute name="title" required="true"%>
<%@ attribute name="message" required="true"%>

<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<ajax-result>
    <ajaxErrorTitle>${fn:escapeXml(title)}</ajaxErrorTitle>
    <ajaxErrorMessage>${fn:escapeXml(message)}</ajaxErrorMessage>
</ajax-result>