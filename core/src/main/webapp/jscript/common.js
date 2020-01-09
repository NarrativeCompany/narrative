/*
    This JS file is for code that is to be shared between the "core" and "mobile" requests
*/
Narrative = {};

OnloadHandler = Class.create();
OnloadHandler.prototype = {
    initialize: function(handler, order) {
        this.handler = handler;
        this.order = order;
    }
};

OnloadObj = Class.create();
OnloadObj.prototype = {
    initialize : function() {
        this.onloadHandlers = new Array();
        this.minOrder = 0;
        this.maxOrder = 0;
        this.haveOnloadHandlersStarted = false;
        this.isOnloadComplete = false;
    },
    addHandler : function(h, order) {
        order = !exists(order) ? 0 : order;
        this.minOrder = Math.min(this.minOrder, order);
        this.maxOrder = Math.max(this.maxOrder, order);
        if(!this.haveOnloadHandlersStarted) {
            if(!exists(this.onloadHandlers[order])) {
                this.onloadHandlers[order] = new Array();
            }
            this.onloadHandlers[order].push(h);
        } else {
            // already loaded, so just execute the function now.
            // todo: have this somehow support ordering?  perhaps by supplying a "process" id as an
            // argument to addOnloadHandler?  then, we can use an ajax call's process id to "group"
            // corresponding onload events.  would be needed if we ever have the need for ordered
            // onload handlers being returned by ajax (e.g. popup windows with WYSIWYG editors).
            h();
        }
    },
    handleOnLoad : function() {
        this.haveOnloadHandlersStarted = true;
        for(var i=this.minOrder; i<=this.maxOrder; i++) {
            if(exists(this.onloadHandlers[i])) {
                this.onloadHandlers[i].each(function(handler) {
                    handler();
                });
            }
        }
        this.isOnloadComplete = true;
    }
};

Narrative.onLoadHandler = new OnloadObj();
Narrative.onReadyHandler = new OnloadObj();

function addOnloadHandler(h, order) {
    Narrative.onLoadHandler.addHandler(h, order);
}

function addOnReadyHandler(h, order) {
    Narrative.onReadyHandler.addHandler(h, order);
}

Narrative.Registry = Class.create();
Narrative.Registry.prototype = {
    initialize : function(onClearPerObjectCallback) {
        this.onClearPerObjectCallback = onClearPerObjectCallback;
        this.objectTypeToObjects = $H();
    },
    registerObject : function(objectType, object) {
        var objects = this.objectTypeToObjects.get(objectType);
        if(!objects) {
            objects = this.objectTypeToObjects.set(objectType, new Array());
        }
        objects.push(object);
    },
    getObjects : function(objectType) {
        return this.objectTypeToObjects.get(objectType);
    },
    clearObjects : function(objectType) {
        var objects = this.objectTypeToObjects.get(objectType);
        if(objects) {
            if(this.onClearPerObjectCallback) {
                var thisRef = this;
                objects.each(function(object) {
                    thisRef.onClearPerObjectCallback(object, objectType);
                });
            }
            delete objects;
        }
        this.objectTypeToObjects.set(objectType, null);
    }
};

Narrative.widgetRegistry = new Narrative.Registry();


/*
  UTILITY FUNCTIONS
 */

function exists(o) {
    return (typeof o != 'undefined') && o!=null && o!=undefined;
}

function isEmpty(o) {
    return !exists(o) || o=='' || (o.length!=null && o.length==0);
}

function strToBool(str){
    return (str === 'true');
}

function executeFunctionOnceAvailable(isUnavailableFunction, handler) {
    var javascriptHandlerConditional = function() {
        if(isUnavailableFunction()) {
            setTimeout(javascriptHandlerConditional, 300);
        } else {
            handler();
        }
    };
    javascriptHandlerConditional();
}

function appendHeadElement(element) {
    document.getElementsByTagName('head').item(0).appendChild(element);
}

function includeCss(url) {
    var css = document.createElement('link');
    css.setAttribute('rel', 'stylesheet');
    css.setAttribute('type', 'text/css');
    css.setAttribute('href', url);

    appendHeadElement(css);
}

function createSpanElement(value, classname, id) {
    var ret = document.createElement('span');
    ret.innerHTML = value;
    if(classname!=null && classname!=undefined) {
        ret.className = classname;
    }
    if(id!=null && id!=undefined) {
        ret.id = id;
    }
    return ret;
}

//usage showElement('id1','id2','id3','id4','etc')
function showElement() {
  for (var i = 0; i < showElement.arguments.length; i++) {
      Element.show(showElement.arguments[i]);
  }
}

//usage hideElement('id1','id2','id3','id4','etc')
function hideElement() {
  for (var i = 0; i < hideElement.arguments.length; i++) {
      Element.hide(hideElement.arguments[i]);
  }
}

function setDisplayShowHide(controlName, isShow) {
    if (isShow) {
        showElement(controlName);
    } else {
        hideElement(controlName);
    }
}

/*
 bl:safari the following won't work in safari if the new URL matches the current aside from the anchor.
 the problem is that safari won't actually reload the page when you call document.location.reload()
 and the page has an anchor in it.  don't ask me why.  it looks like it's going to reload and the stop button
 stays visible, but the page never actually reloads.
 */
function ensurePageReload(location, url, targetBlank) {
    var currentUrl = location.href;
    var currentUrlWithoutAnchor = currentUrl.replace(/#.*/gi, '');
    var targetUrlWithoutAnchor = url.replace(/#.*/gi, '');
    if(currentUrlWithoutAnchor==targetUrlWithoutAnchor) {
        // jw: we were doing a location.reload() here, but this was the equivalent of an F5, which causes tons of extra
        //     page requests. instead we will add/remove a rl=true (reload) parameter to the end of the url to ensure the
        //     url changes and that the browser will reload the page when we set the location.href
        var suffixPos = url.indexOf("#");
        var baseUrl;
        var urlSuffix;
        // jw: first, lets split the base URL from the hash
        if (suffixPos == -1) {
            baseUrl = url;
        } else {
            baseUrl = url.substr(0, suffixPos);
            urlSuffix = url.substr(suffixPos);
        }

        url = baseUrl;

        // jw: If the rl=true parameter does not exist add it
        if (url.indexOf("?rl=true")==-1 && url.indexOf("&rl=true")==-1) {
            url += url.indexOf('?')>-1?"&":"?";
            url += "rl=true";

        // if it does exist, lets make sure we remove it.
        } else {
            // if the rl=true parameter is first and there are other parameters lets make sure that we do not accidentally
            // break the URL by removing the question mark entirely.  Because of that this order is extremely important!
            url = url.replace('?rl=true&','?');
            url = url.replace('?rl=true','');
            url = url.replace('&rl=true','');
        }

        if(urlSuffix) {
            url += urlSuffix;
        }
    }

    if (targetBlank) {
        window.open(url, '_blank');
    } else {
        location.href = url;
    }
}

function setTrueFalseValue(element, isOn) {
  if(isOn) {
    element.value = 'true';
  } else {
    element.value = 'false';
  }
}

function setSimpleCheckboxValue(checkboxId, isChecked) {
    $(checkboxId).checked = isChecked;
    updateCheckboxLabel(checkboxId);
}

function setCheckboxValue(checkboxId, isChecked) {
    setSimpleCheckboxValue(checkboxId, isChecked);
    updateCheckboxHiddenInput(checkboxId);
}

function updateCheckboxHiddenInput(checkboxId) {
    var checkbox = $(checkboxId);
    var isChecked = checkbox.checked;
    setTrueFalseValue($(checkbox.id + '_hidden'), isChecked);
}

function setLabelIsChecked(id, isOn) {
    if(isOn) {
      document.getElementById(id).className = "wwctrl_on";
    }
    else {
      document.getElementById(id).className = "wwctrl";
    }
}

function updateCheckboxLabel(checkboxId) {
    var checkbox = $(checkboxId);
    var isChecked = checkbox.checked;
    setLabelIsChecked('wwctrl_' + checkbox.id, isChecked);
}

function getXmlDataFromTagName(xml, tagName) {
    if(!xml) {
        return null;
    }
    var elts = xml.getElementsByTagName(tagName);
    if(elts!=null && elts.length>0) {
        return getElementNodeValue(elts[0]);
    }
    return null;
}

function getElementNodeValue(elt) {
    if(elt.childNodes.length==0) {
        return "";
    }
    var childNode = elt.firstChild;
    // text node
    if(childNode.nodeType==3) {
        return elt.firstChild.data;
    }
    // CDATA section
    if(childNode.nodeType==4) {
        return elt.firstChild.nodeValue;
    }
    return null;
}

function processElementList(list, handler) {
    for(var i = 0; i<list.length; i++) {
        var elt = list.item(i);
        if(elt.nodeType==1) {
            handler(elt);
        }
    }
}

function getAllChildrenTextNodesAsString(elt) {
    var ret = '';
    if(elt) {
        for(var i=0; i<elt.childNodes.length; i++) {
            var childNode = elt.childNodes[i];
            if(childNode.nodeType==3) {
                ret += childNode.nodeValue;
            }
        }
    }
    return ret;
}

function setupPlaceholder(inputId) {
    var placeholder = $(inputId+'Placeholder');
    var input = $(inputId);

    if (!placeholder || !input) {
        return;
    }

    input.observe('focus', function () {
        isEmpty(input.value) ? placeholder.show() : placeholder.hide();

        placeholder.addClassName('fade');
    });

    input.observe('blur', function () {
        isEmpty(input.value) ? placeholder.show() : placeholder.hide();

        placeholder.removeClassName('fade');
    });

    input.observe('keyup', function() {
        isEmpty(input.value) ? placeholder.show() : placeholder.hide();
    });

    // While its true that the input should be autocomplete="off" lets just make sure its empty before showing the placeholder
    if (isEmpty(input.value)) {
        placeholder.show();
    }
}

(function() {
    Narrative.templateEval = function(template, data) {
        template = $(template);
        template = template.value || template.innerHTML;
        return template.replace(/\{([\w\.]*)\}/g, function (str, key) {
            var keys = key.split(".");
            var value = data[keys.shift()];
            keys.each(function (key) {
                value = value[key];
            });
            return (value === null || value === undefined) ? "" : value;
        });
    };
    // bl: this is based loosely off of the jQuery plugin nano: https://github.com/trix/nano
    Narrative.templateReplace = function(template, target, data) {
        var evaluatedHtml = Narrative.templateEval(template, data);
        Element.update(target, evaluatedHtml);
    };

    Narrative.templateAppend = function(template, target, data) {
        Element.insert(target, Narrative.templateEval(template, data));
    };

    Narrative.templatePrepend = function(template, target, data) {
        Element.insert(target, {
            top: Narrative.templateEval(template, data)
        });
    };
})();

// jw: derived from http://www.w3schools.com/js/js_cookies.asp
Narrative.Cookies = (function() {
    var cookies = null;
    return {
        getCookie: function(name) {
            return this.getCookies().get(name);
        },
        setCookie: function(name, value, expirationDays, path) {
            var cookieValue = encodeURIComponent(value);
            if (expirationDays) {
                var expirationDate=new Date();
                expirationDate.setDate(expirationDate.getDate() + expirationDays);
                cookieValue += "; expires=" + expirationDate.toUTCString();
            }
            cookieValue += "; path=" + (path || '/');
            document.cookie=name + "=" + cookieValue;

            var cookies = this.getCookies();
            cookies.set(name, value);
        },
        deleteCookie: function(name, path) {
            var cookies = this.getCookies();
            if (!isEmpty(cookies.get(name))) {
                var cookieValue = name + "=; expires=Thu, 01-Jan-1970 00:00:01 GMT";
                cookieValue += "; path=" + (path || '/');
                document.cookie=cookieValue;

                cookies.unset(name);
            }
        },
        getCookies: function() {
            if (cookies==null) {
                cookies = $H();

                if (!isEmpty(document.cookie)) {
                    var docCookies=document.cookie.split(";");
                    docCookies.each(function(cookie) {
                        var splitIndex = cookie.indexOf("=");
                        var cookieName = cookie.substr(0, splitIndex);
                        var cookieValue = cookie.substr(splitIndex+1);
                        cookieName = cookieName.replace(/^\s+|\s+$/g,"");

                        cookies.set(cookieName, cookieValue);
                    });
                }
            }
            return cookies;
        }
    }
}());

// jw: lets add some utility methods to ease the use of localStorage
//     http://diveintohtml5.info/storage.html
Narrative.LocalStorage = (function() {
    return {
        isLocalStorageSupported : function() {
            try {
              return 'localStorage' in window && window['localStorage'] !== null;
            } catch (e) {
              return false;
            }
        },
        store : function(key, data) {
            if (!this.isLocalStorageSupported()) {
                return;
            }

            try {
                return localStorage.setItem(key, data);
            } catch(e) {
                // ignore. apparently Mobile Safari in private browsing mode doesn't allow you to store anything in local storage.
            }
        },
        remove : function(key) {
            if (!this.isLocalStorageSupported()) {
                return;
            }

            localStorage.removeItem(key);
        },
        fetch : function(key) {
            if (!this.isLocalStorageSupported()) {
                return null;
            }

            return localStorage.getItem(key);
        }
    }
}());


// from: http://eriwen.com/javascript/js-stack-trace/
// and: http://pastie.org/253058
(function () {

    var getStack = (function (e) {

        if(!e) {
            try {(0)()} catch (e2) {
                e = e2;
            }
        }
        var mode = e.stack ? 'Firefox' : window.opera ? 'Opera' : 'Other';

        switch (mode) {
            case 'Firefox' : return function () {
                return e.stack.replace(/^.*?\n/,'').
                               replace(/(?:\n@:0)?\s+$/m,'').
                               replace(/^\(/gm,'{anonymous}(').
                               split("\n");
            };

            case 'Opera' : return function () {
                var lines = e.message.split("\n"),
                    ANON = '{anonymous}',
                    lineRE = /Line\s+(\d+).*?in\s+(http\S+)(?:.*?in\s+function\s+(\S+))?/i,
                    i,j,len;

                for (i=4,j=0,len=lines.length; i<len; i+=2) {
                    if (lineRE.test(lines[i])) {
                        lines[j++] = (RegExp.$3 ?
                            RegExp.$3 + '()@' + RegExp.$2 + RegExp.$1 :
                            ANON + RegExp.$2 + ':' + RegExp.$1) +
                            ' -- ' + lines[i+1].replace(/^\s+/,'');
                    }
                }

                lines.splice(j,lines.length-j);
                return lines;
            };

            default : return function () {
                var curr  = arguments.callee.caller,
                    FUNC  = 'function', ANON = "{anonymous}",
                    fnRE  = /function\s*([\w\-$]+)?\s*\(/i,
                    stack = [],j=0,
                    fn,args,i;

                while (curr) {
                    fn    = fnRE.test(curr.toString()) ? RegExp.$1 || ANON : ANON;
                    args  = stack.slice.call(curr.arguments);
                    i     = args.length;

                    while (i--) {
                        switch (typeof args[i]) {
                            case 'string'  : args[i] = '"'+args[i].replace(/"/g,'\\"')+'"'; break;
                            case 'function': args[i] = FUNC; break;
                        }
                    }

                    stack[j++] = fn + '(' + args.join() + ')';
                    curr = curr.caller;
                }

                return stack;
            };
        }

    });

    Narrative.getStackTrace = function(e) {
        return getStack(e)().join('<br/>');
    };

    Narrative.getStackTracePlainText = function(e) {
        return getStack(e)().join('\n');
    };
})();


function debugHashProperties(obj, asHtml, includeFunctions) {
    var parts = [];
    parts.add = function(key, value) {
        if(typeof value=='function' && !includeFunctions) {
            return;
        }
        if(asHtml) {
            if(value) {
              value = value.toString().replace(/</ig, '&lt;');
            }
            this.push('<b>' + key + '</b>: ' + value);
        } else {
            this.push(key + ': ' + value);
        }
    };

    obj._each(function(pair) {
      if (!pair.key) return;
      var value = pair.value;

      if (value && typeof value == 'object') {
        if (value.constructor == Array) value.each(function(value) {
          parts.add(pair.key, value);
        });
        return;
      }
      parts.add(pair.key, value);
    });

    return parts.join(asHtml ? '<br />' : '\n');
}

/*
    bl: document.getElementsByClassName is implemented now in Firefox 3 and Safari 3.1 natively.  thus, we can
    no longer rely on using the Prototype method for getting elements by class name.  to fix, I'm creating
    this helper method to getElementsByClassName.

    refer: http://ejohn.org/blog/getelementsbyclassname-pre-prototype-16/
*/
Narrative.getElementsByClassName = function(className, parentEl) {
    if(parentEl) {
        return $(parentEl).select('.' + className);
    }
    return $$('.' + className);
};

var blockedNamedFunctions = new Array();
function blockNamedFunction(name) {
    if (blockedNamedFunctions.indexOf() < 0) {
        blockedNamedFunctions.push(name);
    }
}

function runPotentiallyBlockedFunction(name, blockedFunction) {
    if (blockedNamedFunctions.indexOf(name) > -1) {
        blockedNamedFunctions = blockedNamedFunctions.without(name);
    } else {
        blockedFunction();
    }
}

/**
 * similar to Element.update() in prototype, only instead of completely
 * replacing the contents of the target, the text will be appended
 * to the target.  javascript evaluation is supported as part of this
 * operation.
 * @param target the target to append the text to
 * @param text the text to append (will be converted to dom elements and appended individually)
 */
function appendTextAsNodeChildren(target, text, insertBeforeNode) {
    var tmpDiv = document.createElement('div');
    tmpDiv.innerHTML = text.stripScripts();
    target = $(target);
    while(tmpDiv.childNodes.length>0) {
        var newNode = tmpDiv.removeChild(tmpDiv.childNodes[0]);
        if(insertBeforeNode) {
            target.insertBefore(newNode, insertBeforeNode);
        } else {
            target.appendChild(newNode);
        }
    }
    setTimeout(function() {text.evalScripts()}, 10);
}

Narrative.calDatepickers = $H();

function registerDatepicker(id, defaultDateWording, onChange, anchorCalendarCorner, anchorTargetCorner) {
    YAHOO.util.Event.onDOMReady(function() {

        var Event = YAHOO.util.Event,
            Dom = YAHOO.util.Dom,
            dialog,
            calendar;

        var datepicker = Dom.get(id);

        Event.on(datepicker, "focusout", function() {
            if(isEmpty(datepicker.value)) {
                datepicker.value = defaultDateWording;
            }
        });

        Event.on(datepicker, "focusin", function() {
            if(datepicker.value == defaultDateWording) {
                datepicker.value = '';
            }
            // Lazy Dialog Creation - Wait to create the Dialog, and setup document click listeners, until the first time the button is clicked.
            if (!dialog) {

                // hide the Calendar whenever the field is tabbed out of
                Event.on(datepicker, "keydown", function(e) {
                    if(e.keyCode==Event.KEY_TAB) {
                        dialog.hide();
                    }
                });
                // Hide Calendar if we click anywhere in the document other than the calendar
                Event.on(document, "click", function(e) {
                    var el = Event.getTarget(e);
                    if(dialog) {
                        var dialogEl = dialog.element;
                        if (el != dialogEl && !Dom.isAncestor(dialogEl, el) && el != datepicker && !Dom.isAncestor(datepicker, el)) {
                            dialog.hide();
                        }
                    }
                });

                dialog = new YAHOO.widget.Dialog("container" + id, {
                    visible:false,
                    context:[id, anchorCalendarCorner || "tl", anchorTargetCorner || "bl"],
                    draggable:false,
                    close:false
                });
                dialog.setBody('<div id="cal' + id + '"></div>');
                dialog.render('dialog' + id);
            }

            // Lazy Calendar Creation - Wait to create the Calendar until the first time the button is clicked.
            if (!calendar) {

                calendar = new YAHOO.widget.Calendar("cal" + id, {
                    iframe:false,          // Turn iframe off, since container has iframe support.
                    hide_blank_weeks:true
                });

                Narrative.calDatepickers.set(id, calendar);

                var selectedDate = getDateFromString($(id).value);
                if(selectedDate) {
                    calendar.select(selectedDate);
                }
                calendar.render();

                calendar.selectEvent.subscribe(function() {
                    if (calendar.getSelectedDates().length > 0) {
                        var selDate = calendar.getSelectedDates()[0];

                        datepicker.value = formatDateAsString(selDate);
                    } else {
                        datepicker.value = defaultDateWording;
                    }
                    if(onChange) {
                        onChange();
                    }
                    dialog.hide();
                });
                // some alignments require the width of the dialog to be set, which means that we need to wait until the
                // calendar was rendered into the dialog.  Now that it is, lets re-align the dialog.
                dialog.align();
            }

            var seldate = calendar.getSelectedDates();

            if (seldate.length > 0) {
                // Set the pagedate to show the selected date if it exists
                calendar.cfg.setProperty("pagedate", seldate[0]);
                calendar.render();
            }

            dialog.show();
        });
    });
}

function isReturnKeyCode(event) {
    return event.keyCode==Event.KEY_RETURN || event.keyCode==10;
}

function getSelectedOptionValue(selectEl) {
    selectEl = $(selectEl);
    return selectEl.options[selectEl.selectedIndex].value;
}

function getCheckedRadioButtonValue(form, radioButtonFieldName) {
    var radioButtonElt = form[radioButtonFieldName];
    for(var i=0; i<radioButtonElt.length; i++) {
        if(radioButtonElt[i].checked) {
            return radioButtonElt[i].value;
        }
    }
    return null;
}

function updateCalendarDate(id, date) {
    var cal = Narrative.calDatepickers.get(id);
    if(cal) {
        if(date) {
            cal.select(date);
        } else {
            cal.deselectAll();
        }
        cal.render();
    } else {
        $(id).value = formatDateAsString(date);
    }
}

function formatDateAsString(date) {
    return (date.getMonth()+1) + '/' + date.getDate() + '/' + date.getFullYear();
}

function getDateFromString(str) {
    var millis = Date.parse(str);
    if(isNaN(millis)) {
        return null;
    }
    return new Date(millis);
}

function trim(str) {
    if (str == null) {
        return str;
    }

     str = str.replace( /^\s+/g, "" );// strip leading
     return str.replace( /\s+$/g, "" );// strip trailing
}

function escapeQuotes(str) {
    return str.replace(/"/g, "&quot;");
}

function getIframeParent() {
    try {
        if(exists(window.parent) && window.parent!=window) {
            // if we can't access the parent document, then it must be on a different domain, so we shouldn't use it.
            if(exists(window.parent.document)) {
                return window.parent;
            }
        }
    } catch(e) {
        // ignore the error and return the window
    }
    return window;
}

dateFormaters = $H();
dateFormaters.set('yyyy', function(date) {
    var year = date.getYear();
    if (year<1000) {
        year = year+1900;
    }
    return ""+year;
});
dateFormaters.set('yy', function(date) {
    return dateFormaters.get('yyyy')(date).substring(2,4);
});
dateFormaters.set('MM', function(date) {
    var month = date.getMonth()+1;
    return (month < 10 ? '0' : '') + month;
});
dateFormaters.set('M', function(date) {
    return ""+(date.getMonth()+1);
});

dateFormaters.set('dd', function(date) {
    var day = date.getDate();
    return (day < 10 ? '0' : '')+day;
});
dateFormaters.set('d', function(date) {
    return ""+date.getDate();
});
dateFormaters.set('hh', function(date) {
    var hour = dateFormaters.get('h')(date);
    if(hour.length==1) {
        return '0' + hour;
    }
    return hour;
});
dateFormaters.set('h', function(date) {
    var hours = date.getHours();
    if(hours==0) {
        hours = 12;
    } else if (hours > 12) {
        hours -= 12;
    }
    return ""+hours;
});
dateFormaters.set('HH', function(date) {
    var hours = date.getHours();
    return (hours < 10 ? '0' : '')+hours;
});
dateFormaters.set('H', function(date) {
    return ""+date.getHours();
});

dateFormaters.set('mm', function(date) {
    var minutes = date.getMinutes();
    return (minutes < 10 ? '0' : '')+minutes;
});
dateFormaters.set('m', function(date) {
    return ""+date.getMinutes();
});

dateFormaters.set('ss', function(date) {
    var seconds = date.getSeconds();
    return (seconds < 10 ? '0' : '')+seconds;
});
dateFormaters.set('s', function(date) {
    return ""+date.getSeconds();
});

dateFormaters.set('a', function(date) {
    return date.getHours() > 11 ? 'PM' : 'AM';
});

dateFormaters.set('G', function(date) {
    return 'AD';
});

ignoredDateFormatCharacters = new Array('\'');
function formatDate(date,format) {
   // adapted from excellent routines by Matt Kruse http://www.mattkruse.com
    format = format+"";
    var result = "";
    var i_format = 0;
    var c = "";
    var token = "";

    while (i_format < format.length) {
        // Get next token from format string
        c = format.charAt(i_format);
        token = "";
        while ((format.charAt(i_format) == c) && (i_format < format.length)) {
            if (ignoredDateFormatCharacters.indexOf(c)<0) {
                token += format.charAt(i_format);
            }
            i_format++;
        }
        var formatter = dateFormaters.get(token);
        if (formatter) {
            result = result + formatter(date);
        } else {
            result = result + token;
        }
    }
    return result;
}// end fn


TimeTicker = Class.create();
TimeTicker.prototype = {
    initialize : function(targetId, format, browserOffset, timezoneOffsetInMinutes) {
        this.targetId = targetId;
        this.format = format;
        this.active = false;
        if (browserOffset) {
            this.millisecondShift = (timezoneOffsetInMinutes - browserOffset) * 60 * 1000;
        } else {
            this.millisecondShift = 0;
        }
    },
    getDate : function() {
        var date = new Date();
        date.setTime(date.getTime()+this.millisecondShift);
        return date;
    },
    start : function() {
        this.active = true;
        this.tick();
    },
    stop : function() {
        this.active = false;
    },
    tick : function() {
        if (this.active) {
            var timeTicker = this;
            var date = this.getDate();
            Element.update(this.targetId, formatDate(date, this.format));

            setTimeout(function() {timeTicker.tick();}, 1000)
        }
    }
};

Narrative.getUrlParameter = function(url, parameter) {
    return (url.indexOf('?') > -1 ? '&' : '?')+parameter;
};

Narrative.getUrl = function(urlBase, parameters) {
    var url = urlBase;
    if (parameters!=null) {
        parameters.each(function(pair) {
            url += Narrative.getUrlParameter(url, pair.key+"="+encodeURIComponent(pair.value));
        });
    }
    return url;
};

function cloneInputElementById(elementIdToClone, id, newInputTypeToUse) {
    return cloneInputElement($(elementIdToClone), id, newInputTypeToUse);
}

function cloneInputElement(eltToClone, id, newInputTypeToUse) {
    return createInputElement(!isEmpty(newInputTypeToUse) ? newInputTypeToUse : eltToClone.type, eltToClone.name, eltToClone.value, id);
}

function createHrefElement(href, text, id) {
    var ret = document.createElement('a');
    ret.href = href;
    ret.innerHTML = text;
    if(!isEmpty(id)) {
        ret.id = id;
    }
    return ret;
}


function createInputElement(type, name, value, id, style) {
    var ret = document.createElement('input');
    ret.setAttribute("type", type);
    if(!isEmpty(style)) {
        ret.setAttribute("style", style);
    }
    ret.name = name;
    if(type!='file') {
        ret.value = value;
    }
    if(id!=null && id!=undefined) {
        ret.id = id;
    }
    return ret;
}


function urlEncodePathElement(str) {
    return escape(str).replace(/\//g, "%2F");
}

function getQueryParamArgsFromUrl() {
    var ret = $H();
    var query = window.location.search.substring(1);
    var parms = query.split('&');
    for (var i=0; i<parms.length; i++) {
        var pos = parms[i].indexOf('=');
        if (pos > 0) {
            var key = parms[i].substring(0,pos);
            var val = parms[i].substring(pos+1);
            ret.set(key, val);
        }
    }
    return ret;
}

function cancelEventPropagation(e) {
    // IE does not pass a event object into events, instead you need to get it from window.event.
    var event = e || window.event;
    if (event) {
        if (event.stopPropagation) {
            event.stopPropagation();
        }
        event.cancelBubble = true;
    }
    return false;
}

// jw: adding these utility methods from Foundation utilities to ease some new cluster tools
if (jQuery) {
    (function($){
        $.fn.ss_showHide = function(show) {
            if (show) {
                return this.ss_show();
            }
            return this.ss_hide();
        };

        $.fn.ss_show = function() {
            return this.removeClass('hide');
        };

        $.fn.ss_hide = function() {
            return this.addClass('hide');
        };
    })(jQuery);
}