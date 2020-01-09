var formToElementToOriginalClassName = {};

function ajaxFormSubmit(form, replaceDiv, functionToCall, validationErrorFunctionToCall, failureFunctionToCall, insertErrorIntoReplaceDiv, suppressErrorMessage, formErrorMessagesId, skipFormResetOnSuccess, fullyReplaceDiv) {
    // bl: well, TinyMCE's event handler won't necessarily have been triggered by this point,
    // so we need to trigger it ourselves.  kinda lame, but this is what it's going to take
    // to get the WYSIWYG editor to work on ajax forms.
    if(form.hasWysiwygEditor) {
        tinyMCE.triggerSave();
    }
    var serializedForm = Form.serialize(form);
    var opt = {
        method: form.method,
        parameters: (form.method=='GET' || form.method=='get') ? serializedForm : '',
        postBody: (form.method=='GET' || form.method=='get') ? null : serializedForm
    };

    var validationFunction = function(t) {
        // jw: this is required because form.id will reference any input on the form that has a name of id.  getAttribute
        //     will actually give us the ID of the form itself
        var formId = form.getAttribute('id');
        var errorMessagesContainer = $('errorMessageContainer_' + (isEmpty(formErrorMessagesId) ? formId : formErrorMessagesId));
        var errorMessagesContainerTitle = $('errorMessageContainerTitle_' + (isEmpty(formErrorMessagesId) ? formId : formErrorMessagesId));
        var eltToInsertErrorsInto = $('errorMessages_' + (isEmpty(formErrorMessagesId) ? formId : formErrorMessagesId));
        resetAjaxFormErrors(form, eltToInsertErrorsInto, errorMessagesContainer);
        // create a new array of the original elements
        originalElements = new Array();
        formToElementToOriginalClassName[formId] = originalElements;

        if(t.responseXML != null) {
            var actionErrors = t.responseXML.getElementsByTagName("actionError");
            for(i=0;i<actionErrors.length;i++) {
                var liElement = document.createElement('li');
                liElement.className = 'errorMessageLi';
                liElement.setAttribute('id', formId +'_action_error_'+ i +'_li');
                liElement.appendChild(createSpanElement(actionErrors[i].firstChild.data,'errorMessage', formId +'_action_error_'+i));
                eltToInsertErrorsInto.appendChild(liElement);
            }

            var fieldErrors = t.responseXML.getElementsByTagName("fieldError");
            for(i=0;i<fieldErrors.length;i++) {
                var fieldName = fieldErrors[i].getAttribute("fieldName");
                var messages = fieldErrors[i].getElementsByTagName("message");
                for(j=0;j<messages.length;j++) {
                    // todo: make this support checkbox lists and radio buttons - neither currently supported by error highlighting.
                    var formField = form.elements[fieldName];
                    var isCheckbox=false;
                    var formFieldId=null;
                    if(exists(formField)){
                        isCheckbox = formField.type=='checkbox';
                        formFieldId = formField.getAttribute ? formField.getAttribute('id') : null;
                    }
                    if(isEmpty(formFieldId)){
                        formFieldId= formId+"_"+fieldName;
                        if(!$(formFieldId+'_label')){
                            formFieldId=null;
                        }
                    }
                    if(!isEmpty(formFieldId)) {
                        // checkboxes don't use our control  wrapper
                        if(isCheckbox) {
                            // set the control wrapper as in error
                            var controlWrapperId = 'wwctrl_' + formFieldId;
                            var controlWrapper = $(controlWrapperId);
                            if(exists(controlWrapper)) {
                                var idAndClassName = new Object();
                                idAndClassName['id'] = controlWrapperId;
                                idAndClassName['originalClassName'] = controlWrapper.className;
                                originalElements.push(idAndClassName);
                                controlWrapper.className = 'wwctrl_error';
                            }
                        }

                        var labelId = formFieldId + '_label';
                        var label = $(labelId);
                        if(exists(label)) {
                            var idAndClassName = new Object();
                            idAndClassName['id'] = labelId;
                            idAndClassName['originalClassName'] = label.className;
                            originalElements.push(idAndClassName);
                            label.className = isCheckbox ? 'checkboxErrorLabel' : 'errorLabel';
                        }
                    }
                    var liElement = document.createElement('li');
                    liElement.className = 'errorMessageLi';
                    liElement.setAttribute('id', formId + '_' + fieldName+'_error_'+j+'_li');
                    liElement.appendChild(createSpanElement(messages[j].firstChild.data,'errorMessage', formId + '_' + fieldName+'_error_'+j));
                    eltToInsertErrorsInto.appendChild(liElement);
                    //form.elements[fieldName].parentNode.insertBefore(createSpanElement(messages[j].firstChild.data,'errorMessage', formId + '_' + fieldName+'_error_'+j),form.elements[fieldName]);
                }
            }

            if(actionErrors.length>0 || fieldErrors.length>0) {
                // reset the ajax form errors in the event that someone tries to reset the form.
                // will happen on the call to form.reset() below when the ajax request is successful, too.
                form.resetObserver = function() {
                    resetAjaxFormErrors(form, eltToInsertErrorsInto, errorMessagesContainer);
                };
                Event.observe(form, 'reset', form.resetObserver);
                // call the error function
                if(exists(validationErrorFunctionToCall)){
                    validationErrorFunctionToCall();
                }
                errorMessagesContainer.show();
                errorMessagesContainerTitle.show();
                if(isEmpty(formErrorMessagesId)) {
                    document.location.href = '#formTop_'+ formId;
                }
                return false;
            }
        }
        return true;
    };

    var ajaxSubmitArgs = new AjaxSubmitArgs(replaceDiv, functionToCall, failureFunctionToCall, validationFunction, skipFormResetOnSuccess ? null : form, fullyReplaceDiv);
    if(insertErrorIntoReplaceDiv!=undefined && (typeof insertErrorIntoReplaceDiv!='undefined')) {
        ajaxSubmitArgs.insertErrorIntoReplaceDiv = insertErrorIntoReplaceDiv;
    }
    if(suppressErrorMessage!=undefined && (typeof suppressErrorMessage!='undefined')) {
        ajaxSubmitArgs.suppressErrorMessage = suppressErrorMessage;
    }
    ajaxSubmitArgs.form = form;
    ajaxSubmit(form.getAttribute('action'), opt, ajaxSubmitArgs);
}

AjaxSubmitArgs = Class.create();
AjaxSubmitArgs.prototype = {
    initialize: function(replaceDiv, functionToCall, failureFunctionToCall, responseValidationFunction, formToResetOnSuccess, fullyReplaceDiv) {
        this.replaceDiv = replaceDiv;
        this.functionToCall = functionToCall;
        this.failureFunctionToCall = failureFunctionToCall;
        this.responseValidationFunction = responseValidationFunction;
        this.formToResetOnSuccess = formToResetOnSuccess;
        this.fullyReplaceDiv = fullyReplaceDiv;
    },
    insertErrorIntoReplaceDiv : false,
    suppressErrorMessage : false,
    form : null
};

function handleAjaxErrorMessage(ajaxSubmitArgs, title, message) {
    showAjaxErrorMessage(title, message, ajaxSubmitArgs.insertErrorIntoReplaceDiv, ajaxSubmitArgs.replaceDiv);
}

function handleAjaxXmlError(transport,ajaxSubmitArgs) {
    if(transport && transport.responseXML && !ajaxSubmitArgs.suppressErrorMessage) {
        var ajaxErrorTitle = transport.responseXML.getElementsByTagName("ajaxErrorTitle");
        var ajaxErrorMessage = transport.responseXML.getElementsByTagName("ajaxErrorMessage");
        if(ajaxErrorTitle!=null && ajaxErrorTitle.length>0 && ajaxErrorMessage!=null && ajaxErrorMessage.length>0) {
            handleAjaxErrorMessage(ajaxSubmitArgs, getAllChildrenTextNodesAsString(ajaxErrorTitle[0]), getAllChildrenTextNodesAsString(ajaxErrorMessage[0]));
            return true;
        }
    }
    return false;
}

function showAjaxErrorMessage(title, message, insertErrorIntoReplaceDiv, replaceDiv) {
    if(insertErrorIntoReplaceDiv && !isEmpty(replaceDiv)) {
        Element.update($(replaceDiv), message);
    } else {
        var errorPopup = createConfirmationMessage(title, message);
        errorPopup.openDialog();
        if(typeof pleaseWaitPopupWindow != 'undefined') {
            if(pleaseWaitPopupWindow.isVisible()) {
                pleaseWaitPopupWindow.hide();
            }
        }
    }
}

function defaultAjaxSubmit(url, replaceDiv, functionToCall, failureFunctionToCall, fullyReplaceDiv) {
    defaultAjaxSubmitWithArgs(url, new AjaxSubmitArgs(replaceDiv, functionToCall, failureFunctionToCall, null, null, fullyReplaceDiv));
}

function defaultAjaxSubmitWithArgs(url, ajaxSubmitArgs, prototypeOptions) {
    var opt = {
        method: 'get'
    };
    ajaxSubmit(url, opt, ajaxSubmitArgs, prototypeOptions);
}

function ajaxSubmit(action, ajaxRequestOptions, ajaxSubmitArgs, prototypeOptions) {
    ajaxSubmitArgs = ajaxSubmitArgs || {};

    var reloadCaptchaIfNecessary = function() {
        if(ajaxSubmitArgs.form && ajaxSubmitArgs.form.reloadCaptcha) {
            ajaxSubmitArgs.form.reloadCaptcha();
        }
    };

    var failureAndExceptionFunction = function(isException,debug,transport) {
        reloadCaptchaIfNecessary();
        if(handleAjaxXmlError(transport,ajaxSubmitArgs)) {
            return;
        }
        if (ajaxSubmitArgs.failureFunctionToCall != null) {
            ajaxSubmitArgs.failureFunctionToCall();
        }
        // jw: lets short out if we are not going to be displaying the error message.  No point building it if we know it
        //     wont be used.
        if (ajaxSubmitArgs.suppressErrorMessage) {
            return;
        }

        var errorMessage = isException ? Narrative.ajaxExceptionErrorMessage : Narrative.ajaxFailureErrorMessage;
        errorMessage += '<br /><br /><a href="javascript:void(0);" onclick="Element.show(\'ajaxErrorDetailsMsg\');Element.hide(this);$(\'ajaxErrorDetailsMsg\').select();">' + Narrative.showDetailsText + '</a>';
        errorMessage += '<textarea id="ajaxErrorDetailsMsg" style="width:500px;height:200px;display:none;" onclick="this.select();" readonly="true">';
        errorMessage += 'Reference ID: ' + Narrative.ajaxUnknownErrorReferenceId;
        errorMessage += '\n\naction: ' + action;
        if(!isEmpty(ajaxRequestOptions.parameters)) {
            errorMessage += '\n\nparams: ' + ajaxRequestOptions.parameters;
        }
        if(!isEmpty(ajaxRequestOptions.postBody)) {
            errorMessage += '\n\npostBody: ' + ajaxRequestOptions.postBody;
        }
        if(!isEmpty(debug)) {
            errorMessage += '\n\ndebug: ' + debug;
        }
        errorMessage += '</textarea>';
        // bl: the following ajax error title/message variables are defined and available from the basicWrapper.tag.
        // need to be there so that they can be localized appropriately.
        handleAjaxErrorMessage(ajaxSubmitArgs, Narrative.ajaxFailureErrorTitle, errorMessage);
    };

    var isGet = (ajaxRequestOptions.method=='GET' || ajaxRequestOptions.method=='get');

    var parametersString = ajaxRequestOptions.parameters;
    if(isEmpty(parametersString))
        parametersString = ajaxRequestOptions.postBody;
    if(isEmpty(parametersString))
        parametersString = '';

    ajaxRequestOptions = Object.extend({
        onSuccess: function(t) {
            if(isGet) {
                // bl: need to parse the URL so that we only include the path and query string when submitting it to GA.
                var parser = document.createElement('a');
                parser.href = action;
                var pageViewPath = parser.pathname + parser.search;
                if(typeof ga != 'undefined') {
                    ga(function () {
                        var trackers = ga.getAll();
                        for(var i = 0; i < trackers.length; i++) {
                            var tracker = trackers[i];
                            tracker.send('pageview', pageViewPath);
                            //var trackerId = tracker.get('name');
                            //var sendPrefix = isEmpty(trackerId) ? '' : (trackerId + '.');
                            //ga(sendPrefix + 'send', 'pageview', action);
                        }
                    });
                }
                Narrative.googleAnalyticsIds.each(function(analyticsId) {
                    _gaq.push(['_setAccount', analyticsId]);
                    _gaq.push(['_trackPageview'], pageViewPath);
                });
            }

            // bl: if the status is 0 and the response is empty, then that likely means the AJAX request was canceled (e.g. when navigating away from the page).
            if(t.status==0 && isEmpty(t.responseText)) {
                return;
            }

            if(handleAjaxXmlError(t,ajaxSubmitArgs)) {
                reloadCaptchaIfNecessary();
                return;
            }

            if(!isEmpty(ajaxSubmitArgs.replaceDiv)) {
                /*
                    bl: when setting html via innerHTML, the script tags will not be evaluated
                    in most browsers (other than Netscape 6, from what i understand).  thus,
                    in order to support evaluating the script elements being set via innerHTML,
                    we have to explicitly evaluate the script blocks instead.  luckily, prototype
                    offers this for us in Element.update() :)
                    for more info: http://groups.google.co.uk/group/comp.lang.javascript/browse_thread/thread/f4cb3fae3eb9ea4c/60b52777163fe8ac?lnk=raot&hl=en
                */
                //$(replaceDiv).innerHTML = t.responseText;
                // bl:safaricrash note that the following Element.update is typically the root cause of Safari
                // crashes that result from AJAX responses.  this is because of a reg ex that prototype uses
                // to extract scripts from the resulting HTML.  for some reason, Safari croaks on the combination
                // of some complex HTML/JavaScript and the reg ex.
                if (ajaxSubmitArgs.fullyReplaceDiv) {
                    Element.replace($(ajaxSubmitArgs.replaceDiv), t.responseText);

                } else {
                    Element.update($(ajaxSubmitArgs.replaceDiv), t.responseText);
                }
            }

            if(ajaxSubmitArgs.functionToCall!=null) {
                ajaxSubmitArgs.functionToCall(t.responseXML, t.responseText, parametersString.toQueryParams(), t.responseJSON);
            }

            if(t.responseXML!=null) {
                if (handleAjaxRedirectResponse(t.responseXML)) {
                    return;
                }
            }

            if(ajaxSubmitArgs.formToResetOnSuccess!=null) {
                ajaxSubmitArgs.formToResetOnSuccess.reset();
                // bl: only reload the captcha if we are resetting the form. if we aren't resetting the form,
                // then the page may be reloading, in which case we should just leave the captcha as-is.
                reloadCaptchaIfNecessary();
            }
        },
        onFailure: function(transport, json) {
            // break out if the response validation function didn't succeed.
            if(ajaxSubmitArgs.responseValidationFunction!=null && !ajaxSubmitArgs.responseValidationFunction(transport)) {
                reloadCaptchaIfNecessary();
                return;
            }
            var debug = '';
            debug += 'onFailure';
            debug += '\n\ntransport: ' + transport;
            debug += '\n\ntransport.status code: ' + transport.status;
            debug += '\n\njson: ' + json;
            failureAndExceptionFunction(false,debug,transport);
        },
        onException: function(ajaxRequest, exception) {
            var debug = '';
            debug += 'onException';
            debug += '\n\nurl: ' + document.location.href;
            debug += '\n\nuserAgent: ' + navigator.userAgent;
            debug += '\n\najaxRequest: ' + ajaxRequest;
            debug += '\n\nexception: ' + exception;
            debug += '\n\nstack: ' + Narrative.getStackTracePlainText(exception);
            debug += '\n\nexception properties: ' + debugHashProperties($H(exception));
            // bl: exception.stack will be automatically included in exception properties for Gecko-based browsers.
            //debug += '<br /><b>exception.stack</b>: ' + (exception==null ? null : exception.stack);
            failureAndExceptionFunction(true,debug);
        }
    }, ajaxRequestOptions || {});

    // bl: in order to get ajax working in an iframe, need to make the ajax calls through
    // the parent object.  don't ask me why (and there may be a better way to do it), but at
    // least this is functioning for now!
    // note that the problem was purely a Firefox issue.  ajax works fine in IE in an iframe.
    // the error we would get in Firefox is:
    // Component returned failure code: 0x80004005 (NS_ERROR_FAILURE) [nsIXMLHttpRequest.setRequestHeader]
    // bl: we need to use AJAX in iframes for embedded chat events.  that was causing lots of errors in Safari
    // when rendering embedded chats on another domain, like:
    /*
    Unsafe JavaScript attempt to access frame with URL http://alpha.eveforenterprise.com/ from frame with URL http://intranet.alpha.eveforenterprise.com/chatEmbed/chatEvent/davechat. Domains, protocols and ports must match.
    davechat:3519
    <a> misnested or not properly closed.  Cloning <a> in order to preserve the styles applied by it.
    5Unsafe JavaScript attempt to access frame with URL http://alpha.eveforenterprise.com/ from frame with URL http://intranet.alpha.eveforenterprise.com/chatEmbed/chatEvent/davechat. Domains, protocols and ports must match.
    common.js:963
    TypeError: Result of expression 'windowToUse.Ajax' [undefined] is not an object.
    favicon.ico
    Resource interpreted as other but transferred with MIME type image/x-icon.
    */
    // bl: for now, just going to change this back to using a normal Ajax.Request from the current window
    // since we hardly (if at all?) use iframes anymore.
    //var windowToUse = getIframeParent();
    //new windowToUse.Ajax.Request(action, ajaxRequestOptions);
    new Ajax.Request(action, ajaxRequestOptions, prototypeOptions);
}

function handleAjaxRedirectResponse(xml) {
    var redirectUrl = getXmlDataFromTagName(xml, "redirect-url");
    if(!isEmpty(redirectUrl)) {
        // bl: if reloading in an iframe, reload the parent page instead of the iframe.
        var windowToUse = getIframeParent();
        ensurePageReload(windowToUse.document.location, redirectUrl);
        return true;
    }
    return false;
}

function resetAjaxFormErrors(form, eltToInsertErrorsInto, errorMessagesContainer) {
    errorMessagesContainer.hide();
    var formId = form.getAttribute('id');
    var previousErrors = Narrative.getElementsByClassName('errorMessageLi', eltToInsertErrorsInto);
    for(i=0;i<previousErrors.length;i++){
        var currentError = previousErrors[i];
        // only remove the element from the form currently being submitted as evidenced by the
        // inclusion of the form id at the beginning of the error message field id.
        // previously, this would have removed all error message fields from all of the forms
        // on the page, which isn't very good behavior :)
        var currentId = currentError.getAttribute('id');
        if(!isEmpty(currentId) && currentId.indexOf(formId)==0) {
            currentError.parentNode.removeChild(currentError);
        }
    }

    var originalElements = formToElementToOriginalClassName[formId];
    // reset the original elements, if found
    if(exists(originalElements)) {
        originalElements.each(function(element) {
            $(element['id']).className = element['originalClassName'];
        });
    }

    if(form.resetObserver) {
        Event.stopObserving(form, 'reset', form.resetObserver);
        form.resetObserver = null;
    }
}