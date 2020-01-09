
<%@ page contentType="text/html;charset=UTF-8" language="java" session="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="email" tagdir="/WEB-INF/tags/site/email" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<ss:ref var="task" className="org.narrative.network.core.user.services.SendUserDeletedOrActivationStatusChangeEmailTask" />
<ss:set var="wordletSuffix" object="${task.wordletSuffix}" className="java.lang.String"/>
<ss:set var="subject" object="${h:wordlet('jsp.site.email.emailCommunityMemberDeletedOrDeactivated.subject'+=wordletSuffix)}" scope="request" />

<email:emailWrapper
        subject="${subject}"
        salutationDisplayName="${not empty task.originalName ? task.originalName : task.user.displayNameResolved}"
        title="${subject}"
        includeSignature="${true}"
        information="${h:wordlet('jsp.site.email.emailCommunityMemberDeletedOrDeactivated.message'+=wordletSuffix)}"
/>