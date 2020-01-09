<%@ tag import="org.narrative.network.shared.email.EmailCssClass" %>
<%@ tag import="org.narrative.network.shared.email.NetworkMailUtil" %>
<%@ tag import="java.util.Arrays" %>
<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ attribute name="subject" required="true" type="java.lang.String" %>

<%@ attribute name="extraHeadTag" type="java.lang.String" %>

<%@ attribute name="salutationDisplayName" type="java.lang.String" %>
<%@ attribute name="includeGenericSalutation" type="java.lang.Boolean" %>
<%@ attribute name="hideSalutation" type="java.lang.Boolean" %>
<%@ attribute name="useContextForSalutation" type="java.lang.Boolean" %>

<%@ attribute name="introduction" type="java.lang.String" %>

<%@ attribute name="title" type="java.lang.String" description="green area of email" %>

<%@ attribute name="extraDetails" type="java.lang.String" description="dark gray area of email" %>
<%@ attribute name="details" type="java.lang.String" description="dark gray area of email" %>

<%@ attribute name="information" type="java.lang.String" description="light gray area of email" %>
<%@ attribute name="informationTitle" type="java.lang.String" description="light gray area of email" %>

<%@ attribute name="extraInformation" type="java.lang.String" description="light gray area of email" %>
<%@ attribute name="extraInformationTitle" type="java.lang.String" description="light gray area of email" %>

<%@ attribute name="actionUrl" type="java.lang.String" description="dark bar for action link near bottom of email" %>
<%@ attribute name="actionText" type="java.lang.String" description="dark bar for action link near bottom of email" %>

<%@ attribute name="action2Url" type="java.lang.String" description="dark bar for second action link near bottom of email" %>
<%@ attribute name="action2Text" type="java.lang.String" description="dark bar for second action link near bottom of email" %>

<%@ attribute name="includeSignature" type="java.lang.Boolean" %>

<%@ attribute name="unsubscribeArea" type="org.narrative.network.core.area.base.Area" %>
<%@ attribute name="unsubscribeFromGlobal" type="java.lang.Boolean" %>
<%@ attribute name="dontAllowUnsubscribe" type="java.lang.Boolean" %>

<%@ attribute name="watchedChannelConsumer" type="org.narrative.network.customizations.narrative.channels.ChannelConsumer" %>

<%@ attribute name="extraFooterHtml" type="java.lang.String" %>
<%@ attribute name="cardFooterHtml" type="java.lang.String" %>
<%@ attribute name="extraUnsubscribeHtml" type="java.lang.String" %>
<%@ attribute name="suppressStandardFooter" type="java.lang.Boolean" %>
<%@ attribute name="suppressCustomFooter" type="java.lang.Boolean" %>
<%@ attribute name="bottomFooterHtml" type="java.lang.String" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="email" tagdir="/WEB-INF/tags/site/email" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>
<%@ taglib prefix="page" tagdir="/WEB-INF/tags/common/page" %>
<%@ taglib prefix="s_page" tagdir="/WEB-INF/tags/site/page" %>

<ss:ref var="task" className="org.narrative.network.shared.tasktypes.NetworkTaskImpl"/>
<ss:ref var="networkRegistry" className="org.narrative.network.core.system.NetworkRegistry" />
<ss:ref var="isJspEmailAllowsReplies" className="java.lang.Boolean" />

<ss:set var="networkContext" object="${task.networkContext}" className="org.narrative.network.shared.context.NetworkContext"/>
<ss:set var="authZone" object="${networkContext.authZone}" className="org.narrative.network.core.user.AuthZone"/>

<ss:set var="fromAuthZoneMaster" object="<%=NetworkMailUtil.getEmailFromAuthZoneMaster()%>" className="org.narrative.network.core.user.AuthZoneMaster" />

<ss:set var="includeSignature" object="${false}" className="java.lang.Boolean" />

<!--
VERSION: ${networkRegistry.version}
SERVER: ${networkRegistry.servletName}
-->
<e:elt name="html">
    <e:elt name="head">
        <e:elt name="title">${subject}</e:elt>

        ${extraHeadTag}
        <style type="text/css">
            @import url('https://fonts.googleapis.com/css?family=Lato');
        </style>
    </e:elt>
    <e:elt name="body">
        <%--
            EMAIL HEADER
        --%>
        <ss:set var="headerImageCenterCssClass" object="<%=EmailCssClass.HEADER_IMAGE_CENTER%>"
                className="org.narrative.network.shared.email.EmailCssClass"/>
        <e:elt name="div" cssClass="${headerImageCenterCssClass}">
            <e:a href="${authZone.baseUrl}"><e:img
                                    cssClass="<%=EmailCssClass.WRAPPER_IMAGE%>"
                    src="${authZone.baseUrl}/email/narrative-email-header.png"/></e:a>
                </e:elt>

        <e:elt name="table" cssClass="<%=EmailCssClass.WRAPPER_TABLE%>">
            <e:elt name="tr"><e:elt name="td">
                <c:if test="${isJspEmailAllowsReplies}">
                    <e:elt name="div" cssClass="<%=EmailCssClass.DO_NOT_WRITE_BELOW_LINE%>">== ${h:wordlet('tags.email.emailWrapper.toReplyWriteAboveThisLine')} ==</e:elt>
                </c:if>
            </e:elt></e:elt>

            <e:elt name="tr"><e:elt name="td">
                <e:elt name="div" cssClass="<%=EmailCssClass.EMAIL_WRAPPER%>">
                    <e:elt name="table" cssClass="<%=EmailCssClass.WRAPPER_TABLE%>">
                        <e:elt name="tr"><e:elt name="td">
                            <%--
                                EMAIL BODY
                            --%>
                            <e:elt name="div" cssClass="<%=EmailCssClass.EMAIL_CONTENT%>">
                                <ss:set var="salutation">
                                    <c:choose>
                                        <c:when test="${hideSalutation}" />
                                        <c:when test="${not empty salutationDisplayName}">
                                            ${h:wordlet1Arg('tags.email.emailWrapper.helloDisplayName', salutationDisplayName)}
                                        </c:when>
                                        <c:when test="${useContextForSalutation and networkContext.loggedInUser}">
                                            ${h:wordlet1Arg('tags.email.emailWrapper.helloDisplayName', networkContext.user.displayNameResolved)}
                                        </c:when>
                                        <c:when test="${includeGenericSalutation}">
                                            ${h:wordlet('tags.email.emailWrapper.hello')}
                                        </c:when>
                                    </c:choose>
                                </ss:set>
                                <c:if test="${not empty salutation}">
                                    <e:elt name="div" cssClass="<%=EmailCssClass.SALUTATION%>">
                                        ${salutation}
                                    </e:elt>
                                </c:if>

                                <c:if test="${not empty introduction}">
                                    <e:elt name="div" cssClass="<%=EmailCssClass.INTRO_PARAGRAPH%>">
                                        ${introduction}
                                    </e:elt>
                                </c:if>

                                <c:if test="${not empty title}">
                                    <e:elt name="div" cssClass="<%=EmailCssClass.TITLE_BAR%>">
                                        ${title}
                                    </e:elt>
                                </c:if>

                                <c:if test="${not empty extraDetails}">
                                    <e:elt name="div" cssClasses="<%=EmailCssClass.DETAILS_BOX.getWithDetailBoxShading()%>">
                                        ${extraDetails}
                                    </e:elt>
                                </c:if>

                                <c:if test="${not empty details}">
                                    <e:elt name="div" cssClasses="<%=EmailCssClass.DETAILS_BOX.getWithDetailBoxShading()%>">
                                        ${details}
                                    </e:elt>
                                </c:if>

                                <c:if test="${not empty information}">
                                    <c:if test="${not empty informationTitle}">
                                        <e:elt name="div" cssClass="<%=EmailCssClass.SHADED_BOX_TITLE%>">${informationTitle}</e:elt>
                                    </c:if>
                                    <e:elt name="div" cssClass="<%=EmailCssClass.SHADED_BOX%>">
                                        ${information}
                                    </e:elt>
                                </c:if>

                                <c:if test="${not empty extraInformation}">
                                    <c:if test="${not empty extraInformationTitle}">
                                        <e:elt name="div" cssClass="<%=EmailCssClass.SHADED_BOX_TITLE%>">${extraInformationTitle}</e:elt>
                                    </c:if>
                                    <e:elt name="div" cssClass="<%=EmailCssClass.SHADED_BOX%>">
                                        ${extraInformation}
                                    </e:elt>
                                </c:if>

                                <jsp:doBody />

                                <c:if test="${not empty actionUrl}">
                                    <e:elt name="div" cssClass="<%=EmailCssClass.ACTION_BOX%>">
                                        <e:a href="${actionUrl}" cssClass="<%=EmailCssClass.ACTION_BOX_LINK%>">${actionText}</e:a>
                                    </e:elt>
                                </c:if>

                                <c:if test="${not empty action2Url}">
                                    <e:elt name="div" cssClass="<%=EmailCssClass.ACTION_BOX%>">
                                        <e:a href="${action2Url}" cssClass="<%=EmailCssClass.ACTION_BOX_LINK%>">${action2Text}</e:a>
                                    </e:elt>
                                </c:if>

                                <c:if test="${includeSignature}">
                                    <ss:set var="signatureHtml">
                                        <e:elt name="div">${authZone.name}</e:elt>
                                        <e:a href="${authZone.baseUrl}">${authZone.baseUrl}</e:a>
                                    </ss:set>
                                    <c:if test="${not empty signatureHtml}">
                                        <e:elt name="div" cssClass="<%=EmailCssClass.SIGNATURE%>">
                                            ${signatureHtml}
                                        </e:elt>
                                    </c:if>
                                </c:if>

                                <c:if test="${not empty extraFooterHtml}">
                                    <e:elt name="div" cssClass="<%=EmailCssClass.FOOTER%>">
                                        <e:elt name="div" cssClass="<%=EmailCssClass.UNSUBSCRIBE%>">
                                            ${extraFooterHtml}
                                        </e:elt>
                                    </e:elt>
                                </c:if>

                                <c:if test="${not empty cardFooterHtml}">
                                    <e:elt name="div" cssClass="<%=EmailCssClass.FOOTER%>">
                                        <ss:set var="cssClasses" object="${ss:newSet()}" className="java.util.Collection" />
                                        ${ss:collectionAdd(cssClasses, EmailCssClass.UNSUBSCRIBE)}
                                        ${ss:collectionAdd(cssClasses, EmailCssClass.SMALLER_TEXT)}
                                        ${ss:collectionAdd(cssClasses, EmailCssClass.GRAY_TEXT)}

                                        <e:elt name="div" cssClasses="${cssClasses}">
                                            ${cardFooterHtml}
                                        </e:elt>
                                    </e:elt>
                                </c:if>
                            </e:elt>
                        </e:elt></e:elt>

                        <c:if test="${not empty bottomFooterHtml}">
                            <e:elt name="tr"><e:elt name="td">
                                ${bottomFooterHtml}
                            </e:elt></e:elt>
                        </c:if>
                    </e:elt>
                </e:elt>
            </e:elt></e:elt>
        </e:elt>

        <e:elt name="div" cssClass="<%=EmailCssClass.FOOTER%>">
            <e:elt name="div" cssClasses="<%=Arrays.asList(EmailCssClass.FOOTER_PLEASE_DO_NOT_REPLY, EmailCssClass.FOOTER_LINE)%>">
                <c:if test="${not isJspEmailAllowsReplies}">
                    ${h:wordlet('tags.email.emailWrapper.pleaseDoNotReply')}
                </c:if>

                <c:if test="${not empty extraUnsubscribeHtml}">
                    ${' '}
                    ${extraUnsubscribeHtml}
                </c:if>
                <c:if test="${not dontAllowUnsubscribe and ss:exists(unsubscribeArea)}">
                    ${' '}
                    <ss:set var="notificationSettingsLink">
                        <e:a href="${unsubscribeArea.manageNotificationsUrl}">${h:wordlet('tags.email.emailWrapper.notificationSettings')}</e:a>
                    </ss:set>
                    ${h:wordlet1Arg('tags.email.emailWrapper.toStopReceivingEmailNotifications', notificationSettingsLink)}
                </c:if>
                <c:if test="${watchedChannelConsumer ne null}">
                    ${' '}
                    <ss:set var="channelConsumerLink">
                        <e:a href="${watchedChannelConsumer.displayUrl}">${watchedChannelConsumer.nameForHtml}</e:a>
                    </ss:set>
                    ${h:wordlet1Arg('tags.email.emailWrapper.toUnsubscribeFromChannelConsumerNotifications', channelConsumerLink)}
                </c:if>
                <c:if test="${not dontAllowUnsubscribe and networkContext.loggedInUser}">
                    ${' '}
                    <ss:set var="unsubscribeLink">
                        <e:a href="${networkContext.user.suspendAllEmailsUrl}">${h:wordlet('tags.email.emailWrapper.here')}</e:a>
                    </ss:set>
                    ${h:wordlet1Arg('tags.email.emailWrapper.toUnsubscribeFromAllEmailNotifications', unsubscribeLink)}
                </c:if>
            </e:elt>

            <e:elt name="div" cssClass="<%=EmailCssClass.FOOTER_LINE%>">
                <e:a href="https://www.narrative.org" targetBlank="${true}" rel="noopener noreferrer">www.narrative.org</e:a>
            </e:elt>
        </e:elt>
    </e:elt>
</e:elt>

