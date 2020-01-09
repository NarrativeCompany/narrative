<%@ tag pageEncoding="UTF-8" body-content="empty" %>
<%@ attribute name="currentPage" required="true" type="java.lang.Integer"%>
<%@ attribute name="itemCount" required="true" type="java.lang.Integer"%>
<%@ attribute name="itemsPerPage" required="true" type="java.lang.Integer"%>
<%@ attribute name="urlLessPageNumber" required="false" type="java.lang.String"%>
<%@ attribute name="cssStyle" required="false" type="java.lang.String"%>
<%@ attribute name="onclickJavascriptFunction" required="false" type="java.lang.String" description="A javascript function to call upon clicking a page link.  Function should take one argument that is the page number to go to." %>

<%@ attribute name="showOlderNewer" required="false" type="java.lang.Boolean" %>
<%@ attribute name="dontShowItemCounts" type="java.lang.Boolean" %>
<%@ attribute name="doLineBreakBelowItemCounts" type="java.lang.Boolean" %>
<%@ attribute name="showResultCountIfOnlyOnePage" type="java.lang.Boolean" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>

<jsp:useBean id="ph" class="org.narrative.common.util.PaginationHelper">
    <jsp:setProperty name="ph" property="currentPage" value="${currentPage}"/>
    <jsp:setProperty name="ph" property="itemCount" value="${itemCount}"/>
    <jsp:setProperty name="ph" property="itemsPerPage" value="${itemsPerPage}"/>
</jsp:useBean>

<c:choose>
    <c:when test="${ph.totalPages<=1}">
        <c:if test="${showResultCountIfOnlyOnePage}">
             ${gn:wordlet1Arg('tags.common.pagination.results', itemCount)}
        </c:if>
    </c:when>
    <c:otherwise>
        <c:if test="${showOlderNewer}">
            <div>
                <c:if test="${currentPage > 1}">
                    <c:choose>
                        <c:when test="${not empty onclickJavascriptFunction}">
                            <gct:actionButton wrapperCssClass="leftTools" onClick="${onclickJavascriptFunction}(${currentPage-1});" linkName="${gn:wordlet('pagination.newer')}" />
                        </c:when>
                        <c:otherwise>
                            <gct:actionButton wrapperCssClass="leftTools" linkUrl="${urlLessPageNumber}${currentPage-1}" linkName="${gn:wordlet('pagination.newer')}" />
                        </c:otherwise>
                    </c:choose>
                </c:if>
                <c:if test="${currentPage < ph.totalPages}">
                    <c:choose>
                        <c:when test="${not empty onclickJavascriptFunction}">
                            <gct:actionButton wrapperCssClass="rightTools" onClick="${onclickJavascriptFunction}(${currentPage+1});" linkName="${gn:wordlet('pagination.older')}" />
                        </c:when>
                        <c:otherwise>
                            <gct:actionButton wrapperCssClass="rightTools" linkUrl="${urlLessPageNumber}${currentPage+1}" linkName="${gn:wordlet('pagination.older')}" />
                        </c:otherwise>
                    </c:choose>
                </c:if>
            </div>
            <div class="clear">&#160;</div>
        </c:if>

        <div class="pagination"${g:condAttr('style', cssStyle)}>
            <c:if test="${not dontShowItemCounts}">
                ${gn:wordlet3Arg('tags.common.pagination.showingNumberResults', ph.firstItemOnPage, ph.lastItemOnPage, ph.itemCount)}
                <c:if test="${doLineBreakBelowItemCounts}"><br/></c:if>
            </c:if>
            <%--Page ${currentPage} of ${ph.totalPages}--%>&#160;
            <c:choose>
                <c:when test="${currentPage > 1}">
                    <c:choose>
                        <c:when test="${not empty onclickJavascriptFunction}">
                            <a href="${empty urlLessPageNumber ? 'javascript:void(0);' : g:concat(urlLessPageNumber,1)}" onclick="${empty urlLessPageNumber ? g:concat(onclickJavascriptFunction, '(1)') : g:concat3('hrefOnclickIfNoKeypress(event,',onclickJavascriptFunction,',1);')}">${gn:wordlet('pagination.first')}</a>
                        </c:when>
                        <c:otherwise>
                            <a href="${urlLessPageNumber}1">${gn:wordlet('pagination.first')}</a>
                        </c:otherwise>
                    </c:choose>
                </c:when>
                <c:otherwise>${gn:wordlet('pagination.first')}</c:otherwise>
            </c:choose> |


            <c:choose>
                <c:when test="${currentPage > 1}">
                    <c:choose>
                        <c:when test="${not empty onclickJavascriptFunction}">
                            <a href="${empty urlLessPageNumber ? 'javascript:void(0);' : g:concat(urlLessPageNumber,currentPage-1)}" onclick="${empty urlLessPageNumber ? g:concat4(onclickJavascriptFunction, '(', currentPage-1,');') : g:concat5('hrefOnclickIfNoKeypress(event,',onclickJavascriptFunction,',',currentPage-1,');')}"> ${gn:wordlet('pagination.previous')}</a>
                        </c:when>
                        <c:otherwise>
                            <a href="${urlLessPageNumber}${currentPage-1}">${gn:wordlet('pagination.previous')}</a>
                        </c:otherwise>
                    </c:choose>
                </c:when>
                <c:otherwise>${gn:wordlet('pagination.previous')}</c:otherwise>
            </c:choose> |



            <c:choose>
                <c:when test="${currentPage < ph.totalPages}">
                    <c:choose>
                        <c:when test="${not empty onclickJavascriptFunction}">
                            <a href="${empty urlLessPageNumber ? 'javascript:void(0);' : g:concat(urlLessPageNumber,currentPage+1)}" onclick="${empty urlLessPageNumber ? g:concat4(onclickJavascriptFunction, '(', currentPage+1,');') : g:concat5('hrefOnclickIfNoKeypress(event,',onclickJavascriptFunction,',',currentPage+1,');')}">${gn:wordlet('pagination.next')}</a>
                        </c:when>
                        <c:otherwise>
                            <a href="${urlLessPageNumber}${currentPage+1}">${gn:wordlet('pagination.next')}</a>
                        </c:otherwise>
                    </c:choose>
                </c:when>
                <c:otherwise>${gn:wordlet('pagination.next')}</c:otherwise>
            </c:choose> |

            <c:choose>
                <c:when test="${currentPage < ph.totalPages}">
                    <c:choose>
                        <c:when test="${not empty onclickJavascriptFunction}">
                            <a href="${empty urlLessPageNumber ? 'javascript:void(0);' : g:concat(urlLessPageNumber,ph.totalPages)}" onclick="${empty urlLessPageNumber ? g:concat4(onclickJavascriptFunction, '(', ph.totalPages,');') : g:concat5('hrefOnclickIfNoKeypress(event,',onclickJavascriptFunction,',',ph.totalPages,');')}">${gn:wordlet('pagination.last')}</a>
                        </c:when>
                        <c:otherwise>
                            <a href="${urlLessPageNumber}${ph.totalPages}">${gn:wordlet('pagination.last')}</a>
                        </c:otherwise>
                    </c:choose>
                </c:when>
                <c:otherwise>${gn:wordlet('pagination.last')}</c:otherwise>
            </c:choose>

        </div>
    </c:otherwise>
</c:choose>