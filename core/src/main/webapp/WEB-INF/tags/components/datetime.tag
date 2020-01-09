<%--
  User: jonmark
  Date: 3/3/15
  Time: 10:53 AM

  The reason that I created this tag is to help ensure that we wrap all dates in a <time> element.
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" %>

<%@ attribute name="datetime" required="true" type="java.util.Date" %>
<%@ attribute name="id" type="java.lang.String" %>
<%@ attribute name="itemProperty" type="java.lang.String" %>
<%@ attribute name="extraCssClasses" type="java.lang.String" %>
<%@ attribute name="isTimeOnly" type="java.lang.Boolean" %>
<%@ attribute name="isDateOnly" type="java.lang.Boolean" %>
<%@ attribute name="isNoPrettyTime" type="java.lang.Boolean" %>
<%@ attribute name="isLongFormat" type="java.lang.Boolean" %>
<%@ attribute name="excludeYear" type="java.lang.Boolean" %>
<%@ attribute name="fixedTimeZone" type="java.util.TimeZone" %>
<%@ attribute name="displayTimeZone" type="java.lang.Boolean" %>
<%@ attribute name="showTimeInTitle" type="java.lang.Boolean" %>
<%@ attribute name="dontWrap" type="java.lang.Boolean" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="date" uri="http://www.narrative.org/tags/handy/date" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/components" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>

<c:if test="${datetime ne null}">

    <c:if test="${displayTimeZone==null}">
        <ss:set var="displayTimeZone" object="${false}" className="java.lang.Boolean" />
    </c:if>

    <ss:ref var="networkContext" className="org.narrative.network.shared.context.NetworkContext" />

    ${ss:assert(not excludeYear or (isLongFormat and isDateOnly), 'We currently only support excluding the year for long dates only. If you want to do this for a different format, or with time, you will need to handle that specially, or add support for it.')}
    ${ss:assert(not excludeYear or fixedTimeZone eq null, 'Should never specify a timezone when excluding the year from a long date format!')}
    ${ss:assert(not showTimeInTitle or isDateOnly, 'Should only ever use showDatetimeInTitle for the dateOnly format. It does not make sense in any other scenario!')}

    <ss:set var="datetimeText">
        <c:choose>
            <c:when test="${isTimeOnly}">
                ${date:dateFormatTimeWithTimeZone(datetime, fixedTimeZone, displayTimeZone)}
            </c:when>
            <c:when test="${isDateOnly}">
                <ss:set var="dateHtml">
                    <c:choose>
                        <c:when test="${isLongFormat and excludeYear}">
                            ${date:dateFormatLongMonthDayFormat(datetime)}
                        </c:when>
                        <c:when test="${isLongFormat}">
                            ${date:dateFormatLongDateWithTimeZone(datetime, fixedTimeZone)}
                        </c:when>
                        <c:otherwise>
                            ${date:dateFormatShortDateWithTimeZone(datetime, fixedTimeZone)}
                        </c:otherwise>
                    </c:choose>
                </ss:set>

                <c:choose>
                    <c:when test="${showTimeInTitle}">
                        <ss:set var="timeHtml">
                            <%-- jw: lets hardcode the timezone to be displayed, since this is going to be in a title. --%>
                            ${date:dateFormatTimeWithTimeZone(datetime, fixedTimeZone, true)}
                        </ss:set>
                        <span${ss:attr('title', timeHtml)}>${dateHtml}</span>
                    </c:when>
                    <c:otherwise>
                        ${dateHtml}
                    </c:otherwise>
                </c:choose>
            </c:when>
            <c:when test="${isNoPrettyTime or not date:isDateWithinPrettyTimeFrame(datetime)}">
                <%--
                    jw: we now want to display all non-pretty date times with a @ font icon between the date and time which
                        means that we need to branch this logic even further.  Emails cannot use font icons so to be safe we
                        need to determine if we are displaying this within a email, and if we are we need to just use the
                        standard method to generate the full datetime.
                --%>
                <ss:ref var="isJspEmail" className="java.lang.Boolean" />
                <c:choose>
                    <c:when test="${isJspEmail and isLongFormat}">
                        <%-- bl: always show time zones in emails --%>
                        ${date:dateFormatLongDatetimeNoPrettyTimeWithTimeZone(datetime, fixedTimeZone, true)}
                    </c:when>
                    <c:otherwise>
                        ${ss:assert(isJspEmail, 'Should only render datetimes in JSP emails now!')}
                        <%-- bl: always show time zones in emails --%>
                        ${date:dateFormatShortDatetimeNoPrettyTimeWithTimeZone(datetime, fixedTimeZone, true)}
                    </c:otherwise>
                </c:choose>
            </c:when>
            <c:otherwise>
                <c:choose>
                    <c:when test="${isLongFormat}">
                        ${date:dateFormatLongDatetimeWithTimeZone(datetime, fixedTimeZone, displayTimeZone)}
                    </c:when>
                    <c:otherwise>
                        ${date:dateFormatShortDatetimeWithTimeZone(datetime, fixedTimeZone, displayTimeZone)}
                    </c:otherwise>
                </c:choose>
            </c:otherwise>
        </c:choose>
    </ss:set>

    <%-- bl: set as variable before output to ensure no extra spaces are added to the output --%>
    <ss:set var="datetimeHtml">
        <c:choose>
            <c:when test="${dontWrap}">
                ${datetimeText}
            </c:when>
            <c:otherwise>
                <time${ss:condAttr('id', id)} datetime="${date:dateFormatIso8601UtcDatetime(datetime)}" class="datetime ${extraCssClasses}"${ss:condAttr('itemprop', itemProperty)}>${datetimeText}</time>
            </c:otherwise>
        </c:choose>
    </ss:set>

    ${datetimeHtml}

</c:if>
