function muteSounds(source) {
    /*try {
        if (source != "header") {
            headerFlashObject.audioPlaying(false);
        }
   } catch (e)  {}

    try {
        if (source != "audioSidebar") {
            audioSidebar.stopAll();
        }
   } catch (e)  {}

    try {
        if (source != "videoSidebar") {
            videoSidebar.stopAll();
        }
   } catch (e)  {}*/

    /* todo: figure out if we want to mute/pause video players, too.  commenting out to prevent javascript error.
    try {
        if (source != "videoPlayer") {
            videoPlayer.pauseVideo();
        }
   } catch (e)  {}*/

    for (var i = 0; i < soundObjects.length; i++) {
        try {
            var soundObject = $(soundObjects[i]);
            if(soundObject && soundObject.tagName.toLowerCase() != 'div') {
                soundObject.stopAll();
            }
        } catch (e)  {}
    }
}

var soundObjects = new Array();
function addSoundObject(obj) {
    soundObjects.push(obj);
}

function selectOption(selectEl, value) {
    selectEl = $(selectEl);
    for(var i=0; i<selectEl.options.length; i++) {
        // bl: without the toString() calls here, this won't work when comparing string "false" to boolean false.
        if(selectEl.options[i].value.toString()==value.toString()) {
            selectEl.selectedIndex = i;
            return;
        }
    }
}

function getFlashMovieObject(movieName)
{
  if (window.document[movieName])
  {
      return window.document[movieName];
  }
  if (navigator.appName.indexOf("Microsoft Internet")==-1)
  {
    if (document.embeds && document.embeds[movieName])
      return document.embeds[movieName];
  }
  else // if (navigator.appName.indexOf("Microsoft Internet")!=-1)
  {
    return document.getElementById(movieName);
  }
  return null;
}

//usage toggleView('id1','id2','id3','id4','etc')
function toggleView() {
  var element;
  for (var i = 0; i < toggleView.arguments.length; i++) {
    element = document.getElementById(toggleView.arguments[i]);
    if (element == null) {
      continue
    }
    element.style.display == '' ?  element.style.display = 'none' : element.style.display = '';
  }
}

function setVisibility(objectId, isVisible) {
    // get a reference to the cross-browser style object and make sure the object exists
    var styleObject = $S(objectId);
    return setVisibilityOnStyle(styleObject, isVisible);
}

function setVisibilityOnStyle(style, isVisible) {
    if(!style) {
        return false;
    }
    if(isVisible) {
        style.visibility = 'visible';
    } else {
        style.visibility = 'hidden';
    }
    return true;
}

function $S(v) {
    if($(v))
        return($(v).style);
    return null;
}

function focusFormField(id) {
    if(typeof focusWysiwygEditor == 'undefined') {
        $(id).focus();
    } else {
        focusWysiwygEditor(id);
    }
}

function moveDivToLocation(divToMove,moveToId,top,left) {
    var styleObject = $S(divToMove);
    if(styleObject) {
        /*var position = Position.cumulativeOffset($(moveToId));
        styleObject.left = (position[0] + left) + 'px';
        styleObject.top = (position[1] + top) + 'px';*/
        var xy = YAHOO.util.Dom.getXY(YAHOO.util.Dom.get(moveToId));
        //alert('ext: ' + xy[0] + 'x' + xy[1] + ' for ' + moveToId);
        styleObject.left = (xy[0] + (left ? left : 0)) + 'px';
        styleObject.top = (xy[1] + (top ? top : 0)) + 'px';
        //alert(styleObject.left + 'x' + styleObject.top);
    }
}

function getMaxZIndexForPopupWindows() {
    //return Ext.WindowMgr.zseed + (Ext.WindowMgr.accessList.length-1)*10;
    // bl: just pick an arbitrary max number of windows.  hopefully there will never be more than 20 windows on the page at a time ;)
    return Narrative.defaultPopupZIndex + 20*10;
}

function createSortable(divId, options) {
    // jw: just lost a good chunk of time trying to figure out why parseSortable wasnt giving me ids from a new sortable
    //     list I was creating. When creating sortables the ids for parseSortableList will be derived from the root
    //     elements of the sortable container with a id like: {x}_{id}.  The {x} is ignored and you get back the {id} part.
    // bl: script.aculo.us 1.7.0 uses the following pattern.  it won't work for us because it doesn't
    // allow us to have embedded underscores in our IDs (which we use for sorting sidebar items).
    // changing so that we'll use the same pattern that script.aculo.us used in 1.6.1
    // /^[^_\-](?:[A-Za-z0-9\-\_]*)[_](.*)$/
    options = Object.extend({format : /^[^_]*_(.*)$/}, options || {});
    Sortable.create(divId, options);
}

function parseSortableList(sortableId) {
    var serialized = Sortable.serialize(sortableId);
    if(serialized==null || serialized==undefined || serialized=='') {
        return null;
    }
    var objs = serialized.split('&');
    var ret = new Array(objs.length);
    for(var i=0; i<objs.length;i++) {
        var obj = objs[i];
        var nameAndValue = obj.split('=');
        ret[i] = nameAndValue[1];
    }
    return ret;
}

function removeAllChildren(element) {
    //element.innerHTML = '';
    var children = element.childNodes;
    while(true) {
        if(children==null || children==undefined || children.length==0) {
            break;
        }
        element.removeChild(children[0]);
        children = element.childNodes;
    }
}

ImageDimensions = Class.create();

ImageDimensions.prototype = {
    initialize : function(width, height) {
        this.width = width;
        this.height = height;
    },
    width  : 0,
    height : 0
};

function calcImageDimensions(originalWidth, originalHeight, maxWidth, maxHeight) {
    var scaleFactor = 1;
    // be sure the scale factor is always 1 or less.  this will prevent us
    // from upsizing an image that already fits within the proper dimensions.
    if (maxWidth>0 && originalWidth > maxWidth) {
        scaleFactor = maxWidth / originalWidth;
    }
    if (maxHeight>0 && originalHeight*scaleFactor > maxHeight) {
        scaleFactor = maxHeight / originalHeight;
    }

    if (scaleFactor>=1)
        return new ImageDimensions(originalWidth, originalHeight);
    return new ImageDimensions(Math.max(Math.round(scaleFactor*originalWidth),1), Math.max(Math.round(scaleFactor*originalHeight),1));
}

function checkAll(elementNameToCheck, isChecked) {
    var elements = document.getElementsByName(elementNameToCheck);
    if(elements!=null) {
        for(var i=0; i<elements.length; i++) {
            elements[i].checked = isChecked;
        }
    }
}

function getStringAfterReplacingNewlinesWithBRsAndConsecutiveSpacesWithNbsp(body) {
    if(isEmpty(body)) {
        return body;
    }
    body = body.replace(/\r\n/gi, '\n');
    body = body.replace(/\r/gi, '\n');
    body = body.replace(/\n/gi, '<br />');
    var spaceRegEx = / {2,}/gi;
    var currIndex = 0;
    var newBody = '';
    do {
        var regExArray = spaceRegEx.exec(body);
        if(regExArray!=null) {
            var index = regExArray.index;
            if(index>currIndex) {
                newBody += body.substr(currIndex, index-currIndex);
            }
            var spaces = regExArray[0];
            newBody += ' ';
            for(var i=1; i<spaces.length; i++) {
                newBody += '&nbsp;';
            }
            currIndex = spaceRegEx.lastIndex;
        } else {
            newBody += body.substr(currIndex, body.length-currIndex);
            break;
        }
    } while(true);
    return newBody;
}


function setAnchorOnCurrentRequest(location, anchor) {
    var currentUrl = location.href;
    var currentUrlWithoutAnchor = currentUrl.replace(/#.*/gi, '');

    location.href = currentUrlWithoutAnchor +'#'+ anchor;
}

function getValidCssColor(color) {
    color = color.replace(/[^A-F0-9]/, '');
    if(color.length==6) {
        return color;
    }
    return null;
}

function setBackgroundColor(elt, color) {
    if(elt) {
        color = getValidCssColor(color);
        if(color!=null) {
            color = '#' + color;
            elt.style.backgroundColor = color;
        }
    }
}

function replaceNodeWithText(target, text) {
    var parentNode = target.parentNode;
    var insertBeforeNode = target.nextSibling;
    parentNode.removeChild(target);
    appendTextAsNodeChildren(parentNode, text, insertBeforeNode);
}

function disableLinksOnPageForThemePreview() {
    var replaceFunc = function() {
        alert('Links are disabled while in preview mode.');
    };
    disableEventsOnChildren(document.getElementsByTagName('body')[0].childNodes, replaceFunc);

    var links = document.getElementsByTagName('a');
    for (var j=0; j < links.length; j++) {
        var link = links[j];
        if(!isEmpty(link.href)) {
            link.href = 'javascript:void(0);';
            link.onclick = replaceFunc;
        }
    }
}

function disableEventsOnChildren(childNodes, replaceFunc) {
    for(var i=0; i<childNodes.length; i++) {
        var childNode = childNodes[i];
        if(childNode.nodeType==1) {
            if(exists(childNode.onclick)) {
                childNode.onclick = replaceFunc;
            }
            if(exists(childNode.onchange)) {
                childNode.onchange = replaceFunc;
            }
            if(exists(childNode.onkeypress)) {
                childNode.onkeypress = replaceFunc;
            }
            if(exists(childNode.onkeyup)) {
                childNode.onkeyup = replaceFunc;
            }
            if(exists(childNode.onkeydown)) {
                childNode.onkeydown = replaceFunc;
            }
            if(exists(childNode.onselect)) {
                childNode.onselect = replaceFunc;
            }
            disableEventsOnChildren(childNode.childNodes, replaceFunc);
        }
    }
}

Narrative.yahooHistoryInitializersHandler = new OnloadObj();

function addYahooHistoryInitializerHandler(h, order) {
    Narrative.yahooHistoryInitializersHandler.addHandler(h, order);
}

YAHOO.util.Event.onDOMReady(function() {
    Narrative.onReadyHandler.handleOnLoad();
});
YAHOO.util.Event.addListener(window, 'load', function() {
    Narrative.onLoadHandler.handleOnLoad();
});

function setCheckboxListValue(checkboxId, isChecked) {
    $(checkboxId).checked = isChecked;
    updateCheckboxLabel(checkboxId);
}

function onPermissionClick(id) {
    $('old' + id).value = $(id).checked;
}

function applyStyle(elt, styleString) {
  styleString.parseStyle().each(function(pair) {
      alert('setting: ' + pair.key + ' to: ' + pair.value);
      elt.style[pair.key] = pair.value;
  });
}

function debugProperties(obj, includeFunctions) {
    if(!obj) {
        console.log('no props obj found');
    }
    //var hash = $H();
    console.log('getting props');
    for(var o in obj) {
        try {
            var prop = obj[o];
            if(typeof prop!='function' || includeFunctions) {
                console.log('found ' + o + ' = ' + obj[o]);
            }
            //hash[o] = obj[o];
        } catch(e) {
            console.log('found ' + o + ' but couldn\'t access value: ' + e);
        }
    }
    //console.log(debugHashProperties(hash));
}

function hrefOnclickIfNoKeypress(event,func) {
    var e = (!event) ? window.event : event;
    if (e.shiftKey || e.altKey || e.ctrlKey || e.metaKey)
        return;
    func.apply(this, $A(arguments).slice(2));
    YAHOO.util.Event.preventDefault(event);
}
Narrative.defaultPopupZIndex = 9000;

function createPopupWindow(config) {
    return {
        window : null,
        id : config.id,
        popupContentWrapperDivId : config.popupContentDivId + 'PopupContentWrapper',
        popupContentDivId : config.popupContentDivId + 'PopupContent',
        popupHeaderDivId : config.popupContentDivId + 'PopupHeader',
        popupFooterDivId : config.popupContentDivId + 'PopupFooter',
        title : config.title,
        resizable : config.resizable,
        modal : config.modal,
        closable: config.closable,
        draggable: config.draggable,
        width: config.width,
        height: config.height,
        onCloseFunction: config.onCloseFunction,
        show : function() {
            var thisRef = this;
            var showWindowFunc = function() {
                thisRef.initWindow();
                if(thisRef.isVisible()) {
                    thisRef.window.show();
                } else {
                    /* bl: on hide of the window, we set the PopupContentWrapper to display:none so
                         that flash elements in popups will stop playing.  thus, we need to re-display
                         the wrapper div just prior to calling window.show() or else we will get
                         strange behavior with the popup windows (Ext doesn't like us using display:none
                         since it uses visibility:hidden. */
                    Element.show(thisRef.popupContentWrapperDivId);
                    thisRef.window.show();
                    if(Narrative.isIE) {
                        Element.hide(thisRef.popupContentDivId);
                        Element.show(thisRef.popupContentDivId);
                    }
                }
            };
            if(Narrative.isIE) {
                YAHOO.util.Event.onDOMReady(showWindowFunc);
            } else {
                showWindowFunc();
            }
        },
        hide : function() {
            if(this.window!=null) {
                this.window.hide();
            }
        },
        center : function() {
            if(this.window!=null && this.isVisible()) {
                this.window.center();
            }
        },
        setTitle : function(title) {
            this.title = title;
            if(this.window!=null) {
                this.window.setHeader(title);
            }
        },
        setButtonsDisabled : function(disabled) {
            if(this.buttons) {
                this.buttons.each(function(button) {
                    button.set('disabled', disabled);
                });
            }
        },
        setContent : function(text) {
            Element.update($(this.popupContentDivId), text);
        },
        setAnimateTarget : function(el) {
            if(this.window!=null) {
                // todo: update if we are going to use animateTargets
                //this.window.setAnimateTarget(el);
            }
        },
        on : function(action,f) {
            this.window[action].subscribe(f);
        },
        addButton : function(name, label, handler) {

            if(this.buttonsAdded.get(name))
                return;

            this.buttonsAdded.set(name,true);

            var thisRef = this;
            var addButtonFunc = function() {
                var button = new YAHOO.widget.Button({
                    id: name + "Id",
                    label: label,
                    container: thisRef.popupFooterDivId,
                    type: 'push',
                    onclick: {
                        fn: handler
                    }
                });
                thisRef.buttons.push(button);
                window[name] = button;
            };
            if(this.window!=null) {
                addButtonFunc();
            } else {
                this.onInitHandlers.push(addButtonFunc);
            }
        },
        destroy : function() {
            if(this.window!=null) {
                if(this.isVisible()) {
                    this.hide();
                }
                this.window.destroy();
                this.window = null;
            }
        },
        onInitHandlers : new Array(),
        onOpenHandlers : new Array(),
        temporaryOnOpenHandlers : new Array(),
        buttonsAdded: $H(),
        buttons: $A(),
        isInitialized : function() {
            return this.window!=null;
        },
        initWindow : function() {
            if(this.isInitialized()) {
                return;
            }
            var contentDiv = $(this.popupContentWrapperDivId);
            var popupDivHolder = getYuiDivHolder();
            popupDivHolder.appendChild(contentDiv.parentNode.removeChild(contentDiv));
            this.window = new YAHOO.widget.Panel(this.popupContentWrapperDivId, {
                dragOnly:true,
                modal: this.modal,
                visible: false,
                close: this.closable,
                // bl: only using a shadow if *not* using auto-height.  we can use a shadow if we're using a fixed height.
                //underlay: this.height>0 ? 'shadow' : 'none',
                // bl: going with no underlay for any of the popup windows for now.
                underlay: 'none',

                draggable: this.draggable,
                width: this.width + "px",
                height: this.height>0 ? this.height + "px" : "auto",
                minHeight: 100,
                zIndex: Narrative.defaultPopupZIndex
                //, effect:{effect:YAHOO.widget.ContainerEffect.FADE,duration:0.25}
            });
            this.window.render();
            if(this.resizable) {
                this.resize = new YAHOO.util.Resize(this.popupContentWrapperDivId, {
                    handles: ["br"],
                    autoRatio: false,
                    minWidth: 300,
                    minHeight: 100,
                    status: false
                });

                // Setup startResize handler, to constrain the resize width/height
                // if the constraintoviewport configuration property is enabled.
                this.resize.on("startResize", function(args) {
                    var D = YAHOO.util.Dom;

                    var clientRegion = D.getClientRegion();
                    var elRegion = D.getRegion(this.element);

                    this.resize.set("maxWidth", clientRegion.right - elRegion.left - YAHOO.widget.Overlay.VIEWPORT_OFFSET);
                    this.resize.set("maxHeight", clientRegion.bottom - elRegion.top - YAHOO.widget.Overlay.VIEWPORT_OFFSET);
                }, this.window, true);

                // Setup resize handler to update the Panel's 'height' configuration property
                // whenever the size of the 'resizablepanel' DIV changes.

                // Setting the height configuration property will result in the
                // body of the Panel being resized to fill the new height (based on the
                // autofillheight property introduced in 2.6.0) and the iframe shim and
                // shadow being resized also if required (for IE6 and IE7 quirks mode).
                this.resize.on("resize", function(args) {
                    var panelHeight = args.height;
                    this.cfg.setProperty("height", panelHeight + "px");
                }, this.window, true);

            }
            this.window.setHeader(this.title);
            var thisRef = this;
            this.window.hideEvent.subscribe(function() {
                // bl: need to set display:none on the popup so that if the popup contains a flash
                // movie, the flash movie will stop playing (due to display:none).  visibility:hidden
                // will not have the same effect, and Ext uses visibility:hidden.
                Element.hide(thisRef.popupContentWrapperDivId);
                if(thisRef.onCloseFunction) {
                    thisRef.onCloseFunction();
                }
            });
            this.window.beforeShowEvent.subscribe(function() {
                thisRef.center();
            });
            this.window.showEvent.subscribe(function() {
                thisRef.doOnOpenHandlers();
            });
            this.onInitHandlers.each(function(h) {
                h();
            });
        },
        doOnOpenHandlers : function() {
            this.onOpenHandlers.each(function(h) {
                h();
            });
            this.temporaryOnOpenHandlers.each(function(h) {
                h();
            });
            this.temporaryOnOpenHandlers = new Array();
        },
        isVisible : function() {
            return this.window!=null && $(this.popupContentWrapperDivId).visible();
        },
        setWidth: function(width) {
            this.initWindow();
            this.window.cfg.setProperty('width', width+'px');
        }
    };
}

function getYuiDivHolder() {
    var popupDivHolder = $('SSPopupDivHolderDivId');
    if(!popupDivHolder) {
        popupDivHolder = document.createElement('div');
        // bl: this should style the popup using yahoo's style
        popupDivHolder.className = 'yui-skin-sam';
        popupDivHolder.id = 'SSPopupDivHolderDivId';
        document.body.appendChild(popupDivHolder);
    }
    return popupDivHolder;
}

function createConfirmationMessage(title, message, config) {
    config = config || {};
    return {
        id : 'confirmationMessage',
        messagePopup : null,
        title : title,
        message : message,
        setTitle : function(text) {
            this.title = text;
            if (this.messagePopup!=null) {
                this.messagePopup.setHeader(isEmpty(text) ? '&nbsp;' : text);
            }
        },
        setMessage : function(text) {
            this.message = text;
            if (this.messagePopup!=null) {
                this.messagePopup.setBody(text);
            }
        },
        openDialog : function() {
            var thisRef = this;
            if(this.messagePopup==null) {
                var handleOk = function() {
                    thisRef.closeDialog();
                };

                var popupDivHolder = getYuiDivHolder();

                var dialogConfig = {
                    fixedcenter: 'contained',
                    visible: false,
                    draggable: false,
                    close: true,
                    modal: true,
                    text: this.message,
                    constraintoviewport: true,
                    zIndex: Narrative.defaultPopupZIndex,
                    buttons: [ { text:"OK", handler:handleOk } ]
                };

                // Instantiate the Dialog
                this.messagePopup = new YAHOO.widget.SimpleDialog(this.id, dialogConfig);
                this.messagePopup.setHeader(isEmpty(this.title) ? '&nbsp;' : this.title);

                // Render the Dialog
                this.messagePopup.render(popupDivHolder.id);
                this.messagePopup.hideEvent.subscribe(function() {

                });
            }
            this.messagePopup.show();
            if(!isEmpty(config.autoHideDelayMs)) {
                if(config.isShowingNow) {
                    addOnloadHandler(function() {
                        thisRef.setAutoHideTimeout();
                    });
                }
            } else {
                this.setAutoHideTimeout();
            }
        },
        closeDialog : function() {
            if(this.messagePopup!=null) {
                this.messagePopup.hide();
            }
        },
        setAutoHideTimeout : function() {
            if(!isEmpty(config.autoHideDelayMs)) {
                var thisRef = this;
                setTimeout(function() {
                    thisRef.closeDialog();
                }, config.autoHideDelayMs);
            }
        },
        isVisible : function() {
            return this.messagePopup && $(this.id).visible();
        }
    };
}


Narrative.roundedDivs = {};
Narrative.ajaxRoundedDivs = new Narrative.Registry(function(roundedDivId) {
    delete Narrative.roundedDivs[roundedDivId];
});

Narrative.ajaxDivPopupInnerDivPopups = {
    innerDivPopupsToDestroyOnlyOnFullReload : $H(),
    destroyInnerDivPopup : function(innerDivPopupWindow) {
        innerDivPopupWindow.destroy();
        // bl: destroy the popup and then also destroy the PopupWindow variable
        // so that subsequent AJAX calls will result in the PopupWindow object
        // being recreated.
        eval('delete ' + innerDivPopupWindow.id + 'PopupWindow;');
    },
    allInnerDivPopups : new Narrative.Registry(function(innerDivPopupWindow, ajaxDivPopupId) {
        // skip it if we are only supposed to destroy it on close.
        if(Narrative.ajaxDivPopupInnerDivPopups.innerDivPopupsToDestroyOnlyOnFullReload.get(ajaxDivPopupId)
                && Narrative.ajaxDivPopupInnerDivPopups.innerDivPopupsToDestroyOnlyOnFullReload.get(ajaxDivPopupId).get(innerDivPopupWindow.id)) {
            return;
        }
        Narrative.ajaxDivPopupInnerDivPopups.destroyInnerDivPopup(innerDivPopupWindow);
    }),
    registerInnerDivPopup : function(ajaxDivPopupId, innerDivPopupWindow, onlyDestroyOnFullReload) {
        this.allInnerDivPopups.registerObject(ajaxDivPopupId, innerDivPopupWindow);
        if(onlyDestroyOnFullReload) {
            var docMap = this.innerDivPopupsToDestroyOnlyOnFullReload.get(ajaxDivPopupId);
            if(!docMap) {
                docMap = this.innerDivPopupsToDestroyOnlyOnFullReload.set(ajaxDivPopupId, $H());
            }
            docMap.set(innerDivPopupWindow.id, innerDivPopupWindow);
        }
    },
    clearInnerDivPopups : function(ajaxDivPopupId, isReloadingFullPopupContents) {
        this.allInnerDivPopups.clearObjects(ajaxDivPopupId);
        if(this.innerDivPopupsToDestroyOnlyOnFullReload.get(ajaxDivPopupId)) {
            this.innerDivPopupsToDestroyOnlyOnFullReload.get(ajaxDivPopupId).each(function(pair) {
                if(isReloadingFullPopupContents) {
                    // need to destroy those windows which we did not already destroy
                    Narrative.ajaxDivPopupInnerDivPopups.destroyInnerDivPopup(pair.value);
                } else {
                    // need to re-register those windows which we did not destroy
                    Narrative.ajaxDivPopupInnerDivPopups.allInnerDivPopups.registerObject(ajaxDivPopupId, pair.value);
                }
            });
            if(isReloadingFullPopupContents) {
                this.innerDivPopupsToDestroyOnlyOnFullReload.unset(ajaxDivPopupId);
            }
        }
    }
};

function cleanupAjaxDivPopupItems(ajaxDivPopupId, isReloadingFullPopupContents) {
    Narrative.ajaxDivPopupInnerDivPopups.clearInnerDivPopups(ajaxDivPopupId, isReloadingFullPopupContents);
    Narrative.ajaxRoundedDivs.clearObjects(ajaxDivPopupId);
}

function getStaticVideoPlayerParams() {
    return {
        allowScriptAccess : 'always',
        allowFullScreen : 'true',
        wmode : 'transparent',
        style : 'z-index:-9999',
        bgcolor : '#000000'
    };
}

/**
 * Handles registering a callback method globally, and then including the provided URL
 * as a script in the head.  Once the script is loaded it will be cleaned up, and the callback
 * method will be unregistered from the global scope.  The url must contain a {callback} parameter
 * if you want the application to process your provided callback function when the contents are loaded
 */
var jsonpId = 0, jsonpHead = $$('head')[0], jsonpGlobal = this;
var getJSONP = function(url, callback) {
    var script = document.createElement('script'), token = '__jsonp' + jsonpId;

    // callback should be a global function
    jsonpGlobal[token] = callback;

    // url should have "?" parameter which is to be replaced with a global callback name
    script.src = url.replace('{callback}', token);

    // clean up on load: remove script tag, null script variable and delete global callback function
    script.onload = function() {
      script.remove();
      script = null;
      delete jsonpGlobal[token];
    };
    jsonpHead.appendChild(script);

    // callback name should be unique
    jsonpId++;
};

function getSelectedText() {
    var quote;
    if(window.getSelection && window.getSelection().toString()){
        quote = getSelection();
    } else if(document.selection && document.selection.createRange().text){
        quote = document.selection.createRange().text;
    } else {
        quote = false;
    }

    if(quote){
        quote = quote.toString();
        stripquote = quote.replace(/^\s*|\s*$/g,"");
        return stripquote;
    }
    return "";
}

function setAttributeSafely(element, name, value) {
    if (typeof element != 'undefined' && element.setAttribute) {
        element.setAttribute(name, value);
    }
}

/**
 * JW: in some cases (chrome) we are seeing that the XML returned back from a ajax request which contains raw <tr>...</tr>
 * table data when inserted into a table element does not contain the expected tbody element.  This issue is actually fixed
 * in the current test version of Chrome, but we need a way to support this current funky version.  This code below attempts
 * to handle that case, where the first node may unexpectedly be our expected node
 * @param table The table element which had raw table data updated into it.
 */
function getTrFromTable(table) {
    var tr = table.firstChild;
    if (tr.tagName != 'TR') {
        tr = tr.firstChild;
    }

    return tr;
}

var navigationOverlayRegistry = new Array();
function doHideMenus() {
    navigationOverlayRegistry.each(function(overlayObject) {
        overlayObject.hide();
    });
}

function escapeJavascriptLiteralString(value, escapeQuotes) {
    value = value.replace(/\\/g, '\\\\');
    if(escapeQuotes) {
        value = value.replace(/"/g, '\\"');
    } else {
        value = value.replace(/'/g, "\\'");
    }
    // convert all newlines sequences into just \n
    value = value.replace(/\r\n/g, '\n');
    // now, in order to continue a string onto multiple lines, the line must end with a backslash
    return value.replace(/\n/g, '\\n\\\n');
}

function toggleTools(oid, show) {
    setVisibility($('centerTools_'+oid), show)
}

Narrative.setWindowUnloadMessageForForm = function(message, formId) {
    var saveWysiwygsForForm = function() {
        tinymce.each(tinyMCE.editors, function(editor) {
            var editorField = $(editor.editorId);
            if(editorField && editorField.form && editorField.form.id==formId) {
                triggerSave(editor.id);
            }
        });
    };
    if (Narrative.isSupportsWysiwygEditor) {
        saveWysiwygsForForm();
    }
    var serializedForm = Form.serialize(formId);

    window.onbeforeunload = function (e) {
        if (Narrative.isSupportsWysiwygEditor) {
            saveWysiwygsForForm();
        }
        if (serializedForm==Form.serialize(formId)) {
            return;
        }

        var e = e || window.event;

        // For IE and Firefox
        if(e) {
            e.returnValue = message;
        }

        // For Safari
        return message;
    };
};

Narrative.removeWindowUnloadMessage = function() {
    window.onbeforeunload = null;

    return true;
};

function addUserNoteCountDiv(userOid, userNoteCountDivId) {
    if(!Narrative.userNoteCountIds) {
        Narrative.userNoteCountIds = $H();
    }
    var usersCounts = Narrative.userNoteCountIds.get(userOid);
    if(!usersCounts) {
        Narrative.userNoteCountIds.set(userOid, usersCounts = $A());
    }
    usersCounts.push(userNoteCountDivId);
}

YahooHistory = Class.create();
YahooHistory.prototype = {
    initialize : function(stateName, initialState, onStateChangeHandler, isIe) {
        this.stateName = stateName;
        this.isIe = isIe;
        this.bookmarkedState = YAHOO.util.History.getBookmarkedState(stateName);
        this.initialState = initialState;
        this.initializationState = this.bookmarkedState || this.initialState;
        this.currentState = this.initialState;
        this.onStateChangeHandler = onStateChangeHandler;

        var history = this;
        YAHOO.util.History.register(stateName, this.initializationState, function(state) {
            history.swapState(state);
        });
        addYahooHistoryInitializerHandler(function() {
            history.init();
        });
    },

    swapState: function (state, isInit) {
        if(this.currentState==state) {
            return;
        }
        this.currentState = state;

        this.onStateChangeHandler(state, isInit);
    },

    changeState: function (state) {
        try {
            YAHOO.util.History.navigate(this.stateName, state);
            if (this.isIe) {
                setTimeout(function() {
                    // do nothing
                }, 25);
            }
        } catch(e) {
            alert(e);
            this.swapState(state);
        }
    },

    init: function() {
        if (document.location.hash.substr(1).length > 0) {
            var currentPageState = YAHOO.util.History.getCurrentState(this.stateName);
            if(currentPageState != this.initialState) {
                this.swapState(currentPageState, true);
            }
        }
    }
};
