package org.narrative.network.shared.interceptors;

import org.narrative.common.util.IPStringUtil;
import org.narrative.common.web.struts.MethodPropertiesUtil;
import org.narrative.network.shared.struts.NetworkResponses;
import org.narrative.network.shared.util.NetworkLogger;
import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.Validateable;
import com.opensymphony.xwork2.ValidationAware;
import com.opensymphony.xwork2.interceptor.MethodFilterInterceptor;
import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpServletResponse;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * <!-- START SNIPPET: description -->
 * <p>
 * An interceptor that does some basic validation workflow before allowing the interceptor chain to continue.
 * <p>
 * <p/>This interceptor does nothing if the name of the method being invoked is specified in the <b>excludeMethods</b>
 * parameter. <b>excludeMethods</b> accepts a comma-delimited list of method names. For example, requests to
 * <b>foo!input.action</b> and <b>foo!back.action</b> will be skipped by this interceptor if you set the
 * <b>excludeMethods</b> parameter to "input, back".
 * <p>
 * <p/>The order of execution in the workflow is:
 *
 * <ol>
 *
 * <li>If the action being executed implements {@link com.opensymphony.xwork2.Validateable}, the action's {@link com.opensymphony.xwork2.Validateable#validate()
 * validate} method is called.</li>
 *
 * <li>Next, if the action implements {@link com.opensymphony.xwork2.ValidationAware}, the action's {@link com.opensymphony.xwork2.ValidationAware#hasErrors()
 * hasErrors} method is called. If this method returns true, this interceptor stops the chain from continuing and
 * immediately returns {@link com.opensymphony.xwork2.Action#INPUT}</li>
 *
 * </ol>
 * <p>
 * <p/> Note: if the action doesn't implement either interface, this interceptor effectively does nothing. This
 * interceptor is often used with the <b>validation</b> interceptor. However, it does not have to be, especially if you
 * wish to write all your validation rules by hand in the validate() method rather than in XML files.
 * <p>
 * <p/>
 *
 * <b>NOTE:</b> As this method extends off MethodFilterInterceptor, it is capable of
 * deciding if it is applicable only to selective methods in the action class. See
 * <code>MethodFilterInterceptor</code> for more info.
 * <p>
 * <!-- END SNIPPET: description -->
 * <p>
 * <p/> <u>Interceptor parameters:</u>
 * <p>
 * <!-- START SNIPPET: parameters -->
 *
 * <ul>
 *
 * <li>None</li>
 *
 * </ul>
 * <p>
 * <!-- END SNIPPET: parameters -->
 * <p>
 * <p/> <u>Extending the interceptor:</u>
 * <p>
 * <p/>
 * <p>
 * <!-- START SNIPPET: extending -->
 * <p>
 * There are no known extension points for this interceptor.
 * <p>
 * <!-- END SNIPPET: extending -->
 * <p>
 * <p/> <u>Example code:</u>
 *
 * <pre>
 * <!-- START SNIPPET: example -->
 *
 * &lt;action name="someAction" class="com.examples.SomeAction"&gt;
 *     &lt;interceptor-ref name="params"/&gt;
 *     &lt;interceptor-ref name="validation"/&gt;
 *     &lt;interceptor-ref name="workflow"/&gt;
 *     &lt;result name="success"&gt;good_result.ftl&lt;/result&gt;
 * &lt;/action&gt;
 *
 * &lt;-- In this case myMethod of the action class will not pass through
 *        the workflow process --&gt;
 * &lt;action name="someAction" class="com.examples.SomeAction"&gt;
 *     &lt;interceptor-ref name="params"/&gt;
 *     &lt;interceptor-ref name="validation"/&gt;
 *     &lt;interceptor-ref name="workflow"&gt;
 *         &lt;param name="excludeMethods"&gt;myMethod&lt;/param&gt;
 *     &lt;/interceptor-ref name="workflow"&gt;
 *     &lt;result name="success"&gt;good_result.ftl&lt;/result&gt;
 * &lt;/action&gt;
 *
 * <!-- END SNIPPET: example -->
 * </pre>
 *
 * @author Jason Carreira
 * @author Rainer Hermanns
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 * @version $Date: 2006/03/18 04:42:54 $ $Id: DefaultWorkflowInterceptor.java,v 1.18 2006/03/18 04:42:54 tmjee Exp $
 */
public class NarrativeWorkflowInterceptor extends MethodFilterInterceptor {

    private static final NetworkLogger _log = new NetworkLogger(NarrativeWorkflowInterceptor.class);

    /**
     * @see com.opensymphony.xwork2.interceptor.MethodFilterInterceptor#doIntercept(com.opensymphony.xwork2.ActionInvocation)
     */
    protected String doIntercept(ActionInvocation invocation) throws Exception {
        Object action = invocation.getAction();

        if (action instanceof Validateable) {
            Validateable validateable = (Validateable) action;
            if (_log.isDebugEnabled()) {
                _log.debug("invoking validate() on action " + validateable);
            }
            validateable.validate();
        }

        if (action instanceof ValidationAware) {
            ValidationAware validationAwareAction = (ValidationAware) action;

            if (validationAwareAction.hasErrors()) {
                HttpServletResponse response = ServletActionContext.getResponse();
                // bl: if there are validation errors, then mark the request as a bad request.
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

                if (_log.isDebugEnabled()) {
                    _log.debug("errors on action " + validationAwareAction + ", returning result name 'input'");
                }
                if (action instanceof ValidationErrorResponseCodeProvider) {
                    String errorResponseCode = ((ValidationErrorResponseCodeProvider) action).getErrorResponseCode();
                    if (!IPStringUtil.isEmpty(errorResponseCode)) {
                        return errorResponseCode;
                    }
                }
                String errorResponseCode = MethodPropertiesUtil.getErrorResponseCode(invocation);
                if (!IPStringUtil.isEmpty(errorResponseCode)) {
                    return errorResponseCode;
                }
                // bl: default ajax requests to ajaxErrorsResult.
                // bl: we'll do the AJAX errors result regardless of whether the request is supposed to be AJAX or not.
                // if it's requested as AJAX or if it's supposed to be AJAX, we'll return the AJAX errors result.
                // there's no guarantee that returning input will work in all cases, so this is really the only option.
                if (MethodPropertiesUtil.isAjaxRequest(invocation)) {
                    return NetworkResponses.legacyAjaxErrorsResult();
                }
                // default to input for non-ajax requests.
                return Action.INPUT;
            }
        }

        return invocation.invoke();
    }

    protected boolean applyInterceptor(ActionInvocation invocation) {
        // bl: don't apply the interceptor if this method is set to bypass validate.
        if (MethodPropertiesUtil.isBypassValidate(invocation)) {
            return false;
        }
        // bl: also do not validate for any GET requests, regardless of whether they are called "input" or not.
        if (MethodPropertiesUtil.getHttpMethodType(invocation).isGet()) {
            return false;
        }

        // if this is not a post we do not want to apply the Validate method
        if (!networkContext().getReqResp().isPost()) {
            return false;
        }

        return super.applyInterceptor(invocation);
    }
}
