package org.narrative.common.web.jsp;

import org.narrative.common.util.UnexpectedError;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: May 1, 2006
 * Time: 11:23:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class InternalServletOutputStream extends ServletOutputStream {
    private ByteArrayOutputStream out = new ByteArrayOutputStream();

    public void write(int b) throws IOException {
        out.write(b);
    }

    public ByteArrayOutputStream getOut() {
        return out;
    }

    @Override
    public boolean isReady() {
        // bl: we use a ByteArrayOutputStream, so it should always be ready
        return true;
    }

    @Override
    public void setWriteListener(WriteListener listener) {
        throw UnexpectedError.getRuntimeException("Don't support setting writeListener for InternalServletOutputStream!");
    }
}
