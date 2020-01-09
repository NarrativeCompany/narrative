<%--
  User: jonmark
  Date: 11/1/12
  Time: 12:22 PM
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" %>

<%@ attribute name="src" required="true" type="java.lang.String" %>
<%@ attribute name="width" type="java.lang.Integer" %>
<%@ attribute name="height" type="java.lang.Integer" %>
<%@ attribute name="alt" type="java.lang.String" %>
<%@ attribute name="title" type="java.lang.String" %>
<%@ attribute name="cssClass" type="org.narrative.network.shared.email.EmailCssClass" %>
<%@ attribute name="dontResolveCss" type="java.lang.Boolean" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<e:elt name="img" src="${src}" width="${width}" height="${height}" alt="${alt}" title="${title}" cssClass="${cssClass}" dontResolveCss="${dontResolveCss}" />
