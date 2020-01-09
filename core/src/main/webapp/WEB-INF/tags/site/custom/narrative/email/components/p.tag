<%--
  User: jonmark
  Date: 6/15/18
  Time: 2:27 PM
--%>
<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ attribute name="css" type="java.lang.String" %>
<%@ attribute name="fontSizeOverride" type="java.lang.String" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/components" %>
<%@ taglib prefix="page" tagdir="/WEB-INF/tags/common/page" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>
<%@ taglib prefix="s_page" tagdir="/WEB-INF/tags/site/page" %>

<p style="font-family: 'proxima-soft', Open Sans,Arial,sans-serif;color: #5d5d5d;font-size: ${empty fontSizeOverride ? '16px' : fontSizeOverride};letter-spacing: 0.37px;line-height: 30px;margin-bottom: 15px;${css}">
  <jsp:doBody />
</p>
