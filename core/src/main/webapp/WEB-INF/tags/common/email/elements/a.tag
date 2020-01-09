<%--
  User: jonmark
  Date: 11/1/12
  Time: 12:24 PM
--%>
<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ attribute name="href" required="true" type="java.lang.String" %>
<%@ attribute name="noFollow" type="java.lang.Boolean" %>
<%@ attribute name="targetBlank" type="java.lang.Boolean" %>
<%@ attribute name="rel" type="java.lang.String" %>

<%@ attribute name="title" type="java.lang.String" %>
<%@ attribute name="cssClass" type="org.narrative.network.shared.email.EmailCssClass" %>
<%@ attribute name="cssClassNames" type="java.lang.String" %>
<%@ attribute name="itemProp" type="java.lang.String" %>
<%@ attribute name="dontResolveCss" type="java.lang.Boolean" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<ss:ref var="task" className="org.narrative.network.shared.tasktypes.NetworkTaskImpl"/>

<ss:ref var="isJspEmail" className="java.lang.Boolean" />

<e:elt name="a" cssClass="${cssClass}" href="${href}" noFollow="${noFollow}" title="${title}" targetBlank="${targetBlank}" rel="${rel}" cssClassNames="${cssClassNames}" itemProp="${itemProp}" dontResolveCss="${dontResolveCss}"><jsp:doBody /></e:elt>
