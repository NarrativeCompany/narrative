<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ attribute name="redirect" required="true" type="java.lang.String"%>

<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<ajax-result>
    <redirect-url>${fn:escapeXml(redirect)}</redirect-url>
</ajax-result>
