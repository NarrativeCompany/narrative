package org.narrative.common.web.jsp;

import org.narrative.common.util.IPUtil;
import org.narrative.common.util.UnexpectedError;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: May 1, 2006
 * Time: 11:04:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class InternalServletResponse implements HttpServletResponse {

    private Locale locale;
    private final InternalServletOutputStream os = new InternalServletOutputStream();
    private final PrintWriter pw;

    public InternalServletResponse() {
        try {
            pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(os, IPUtil.IANA_UTF8_ENCODING_NAME)), false);
        } catch (UnsupportedEncodingException e) {
            throw UnexpectedError.getRuntimeException("Unsupported UTF-8 encoding!", e);
        }
    }

    public void addCookie(Cookie cookie) {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public boolean containsHeader(String string) {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public String encodeURL(String string) {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public String encodeRedirectURL(String string) {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public String encodeUrl(String string) {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public String encodeRedirectUrl(String string) {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public void sendError(int i, String string) throws IOException {
        throw UnexpectedError.getRuntimeException("Error: + " + i + "\n" + string);
    }

    public void sendError(int i) throws IOException {
        throw UnexpectedError.getRuntimeException("Error :" + i);
    }

    public void sendRedirect(String string) throws IOException {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public void setDateHeader(String string, long l) {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public void addDateHeader(String string, long l) {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public void setHeader(String string, String string1) {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public void addHeader(String string, String string1) {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public void setIntHeader(String string, int i) {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public void addIntHeader(String string, int i) {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public void setStatus(int i) {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public void setStatus(int i, String string) {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public String getCharacterEncoding() {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public String getContentType() {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return os;
    }

    public PrintWriter getWriter() throws IOException {
        return pw;
    }

    public void setCharacterEncoding(String string) {
        throw UnexpectedError.getRuntimeException("Not Supported in internal requests");
    }

    public void setContentLength(int i) {

    }

    public void setContentType(String string) {

    }

    public void setBufferSize(int i) {

    }

    public int getBufferSize() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void flushBuffer() throws IOException {

    }

    public void resetBuffer() {

    }

    public boolean isCommitted() {
        return false;
    }

    public void reset() {

    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }

    @Override
    public int getStatus() {
        return HttpServletResponse.SC_OK;
    }

    @Override
    public String getHeader(String name) {
        return null;
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return Collections.emptySet();
    }

    @Override
    public Collection<String> getHeaderNames() {
        return Collections.emptySet();
    }

    @Override
    public void setContentLengthLong(long length) {
        throw UnexpectedError.getRuntimeException("Don't support setting contentLengthLong on InternalServletResponse!");
    }

    public InternalServletOutputStream getInternalServletOutputStream() {
        return os;
    }

    public PrintWriter getPw() {
        return pw;
    }
}
