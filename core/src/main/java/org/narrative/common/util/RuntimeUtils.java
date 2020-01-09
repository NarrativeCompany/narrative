package org.narrative.common.util;

import org.narrative.common.persistence.ObjectQuadruplet;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Aug 25, 2006
 * Time: 11:11:21 AM
 */
public class RuntimeUtils {

    // todo: stop using common ThreadBucket here and instead use new network ThreadBucket
    private static final ThreadBucket inputHandlers = new ThreadBucket("RuntimeUtils.inputHandlers", 4, 4);

    private static final NarrativeLogger logger = new NarrativeLogger(RuntimeUtils.class);

    /**
     * Tries to exec the command, waits for it to finish, logs errors if exit
     * status is nonzero, and returns true if exit status is 0 (success).
     *
     * @param command Description of the Parameter
     * @return Triplet of success, stdout, stderr
     */
    public static ObjectQuadruplet<Boolean, String, String, Integer> exec(List<String> command) {
        return exec(null, command, new Options());
    }

    public static ObjectQuadruplet<Boolean, String, String, Integer> exec(String command) {
        return exec(command, null, new Options());
    }

    public static ObjectQuadruplet<Boolean, String, String, Integer> exec(String command, Options options) {
        return exec(command, null, options);
    }

    public static ObjectQuadruplet<Boolean, String, String, Integer> exec(List<String> command, Options options) {
        return exec(null, command, options);
    }

    private static ObjectQuadruplet<Boolean, String, String, Integer> exec(String commandStr, List<String> command, Options options) {
        Process proc;
        String stdout = null;
        String stderr = null;
        InputStream stdoutIs = null;
        InputStream stderrIs = null;
        InputHandler errorHandler = null;
        InputHandler outputHandler = null;

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Trying to execute command " + IPStringUtil.getSeparatedList(command, " "));
            }
            if (!IPStringUtil.isEmpty(commandStr)) {
                proc = Runtime.getRuntime().exec(commandStr);
            } else {
                proc = Runtime.getRuntime().exec(command.toArray(new String[]{}));
            }

            if (options.captureOutputOnSeparateThreads) {
                errorHandler = new InputHandler(proc.getErrorStream());
                outputHandler = new InputHandler(proc.getInputStream());
                synchronized (inputHandlers) {
                    // bl: for RuntimeUtils, when capturing output on a separate thread, we actually want to block
                    // until there is an available thread on which we can capture stdout and stderr.
                    inputHandlers.addRunnableAndWaitIfFull(errorHandler);
                    inputHandlers.addRunnableAndWaitIfFull(outputHandler);
                }
            } else {
                // bl: if the outputStream was specified, then we don't want to try to steal the output.
                if (options.outputStream == null) {
                    stdoutIs = new BufferedInputStream(proc.getInputStream());
                }
                stderrIs = new BufferedInputStream(proc.getErrorStream());
            }

            if (options.inputStream != null) {
                OutputStream os = proc.getOutputStream();
                int ch;
                while ((ch = options.inputStream.read()) != -1) {
                    os.write(ch);
                }
                options.inputStream.close();
                os.close();
            } else if (options.outputStream != null) {
                InputStream is = new BufferedInputStream(proc.getInputStream());
                try {
                    int ch;
                    while ((ch = is.read()) != -1) {
                        options.outputStream.write(ch);
                    }
                } finally {
                    options.outputStream.close();
                    is.close();
                }
            }
        } catch (IOException e) {
            if (options.captureOutputOnSeparateThreads) {
                try {
                    int waitCount = 0;
                    while (errorHandler != null && (!errorHandler.isDone() || !outputHandler.isDone())) {
                        // don't wait more than 100ms.
                        if (waitCount > 100) {
                            break;
                        }
                        synchronized (errorHandler) {
                            errorHandler.wait(1);
                            waitCount++;
                        }
                    }
                } catch (InterruptedException ie) {
                    throw UnexpectedError.getRuntimeException("Unable to finish process.  Output thread never finished.", ie);
                }
                if (outputHandler != null) {
                    stdout = outputHandler.getOutput();
                }
                if (errorHandler != null) {
                    stderr = errorHandler.getOutput();
                }
            } else {
                if (stdoutIs != null) {
                    stdout = IPStringUtil.getStringFromInputStream(stdoutIs);
                }
                if (stderrIs != null) {
                    stderr = IPStringUtil.getStringFromInputStream(stderrIs);
                }
            }
            if (!options.dontLogErrors) {
                logger.error("IOException while trying to execute " + command + " output: \"" + stdout + "\" error: \"" + stderr + "\"", e);
            } else if (logger.isTraceEnabled()) {
                logger.trace("IOException while trying to execute " + command + " output: \"" + stdout + "\" error: \"" + stderr + "\"", e);
            }
            return new ObjectQuadruplet<>(false, stdout, stderr, null);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Got process object, waiting to return.");
        }

        int exitStatus;

        while (true) {
            try {
                exitStatus = proc.waitFor();
                break;
            } catch (InterruptedException e) {
                throw UnexpectedError.getRuntimeException("Thread interrupted while waiting for runtime process output!", e);
            }
        }
        if (exitStatus != 0) {
            if (!options.dontLogErrors) {
                logger.warn("Error executing command:" + exitStatus);
            }
        }

        if (options.captureOutputOnSeparateThreads) {
            try {
                while (!errorHandler.isDone() || !outputHandler.isDone()) {
                    synchronized (errorHandler) {
                        errorHandler.wait(1);
                    }
                }
            } catch (InterruptedException e) {
                throw UnexpectedError.getRuntimeException("Unable to finish process.  Output thread never finished.", e);
            }
            stdout = outputHandler.getOutput();
            stderr = errorHandler.getOutput();
        } else {
            stdout = IPStringUtil.getStringFromInputStream(proc.getInputStream());
            stderr = IPStringUtil.getStringFromInputStream(proc.getErrorStream());
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Successfully executed " + command + " output: \"" + stdout + "\" error: \"" + stderr + "\"");
        }

        return new ObjectQuadruplet<>(exitStatus == 0, stdout, stderr, exitStatus);
    }

    private static class InputHandler implements Runnable {
        private InputStream input;
        private StringBuilder output;
        private boolean done = false;

        public InputHandler(InputStream input) {
            this.input = input;
        }

        public void setInput(InputStream input) {
            this.input = input;
        }

        public void run() {
            output = new StringBuilder();
            try {
                int c;
                while ((c = input.read()) != -1) {
                    output.append((char) c);
                }
            } catch (IOException e) {
                throw UnexpectedError.getRuntimeException("Error reading input streams", e, true);
            } finally {
                done = true;
            }
        }

        public boolean isDone() {
            return done;
        }

        public String getOutput() {
            return output.toString();
        }
    }

    public static class Options {
        private boolean captureOutputOnSeparateThreads;
        private boolean dontLogErrors;
        private OutputStream outputStream;
        private InputStream inputStream;

        public Options() {
        }

        public Options(boolean dontLogErrors) {
            this.dontLogErrors = dontLogErrors;
        }

        public Options(boolean captureOutputOnSeparateThreads, boolean dontLogErrors) {
            this.captureOutputOnSeparateThreads = captureOutputOnSeparateThreads;
            this.dontLogErrors = dontLogErrors;
        }

        public Options(OutputStream outputStream) {
            this.outputStream = outputStream;
        }

        public Options(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public Options(boolean captureOutputOnSeparateThreads, boolean dontLogErrors, OutputStream outputStream, InputStream inputStream) {
            this.captureOutputOnSeparateThreads = captureOutputOnSeparateThreads;
            this.dontLogErrors = dontLogErrors;
            this.outputStream = outputStream;
            this.inputStream = inputStream;
        }

        public boolean isCaptureOutputOnSeparateThreads() {
            return captureOutputOnSeparateThreads;
        }

        public boolean isDontLogErrors() {
            return dontLogErrors;
        }

        public OutputStream getOutputStream() {
            return outputStream;
        }

        public InputStream getInputStream() {
            return inputStream;
        }
    }

}
