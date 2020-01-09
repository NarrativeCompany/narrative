package org.narrative.common.web.jsp;

import org.narrative.common.util.UnexpectedError;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: May 1, 2006
 * Time: 11:04:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class InternalServletRequest implements HttpServletRequest {

    private final String servletPath;
    private final Locale locale;
    private final Vector locales = new Vector();
    private final Map<String, Object> attributes;
    private final Map<String, String> parameters;
    private final HttpSession session;

    public InternalServletRequest(String servletPath, Locale locale, HttpSession session, Map<String, Object> attributes, Map<String, String> parameters) {
        this.servletPath = servletPath;
        this.locale = locale;
        this.locales.add(locale);
        this.attributes = attributes;
        this.parameters = parameters;
        this.session = session;
    }

    public String getAuthType() {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public Cookie[] getCookies() {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public long getDateHeader(String string) {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public String getHeader(String string) {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public Enumeration getHeaders(String string) {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public Enumeration getHeaderNames() {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public int getIntHeader(String string) {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public String getMethod() {
        // bl: Jasper in Tomcat 8 apparently requires this to ensure "JSPs only permit GET POST or HEAD"
        return "GET";
    }

    public String getPathInfo() {
        return null;
    }

    public String getPathTranslated() {
        return null;
    }

    public String getContextPath() {
        return "";
    }

    public String getQueryString() {
        return "";
    }

    public String getRemoteUser() {
        return "";
    }

    public boolean isUserInRole(String string) {
        return false;
    }

    public Principal getUserPrincipal() {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public String getRequestedSessionId() {
        return "";
    }

    public String getRequestURI() {
        return "";
    }

    public StringBuffer getRequestURL() {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public String getServletPath() {
        return servletPath;
    }

    public HttpSession getSession(boolean b) {
        return session;
    }

    public HttpSession getSession() {
        return session;
    }

    public boolean isRequestedSessionIdValid() {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public boolean isRequestedSessionIdFromCookie() {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public boolean isRequestedSessionIdFromURL() {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public boolean isRequestedSessionIdFromUrl() {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public Object getAttribute(String string) {
        return attributes.get(string);
    }

    public Enumeration getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    public String getCharacterEncoding() {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public void setCharacterEncoding(String string) throws UnsupportedEncodingException {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public int getContentLength() {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public String getContentType() {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public ServletInputStream getInputStream() throws IOException {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public String getParameter(String string) {
        return parameters.get(string);
    }

    public Enumeration getParameterNames() {
        return Collections.enumeration(parameters.keySet());
    }

    public String[] getParameterValues(String string) {
        return parameters.values().toArray(new String[]{});
    }

    public Map getParameterMap() {
        return parameters;
    }

    public String getProtocol() {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public String getScheme() {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public String getServerName() {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public int getServerPort() {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public BufferedReader getReader() throws IOException {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public String getRemoteAddr() {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public String getRemoteHost() {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public void setAttribute(String string, Object object) {
        attributes.put(string, object);
    }

    public void removeAttribute(String string) {
        attributes.remove(string);
    }

    public Locale getLocale() {
        return locale;
    }

    public Enumeration getLocales() {
        return locales.elements();
    }

    public boolean isSecure() {
        return false;
    }

    public RequestDispatcher getRequestDispatcher(String string) {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public String getRealPath(String string) {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public int getRemotePort() {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public String getLocalName() {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public String getLocalAddr() {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public int getLocalPort() {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    @Override
    public String changeSessionId() {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    @Override
    public void login(String username, String password) throws ServletException {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    @Override
    public void logout() throws ServletException {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> httpUpgradeHandlerClass) throws IOException, ServletException {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    @Override
    public long getContentLengthLong() {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    @Override
    public ServletContext getServletContext() {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    @Override
    public DispatcherType getDispatcherType() {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }
}
