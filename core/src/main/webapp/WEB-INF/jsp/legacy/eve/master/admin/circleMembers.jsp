<%@ page import="org.narrative.network.core.cluster.actions.RemoveCircleMemberAction" %>
<%@ page import="org.narrative.network.core.cluster.actions.AddCircleMemberAction" %>
<%--
  User: jonmark
  Date: 2020-01-03
  Time: 13:26
--%>
<%@ page contentType="text/html;charset=UTF-8" session="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="ge" tagdir="/WEB-INF/tags/legacy/eve" %>
<%@ taglib prefix="gma" tagdir="/WEB-INF/tags/legacy/master/admin" %>
<%@ taglib prefix="s_page" tagdir="/WEB-INF/tags/site/page" %>

<ss:ref var="action" className="org.narrative.network.core.cluster.actions.ManageCircleMembersAction" />

<ss:set var="circleName" object="${action.circle.name}" className="java.lang.String" />

<gct:wrapperCommonParams title="${circleName} Members" />

<ge:clusterWrapper>
    <c:choose>
        <c:when test="${not empty action.circleMembers}">
            <div id="circleMembersContainer">
                <ss:forEach obj="circleMember" items="${action.circleMembers}" className="org.narrative.network.core.area.user.AreaUser">
                    <div style="margin:10px 0;">
                        <ss:set var="user" object="${circleMember.user}" className="org.narrative.network.core.user.User" />

                        <s_page:displayName role="${user}" targetBlank="${true}" />

                        <span style="margin-left: 5px;">
                            @${user.username}
                        </span>
                        <a data-user-oid="${circleMember.user.oid}" class="remove-circle-member-link" style="margin-left: 10px; font-size: 0.8em;">
                            Remove
                        </a>
                    </div>
                </ss:forEach>
            </div>

            <gct:divPopup id="removeCircleMember" title="Remove ${circleName} Member" width="600">
                <jsp:attribute name="initJavascriptBlock">
                    <script type="text/javascript">
                      removeCircleMemberPopupWindow.addButton('removeCircleMemberCancelButton', 'Cancel', function() {
                            removeCircleMemberPopupWindow.hide();
                        });
            
                        removeCircleMemberPopupWindow.addButton('removeCircleMemberUpdateButton', 'Remove', function() {
                            removeCircleMemberFormCallAjax();
                        });
                    </script>
                </jsp:attribute>
                <jsp:body>
                    Are you sure you want to remove this ${circleName} member?
                    <ss:set var="removeActionPath" object="<%=RemoveCircleMemberAction.FULL_ACTION_PATH%>" className="java.lang.String" />
                    <gfs:ajaxForm id="removeCircleMemberForm" action="${removeActionPath}!execute">
                        <gfs:hidden name="<%=RemoveCircleMemberAction.CIRCLE_PARAM%>" value="${action.circle}" />
                        <gfs:hidden id="removeUserInput" name="<%=RemoveCircleMemberAction.USER_PARAM%>" value="" />
                    </gfs:ajaxForm>
                </jsp:body>
            </gct:divPopup>
            <script type="text/javascript">
                jQuery('#circleMembersContainer').on('click.removeCircleMember', 'a.remove-circle-member-link', function(event) {
                    var $link = jQuery(event.currentTarget);

                    jQuery('#removeUserInput').val($link.data('userOid'));
                    removeCircleMemberPopupWindow.show();
                });
            </script>
        </c:when>
        <c:otherwise>
            There are currently no ${circleName} members.
        </c:otherwise>
    </c:choose>

    <h2 style="margin-top: 20px;">Add ${circleName} Member</h2>
    <ss:set var="addActionPath" object="<%=AddCircleMemberAction.FULL_ACTION_PATH%>" className="java.lang.String" />
    <gfs:ajaxForm id="addCircleMemberForm" action="${addActionPath}!execute">
        <gfs:hidden name="<%=AddCircleMemberAction.CIRCLE_PARAM%>" value="${action.circle}" />
         <gfs:text
                 label="Member Handle"
                 name="<%=AddCircleMemberAction.HANDLE_PARAM%>"
                 value=""
         />
        <gfs:submit value="Add" />
    </gfs:ajaxForm>

</ge:clusterWrapper>

