<%@ tag pageEncoding="UTF-8" body-content="empty" %>

<%@ attribute name="name" required="true" %>
<%@ attribute name="value" required="true" type="org.narrative.common.web.TimeInput" %>
<%@ attribute name="disabled" type="java.lang.Boolean" %>
<%-- bl: intentionally not setting the type for the Integer fields or else values that are explicitly supplied
     will be converted to 0, which is not the desired behavior for any of them. --%>
<%@ attribute name="tabindex" %>
<%@ attribute name="id" %>
<%@ attribute name="cssClass" %>
<%@ attribute name="cssStyle" %>
<%@ attribute name="onchange" description="Set the html onchange attribute on rendered html element" %>
<%@ attribute name="allowQuarterIncrements" description="Allow 15 and 45 minute values in addition to 0 and 30" type="java.lang.Boolean" %>
<%@ attribute name="limitOptionsFromValue" type="org.narrative.common.web.TimeInput" description="Whether to limit the single dropdown selector options to start at the specified time" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="gtu" uri="http://legacy.narrative.org/tags/tagUtils" %>

<g:set var="timepickerId" object="${gtu:createInputId(id,formId,name)}"/>
<g:ref var="action" className="org.narrative.network.shared.baseactions.NetworkAction" />

<g:set var="useAmPm" object="${gn:isAmPmTimePattern()}" />

<c:choose>
    <c:when test="${empty value or value.hour < 0}">
        <g:set var="hourValue" object="${0}"/>
     </c:when>
    <c:otherwise>
        <g:set var="hourValue" object="${value.resolvedHourOfDay}"/>
    </c:otherwise>
</c:choose>
<c:choose>
    <c:when test="${empty value or value.minute < 0}">
        <g:set var="minuteValue" object="${0}"/>
     </c:when>
    <c:otherwise>
        <g:set var="minuteValue" object="${value.minute}" />
    </c:otherwise>
</c:choose>
<c:if test="${useAmPm}">
    <g:set var="amPmValue" object="${(empty value or value.hour < 0) ? 0 : value.amPm}" />
</c:if>

<g:set var="textsAndValues" object="${g:newList()}" className="java.util.List" />
<g:set var="minuteValues" object="${g:newList()}" className="java.util.List" />
${g:collectionAdd(minuteValues, 0)}
<c:if test="${allowQuarterIncrements}">
    ${g:collectionAdd(minuteValues, 15)}
</c:if>
${g:collectionAdd(minuteValues, 30)}
    <c:if test="${allowQuarterIncrements}">
    ${g:collectionAdd(minuteValues, 45)}
</c:if>
<g:set var="fountLimitValue" object="${false}" className="java.lang.Boolean" />
<g:set var="selectedValue" object="${null}" className="org.narrative.common.persistence.ObjectPair" />
<g:forLoop begin="${0}" end="${23}" varStatus="hour">
    <g:set var="hourString">
        <c:choose>
            <c:when test="${useAmPm}">
                <g:set var="amPmHourIndex" object="${hour.index mod 12}" />
                ${amPmHourIndex eq 0 ? 12 : amPmHourIndex}
            </c:when>
            <c:otherwise>
                ${hour.index < 10 ? '0' : ''}${hour.index}
            </c:otherwise>
        </c:choose>
    </g:set>
    <g:set var="amPmString">
        <c:if test="${useAmPm}">
            ${gn:wordlet(hour.index < 12 ? 'tags.common.forms.simple.timepickerSimple.am' : 'tags.common.forms.simple.timepickerSimple.pm')}
        </c:if>
    </g:set>
    <g:forEach items="${minuteValues}" obj="minVal" className="java.lang.Long" varStatus="minStatus">
        <g:set var="stringText">${hourString}:${minVal==0 ? '00' : minVal}${amPmString}</g:set>
        <g:set var="stringValue">${hour.index}:${minVal}</g:set>
        <g:set var="currentValue" object="${g:newObjectPair(stringText, stringValue)}" className="org.narrative.common.persistence.ObjectPair" />

        <c:if test="${not fountLimitValue and (hour.index eq limitOptionsFromValue.resolvedHourOfDay and limitOptionsFromValue.minute eq minVal)}">
            <g:set var="fountLimitValue" object="${true}" className="java.lang.Boolean" />
        </c:if>

        <c:if test="${empty selectedValue and (hour.index eq value.resolvedHourOfDay and value.minute eq minVal)}">
            <g:set var="selectedValue" object="${currentValue}" className="org.narrative.common.persistence.ObjectPair" />
        </c:if>

        <c:if test="${not limitOptionsFromValue.valid or fountLimitValue}">
            ${g:collectionAdd(textsAndValues, currentValue)}
        </c:if>
    </g:forEach>
</g:forLoop>

<gfs:hidden id="${timepickerId}_timestamp" name="${name}.fromString" value="${value.resolvedHourOfDay}:${value.minute}" />
<span id="${timepickerId}_wrapper" class="${cssClass} yuiTimepickerButtonWrapper yui-skin-sam"${g:condAttr('style', cssStyle)}>
</span>

<script type="text/javascript">

    <gct:doOnce id="timepickerSelectFunc">
        if(typeof Narrative.timepickerButtons == 'undefined') {
            Narrative.timepickerButtons = $H();
        }

        selectTimepickerValueForMenuItem = function(timepickerId, menuItem, isEventTrigger) {
            var button = Narrative.timepickerButtons.get(timepickerId);
            if(menuItem) {
                button.set("label", menuItem.cfg.getProperty("text"));

                $(timepickerId + '_timestamp').value = menuItem.value;
            }
        };

        getTimepickerMenuItemForValue = function(timepickerId, value) {
            var button = Narrative.timepickerButtons.get(timepickerId);
            var items = button.getMenu().getItems();
            var i;
            for(i=0; i<items.length; i++) {
                var item = items[i];
                if(item.value==value) {
                    return item;
                }
            }
            return null;
        };
    </gct:doOnce>

    (function() {
        var menuData = [
        <g:forEach items="${textsAndValues}" obj="textAndValue" className="org.narrative.common.persistence.ObjectPair" varStatus="status">
            {text: "${textAndValue.one}", value: "${textAndValue.two}"},${not status.last ? ',' : ''}
        </g:forEach>
        ];

        <g:set var="currentHourValue">
            <c:choose>
                <c:when test="${useAmPm}">
                    <g:set var="amPmHourIndex" object="${hourValue mod 12}" />
                    ${amPmHourIndex eq 0 ? 12 : amPmHourIndex}
                </c:when>
                <c:otherwise>
                    ${hourValue < 10 ? '0' : ''}${hourValue}
                </c:otherwise>
            </c:choose>
        </g:set>

        var button = new YAHOO.widget.Button({
            id: "${timepickerId}",
            label: "${currentHourValue}:${minuteValue < 10 ? '0' : ''}${minuteValue}${useAmPm ? gn:wordlet(hourValue < 12 ? 'tags.common.forms.simple.timepickerSimple.am' : 'tags.common.forms.simple.timepickerSimple.pm') : null}",
            type: "menu",
            disabled: ${disabled ? true : false},
            tabindex: ${g:getJavascriptStringValue(tabindex)},
            menu: menuData,
            lazyloadmenu: false,
            container: "${timepickerId}_wrapper"
        });

        <%-- bl: don't want to focus on the first call (which just sets the default value). --%>
        var firstCall = true;
        button.on("selectedMenuItemChange", function(event) {
            var menuItem = event.newValue;
            selectTimepickerValueForMenuItem('${timepickerId}', menuItem, true);

            ${onchange}
            if(!firstCall) {
                button.focus();
            }
            firstCall = false;
        });

        button.getMenu().subscribe("render", function() {
            button.set("selectedMenuItem", getTimepickerMenuItemForValue('${timepickerId}', '${hourValue}:${minuteValue}'));
        });

        <%-- bl: tried to use this to subscribe to the show event to highlight and focus the current menu item,
             but it doesn't seem to work properly.
        button.getMenu().showEvent.subscribe(function() {
            button.getMenu().clearActiveItem();

            var menuItem = getTimepickerMenuItemForValue('${timepickerId}', $('${timepickerId}_timestamp').value);
            menuItem.cfg.setProperty("selected", true);
            menuItem.focus();
            //button.getMenu().activeItem = menuItem;
            //window.location = "#" + menuItem.id;
        });--%>

        <%-- type to complete.  doesn't 100% work as it doesn't select/highlight the item in the list properly.
             also, it only works with the first character anyway, so it's not that useful
        // from: http://ciitronian.com/blog/programming/yui-button-mimicking-native-select-dropdown-avoid-width-problem/
        //match the keypress character with the menu item text, if matched, select it
        var labelCharCode = function (type, arg, obj) {
            var char = String.fromCharCode(arg[0]);
            var items = button.getMenu().getItems();
            for(var i = 0; i < items.length; i++) {
                var item = items[i];
                var firstChar = item.cfg.getProperty('text').slice(0, 1);
                if(firstChar === char) {
                    //window.location = "#" + item.id;
                    button.getMenu().activeItem = item;
                    item.focus();
                    break;
                }
            }
        };

        //get the keyboard character codes to listen to
        var keyArr = [];
        var count = 0;
        var i;
        for(i = 65; i <= 90; i++) {
            keyArr.push(i);
        }
        for(i = 48; i <= 57; i++) {
            keyArr.push(i);
        }

        //get the keylistener working
        var kl = new YAHOO.util.KeyListener("${timepickerId}_wrapper", { keys:keyArr },
                { fn:labelCharCode,
                    scope:button,
                    correctScope:true });

        kl.enable();
        --%>

        Narrative.timepickerButtons.set('${timepickerId}', button);
    })();
</script>
