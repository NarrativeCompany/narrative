<%--
  User: brian
  Date: 2019-06-14
  Time: 12:16
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" %>

<%@ attribute name="fieldName" required="true" type="java.lang.String" %>
<%@ attribute name="transactionId" required="true" type="java.lang.String" %>
<%@ attribute name="isDisabled" required="false" type="java.lang.Boolean" %>
<%@ attribute name="afterInputHtml" required="false" type="java.lang.String" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="page" tagdir="/WEB-INF/tags/common/page" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/components" %>

<ss:ref var="action" className="org.narrative.network.core.master.admin.actions.NeoTransactionsAction"/>

<div>
    <input
        type="text"
        name="${fieldName}"
        value="${transactionId}"
        size="100"
        ${ss:condAttr('disabled', isDisabled ? 'disabled' : null)}
    />
    ${afterInputHtml}
</div>