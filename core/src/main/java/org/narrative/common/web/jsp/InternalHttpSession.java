package org.narrative.common.web.jsp;

import org.narrative.common.util.UnexpectedError;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: May 1, 2006
 * Time: 2:15:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class InternalHttpSession implements HttpSession {
    private ServletContext servletContext;
    private Map<String, Object> attributes = new HashMap<String, Object>();
    private Map<String, Object> values = new HashMap<String, Object>();

    public InternalHttpSession(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public long getCreationTime() {
        return System.currentTimeMillis();
    }

    public String getId() {
        return "0";
    }

    public long getLastAccessedTime() {
        return System.currentTimeMillis();
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setMaxInactiveInterval(int interval) {

    }

    public int getMaxInactiveInterval() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public HttpSessionContext getSessionContext() {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public Enumeration getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    public void setAttribute(String name, Object object) {
        attributes.put(name, object);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public Object getValue(String name) {
        return values.get(name);
    }

    public String[] getValueNames() {
        return values.keySet().toArray(new String[]{});
    }

    public void putValue(String name, Object value) {
        values.put(name, value);
    }

    public void removeValue(String name) {
        values.remove(name);
    }

    public void invalidate() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isNew() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
