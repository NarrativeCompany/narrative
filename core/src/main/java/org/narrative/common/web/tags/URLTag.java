package org.narrative.common.web.tags;

import org.narrative.common.util.NarrativeException;
import org.narrative.common.util.IPHTMLUtil;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.UnexpectedError;
import com.opensymphony.xwork2.ActionContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Feb 16, 2006
 * Time: 11:14:01 AM
 */
public class URLTag extends BodyTagSupport {
    protected String includeParams;
    protected String value;
    protected String stripParamName;
    // bl: no longer going to encode URLs since we're basically going to require
    // that you have cookies enabled in order to use our system.
    private boolean encode = false;
    private String id;
    private Map<String, Collection<String>> parameters;
    private boolean appendParamsAsPathParams = false;

    public static final String NONE = "none";
    public static final String GET = "get";
    public static final String ALL = "all";

    public void release() {
        super.release();
        includeParams = null;
        value = null;
        stripParamName = null;
        encode = false;
        id = null;
        parameters = null;
        appendParamsAsPathParams = false;
    }

    public int doStartTag() throws JspException {
        // bl: can't count on release being called (for whatever reason), so instead, reset the parameter
        // map in the start tag each time.
        parameters = new LinkedHashMap<String, Collection<String>>();
        return super.doStartTag();
    }

    public int doEndTag() throws JspException {

        Writer writer = pageContext.getOut();

        HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
        HttpServletResponse resp = (HttpServletResponse) pageContext.getResponse();
        Map parameters = new LinkedHashMap();

        if (!IPStringUtil.isEmpty(stripParamName)) {
            stripParamName = Pattern.quote(stripParamName);
            // strip out path params
            value = value.replaceAll("\\/" + stripParamName + "\\/[^\\/\\?]+", "");
            // strip out query args after a &
            value = value.replaceAll("\\&" + stripParamName + "\\=[^\\&]*", "");
            // strip out query args after a ?
            value = value.replaceAll("\\?" + stripParamName + "\\=[^\\&]*", "?");
        }

        if (appendParamsAsPathParams) {
            value = IPHTMLUtil.getParametersCollectionAsPathParametersInUrl(value, this.parameters);
        } else {
            addParameterValueCollectionsToMap(parameters, this.parameters);
        }

        if ((includeParams == null && value == null) || GET.equalsIgnoreCase(includeParams)) {
            // Parse the query string to make sure that the parameters come from the query, and not some posted data
            String query = req.getQueryString();

            if (query != null) {
                // Remove possible #foobar suffix
                int idx = query.lastIndexOf('#');

                if (idx != -1) {
                    query = query.substring(0, idx - 1);
                }

                addParameterValueCollectionsToMap(parameters, IPHTMLUtil.parseQueryString(query));
            }
        } else if (ALL.equalsIgnoreCase(includeParams)) {
            parameters.putAll(ActionContext.getContext().getParameters());
        } else if (value == null && !NONE.equalsIgnoreCase(includeParams)) {
            throw UnexpectedError.getRuntimeException("Unknown value for includeParams parameter to URL tag: " + includeParams);
        }

        String result = buildUrl(value, resp, parameters, encode);

        // bl: changed so that we either set the id attribute or else we write out the URL to the body.
        if (!isEmpty(id)) {
            // bl: used to set the variable as a Request attribute, but page context variables take precedence over
            // those. thus, we should set these variables directly on the PageContext instead.
            // NOTE: we don't currently support a scope attribute, so the assumption will be that the
            // variable set with the specified id is always limited to page scope.
            //req.setAttribute(id, result);
            pageContext.setAttribute(id, result, PageContext.PAGE_SCOPE);
        } else {
            try {
                writer.write(result.trim());
            } catch (IOException e) {
                throw new NarrativeException("IOError: " + e.getMessage(), e);
            }
        }

        return EVAL_PAGE;

    }

    private void addParameterValueCollectionsToMap(Map parameters, Map<String, Collection<String>> parametersAsCollection) {
        // bl: supporting nested params, but need the values to be String[] in the parameters Map passed to
        // the Struts utility method.
        for (Map.Entry<String, Collection<String>> entry : parametersAsCollection.entrySet()) {
            parameters.put(entry.getKey(), entry.getValue().toArray(new String[]{}));
        }
    }

    public void addParameter(String paramName, String paramValue) {
        Collection<String> paramValues = parameters.get(paramName);
        if (paramValues == null) {
            parameters.put(paramName, paramValues = new LinkedList<String>());
        }
        paramValues.add(paramValue);
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setStripParamName(String stripParamName) {
        this.stripParamName = stripParamName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIncludeParams(String includeParams) {
        this.includeParams = includeParams;
    }

    public void setEncode(boolean encode) {
        this.encode = encode;
    }

    public void setAppendParamsAsPathParams(boolean appendParamsAsPathParams) {
        this.appendParamsAsPathParams = appendParamsAsPathParams;
    }

    public static String buildUrl(String baseUrl, HttpServletResponse response, Map params, boolean encodeResult) {
        String result = IPHTMLUtil.getParametersAsURL(baseUrl, params);

        if (encodeResult) {
            try {
                result = response.encodeURL(result);
            } catch (Exception ex) {
                // Could not encode the URL for some reason
                // Use it unchanged
            }
        }

        return result;
    }
}
