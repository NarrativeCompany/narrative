package org.narrative.network.shared.struts;

import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.processes.TraceProcessHistory;
import org.narrative.common.util.trace.TraceItem;
import org.narrative.common.util.trace.TraceManager;
import org.narrative.common.web.RequestResponseHandler;
import org.narrative.network.core.statistics.StatisticManager;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.util.TextParseUtil;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.struts2.dispatcher.StreamResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Feb 20, 2006
 * Time: 10:04:58 AM
 */
public class NetworkStreamResult extends StreamResult {

    private String zipOutput;
    private String filename;

    public void setZipOutput(String zipOutput) {
        this.zipOutput = zipOutput;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * set the contentLengthForStream variable.  this variable will only ever be set via result config
     * parameters in the struts xml for network-stream results.  it should be set to an ognl expression,
     * e.g. ${contentLength} that will be evaluated against the ognl stack.  the result of that evaluation
     * should be a parseable integer and will be set on the StreamResult as the contentLength to be used
     * for the stream to the client.
     *
     * @param length an ognl string to evaluate against the stack whose value should be used for the content length.
     */
    public void setContentLengthForStream(String length) {
        setContentLength(TextParseUtil.translateVariables(length, ActionContext.getContext().getValueStack()));
    }

    public void execute(ActionInvocation invocation) throws Exception {
        TraceItem ti = null;
        if (TraceManager.isTracing()) {
            ti = TraceManager.startTrace(new TraceProcessHistory("file-stream"));
        }
        try {
            super.execute(invocation);
        } finally {
            if (ti != null) {
                TraceManager.endTrace(ti);
            }
        }
    }

    /**
     * bl: need to override doExecute so that we can add zip compression of output files
     * to prevent an IE security vulnerability with opening certain files containing HTML
     * directly in the browser.
     *
     * @param finalLocation the location
     * @param invocation    the invocation
     * @throws Exception if a problem occurs
     */
    protected void doExecute(String finalLocation, ActionInvocation invocation) throws Exception {

        InputStream oInput = null;
        OutputStream oOutput = null;

        try {
            // Find the inputstream from the invocation variable stack
            oInput = (InputStream) invocation.getStack().findValue(conditionalParse(inputName, invocation));

            if (oInput == null) {
                String msg = ("Can not find a java.io.InputStream with the name [" + inputName + "] in the invocation stack. " + "Check the <param name=\"inputName\"> tag specified for this action.");
                LOG.error(msg);
                throw new IllegalArgumentException(msg);
            }

            // Find the Response in context
            HttpServletResponse oResponse = (HttpServletResponse) invocation.getInvocationContext().get(HTTP_RESPONSE);

            HttpServletRequest oRequest = (HttpServletRequest) invocation.getInvocationContext().get(HTTP_REQUEST);

            final boolean isZipOutput = Boolean.valueOf(conditionalParse(zipOutput, invocation));
            // Set the content length
            if (isZipOutput) {
                // don't know the content length, so don't set the content length header
                contentLength = null;
                contentDisposition = "application/zip";
            }

            // Set the content-disposition
            if (contentDisposition != null) {
                String disposition = conditionalParse(contentDisposition, invocation);
                oResponse.addHeader("Content-disposition", disposition);
            }

            int contentLengthAsInt = -1;
            if (contentLength != null) {
                String contentLengthParsed = conditionalParse(contentLength, invocation);
                try {
                    contentLengthAsInt = Integer.parseInt(contentLengthParsed);
                } catch (NumberFormatException e) {
                    LOG.warn("failed to recognize " + contentLengthParsed + " as a number, contentLength header will not be set", e);
                }
            }

            RequestResponseHandler reqResp = networkContext().getReqResp();
            reqResp.parseRange(contentLengthAsInt);

            final boolean useBytesRange = reqResp.isUseBytesRange();
            if (useBytesRange) {
                if (isZipOutput) {
                    throw UnexpectedError.getRuntimeException("Can't use byte range if zipping output!");
                }
                if (contentLengthAsInt < 0) {
                    throw UnexpectedError.getRuntimeException("Can't use byte range if content length is unknown!");
                }
                oResponse.setContentLength(reqResp.getRangeContentLength());
            } else if (contentLengthAsInt >= 0) {
                oResponse.setContentLength(contentLengthAsInt);
            }

            // Set the content type
            oResponse.setContentType(conditionalParse(contentType, invocation));

            oResponse.setHeader("Accept-Ranges", "bytes");

            // Get the outputstream
            oOutput = oResponse.getOutputStream();

            if (isZipOutput) {
                ZipOutputStream zipOut = new ZipOutputStream(oOutput);
                zipOut.putNextEntry(new ZipEntry(conditionalParse(filename, invocation)));
                oOutput = zipOut;
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Streaming result [" + inputName + "] type=[" + contentType + "] length=[" + contentLength + "] content-disposition=[" + contentDisposition + "] isZipOutput=[" + isZipOutput + "]");
            }

            try {
                LOG.debug("Streaming to output buffer +++ START +++");
                if (!useBytesRange) {
                    // Copy input to output
                    byte[] oBuff = new byte[bufferSize];
                    int iSize;
                    while (-1 != (iSize = oInput.read(oBuff))) {
                        oOutput.write(oBuff, 0, iSize);
                    }
                } else {
                    // Copy input to output
                    oResponse.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

                    try {
                        int bytesStart = reqResp.getRangeBytesStart();
                        int bytesEnd = reqResp.getRangeBytesEnd();
                        oResponse.setHeader("Content-Range", "bytes " + bytesStart + "-" + bytesEnd + "/" + contentLengthAsInt);

                        int bytesToRead = bytesEnd - bytesStart + 1;

                        final byte buffer[] = new byte[bufferSize];

                        int bytesToSkip = bytesStart;
                        while (bytesToRead > 0) {
                            int len = oInput.read(buffer);
                            // bl: len==-1 if there's no data left to read.
                            if (len < 0) {
                                break;
                            }
                            int offset = 0;
                            if (bytesToSkip > 0) {
                                if (bytesToSkip >= len) {
                                    bytesToSkip -= len;
                                    continue;
                                }
                                // bl: otherwise, we have a partial buffer to skip and partial to use.
                                offset = bytesToSkip;
                                len -= bytesToSkip;
                                bytesToSkip = 0;
                            }
                            if (len > 0) {
                                oOutput.write(buffer, offset, len);
                                if (bytesToRead >= len) {
                                    bytesToRead -= len;
                                } else {
                                    bytesToRead = 0;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        LOG.error("Error streaming file", ex);
                        StatisticManager.recordException(ex, false, reqResp);
                    }
                }
                LOG.debug("Streaming to output buffer +++ END +++");

                // Flush
                oOutput.flush();
            } catch (ClientAbortException ex) {
                // Client aborted... just swallow                                                      
            }

            if (isZipOutput) {
                ((ZipOutputStream) oOutput).closeEntry();
            }
        } finally {
            try {
                if (oInput != null) {
                    oInput.close();
                }
                if (oOutput != null) {
                    oOutput.close();
                }
            } catch (ClientAbortException ex) {
                // Client aborted... just swallow
            }
        }
    }
}
