<%--
  User: jonmark
  Date: 1/22/18
  Time: 12:14 PM

  jw: there are a few places in our system where we need to generate Email HTML, and setting up these global variables can be a bit tricky. This takes the guess-work out of that.
--%>
<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ attribute name="task" type="org.narrative.network.shared.tasktypes.NetworkTaskImpl" %>
<%@ attribute name="allowReplies" type="java.lang.Boolean" %>
<%@ attribute name="forPreview" type="java.lang.Boolean" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/components" %>
<%@ taglib prefix="page" tagdir="/WEB-INF/tags/common/page" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>
<%@ taglib prefix="s_page" tagdir="/WEB-INF/tags/site/page" %>

<%--
    jw: All of these parameters are used by the emailWrapper framework to ensure that links, images and other
        ui components are rendered via the elt.tag and thus properly styled.  We are also setting explicitly
        whether the email allows replies to simulate both scenarios in the preview popups
--%>
<c:if test="${task ne null}">
    <ss:set var="task" object="${task}" scope="request" className="org.narrative.network.shared.tasktypes.NetworkTaskImpl" />
</c:if>
<ss:set var="isJspEmail" object="${true}" scope="request" className="java.lang.Boolean" />
<ss:set var="isJspEmailAllowsReplies" object="${allowReplies}" scope="request" className="java.lang.Boolean" />

<jsp:doBody />

<%--
    jw: Now that we have finished rendering this email preview lets make sure we unset them all so that rendering
        returns to normal.  Probably doesnt matter since we are done, but lets just be safe.
--%>
<ss:set var="task" object="${null}" scope="request" className="org.narrative.network.shared.tasktypes.NetworkTaskImpl" />
<ss:set var="isJspEmail" object="${false}" scope="request" className="java.lang.Boolean" />
<ss:set var="isJspEmailAllowsReplies" object="${false}" scope="request" className="java.lang.Boolean" />
