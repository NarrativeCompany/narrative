<?xml version="1.0" encoding="UTF-8"?>
<%@ page contentType="text/xml;charset=UTF-8" session="false" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<g:ref var="action" className="org.narrative.network.shared.baseactions.NetworkAction" />

<ajax-result>
    <g:forEach items="${action.actionErrors}" obj="actionError" className="java.lang.String">
        <actionError>${fn:escapeXml(actionError)}</actionError>
    </g:forEach>
    <g:forEach items="${action.fieldErrors}" obj="fieldNameToFieldErrors" className="java.util.Map.Entry">
        <g:set var="fieldName" object="${fieldNameToFieldErrors.key}" className="java.lang.String" />
        <fieldError fieldName="${fn:escapeXml(fieldName)}">
            <g:forEach items="${fieldNameToFieldErrors.value}" obj="fieldErrorMessage" className="java.lang.String">
                <message>${fn:escapeXml(fieldErrorMessage)}</message>
            </g:forEach>
        </fieldError>
    </g:forEach>
</ajax-result>
