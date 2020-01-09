package org.narrative.common.web.jsp;

import org.narrative.common.util.IPUtil;
import org.narrative.common.util.UnexpectedError;
import org.apache.catalina.Globals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: May 1, 2006
 * Time: 3:02:19 PM
 * To change this template use File | Settings | File Templates.
 */
@Component
public class InternalJSPRunner {

    private final ServletContext ctx;

    @Autowired
    public InternalJSPRunner(ServletContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Evaluates the specified jsp page
     *
     * @param jspFile    The jsp file to process
     * @param attributes The attribute map the html page will use
     * @return
     */
    public String runJsp(String jspFile, Map<String, Object> attributes) {
        // bl: mirroring this from ApplicationHttpRequest. this is needed to get catalina to process the request.
        attributes.put(Globals.DISPATCHER_TYPE_ATTR, DispatcherType.REQUEST);
        InternalHttpSession session = new InternalHttpSession(ctx);
        InternalServletRequest req = new InternalServletRequest(jspFile, Locale.getDefault(), session, attributes, new HashMap<>());
        InternalServletResponse resp = new InternalServletResponse();

        try {
            RequestDispatcher requestDispatcher = ctx.getRequestDispatcher(jspFile);
            if(requestDispatcher==null) {
                throw UnexpectedError.getRuntimeException("Failed to identify RequestDispatcher for path!");
            }
            requestDispatcher.forward(req, resp);
        } catch (Exception e) {
            throw UnexpectedError.getRuntimeException("Unable to run internal jsp page: " + jspFile, e, true);
        }
        resp.getPw().flush();
        try {
            return resp.getInternalServletOutputStream().getOut().toString(IPUtil.IANA_UTF8_ENCODING_NAME);
        } catch (UnsupportedEncodingException e) {
            throw UnexpectedError.getRuntimeException("Unsupported UTF-8 encoding when processing email!", e);
        }
    }
}
