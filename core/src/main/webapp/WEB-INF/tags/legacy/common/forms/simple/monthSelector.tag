<%--
  User: jonmark
  Date: 2/21/13
  Time: 5:37 PM
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" %>

<%@ attribute name="month" required="true" type="java.lang.Number" %>
<%@ attribute name="monthIndex" type="java.lang.Boolean" %>
<%@ attribute name="cssClass" type="java.lang.String" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core" %>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network"%>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="gmt" tagdir="/WEB-INF/tags/legacy/master" %>
<%@ taglib prefix="gta" tagdir="/WEB-INF/tags/legacy/area" %>

<select name="month"${g:condAttr('class', cssClass)}>
    <g:forLoop begin="0" end="11" varStatus="status">
        <g:set var="monthValue" object="${monthIndex ? status.index : status.index+1}" className="java.lang.Number" />
        <option value="${monthValue}"${g:condAttr('selected', monthValue eq month ? 'selected' : null)}>${gn:wordlet(g:concat('monthName.', status.index))}</option>
    </g:forLoop>
</select>
