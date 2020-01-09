package org.narrative.network.core.system;

import org.narrative.common.util.IPDateUtil;
import org.narrative.network.shared.util.NetworkLogger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Sep 19, 2006
 * Time: 3:57:24 PM
 * To change this template use File | Settings | File Templates.
 */
public enum HeartbeatServer {
    INSTANCE,
    DIRECT_SERVLET;

    private static final NetworkLogger logger = new NetworkLogger(HeartbeatServer.class);

    private long lastPing = 0L;

    private Thread heartbeatThread;
    private Selector selector = null;

    private volatile int port;
    private volatile boolean isRunning = false;

    public void startServer(int port) {
        if (isRunning) {
            return;
        }

        this.port = port;
        heartbeatThread = new Thread(this::serverLoop);
        heartbeatThread.setName("HeartbeatServerThread");
        heartbeatThread.start();
    }

    public void stopServerSafely() {
        try {
            stopServer();
        } catch (Throwable t) {
            logger.error("Failed shutting down HeartbeatServer", t);
        }
    }

    public void stopServer() {
        // bl: nothing to do if it's not running.
        if (!isRunning) {
            return;
        }
        if (logger.isInfoEnabled()) {
            logger.info("Stopping heartbeat server");
        }
        // bl: in order to avoid a race condition, get a reference to the selector in advance of setting isRunning
        // to false.  this should ensure that we get the proper selector so we can attempt to wake it up.
        Selector iSelector = selector;
        isRunning = false;
        // bl: once we set isRunning to false, let's wakeup the selector to ensure it shuts down the heartbeat server immediately.
        // bl: only wakeup the selector if it actually was found.
        if (iSelector != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Waking up heartbeat server selector");
            }
            iSelector.wakeup();
        }
        // bl: just wait for the heartbeat thread to finish executing
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Calling join on heartbeat server thread");
            }
            heartbeatThread.join();
        } catch (InterruptedException e) {
            logger.error("Error shutting down heartbeat server", e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Heartbeat server should be shut down");
        }
        assert !heartbeatThread.isAlive() : "heartbeatThread should be dead!";
        assert selector == null : "Selector should have been cleared after heartbeat server shutdown!";
        heartbeatThread = null;
        if (logger.isInfoEnabled()) {
            logger.info("Heartbeat server shut down. lastPing/" + lastPing);
        }
    }

    private void serverLoop() {
        ServerSocketChannel serverChannel;

        //start up the heartbeat server
        int serverStartupErrorCount = 0;
        while (true) {
            try {
                //set up the server socket and selector
                selector = Selector.open();
                serverChannel = ServerSocketChannel.open();
                serverChannel.configureBlocking(false);
                serverChannel.socket().bind(new InetSocketAddress(port));
                serverChannel.register(selector, serverChannel.validOps());
                break;
            } catch (Throwable e) {
                serverStartupErrorCount++;
                if (serverStartupErrorCount > 5) {
                    logger.error("Too many failures starting heartbeat server.  Quitting.", e);
                    return;
                }

                logger.error("Error starting heartbeat server.  Retrying...", e);
            }
        }

        isRunning = true;
        int badSelectCount = 0;

        while (isRunning) {
            try {
                //main server loop
                while (isRunning && selector.select(IPDateUtil.SECOND_IN_MS) > 0) {
                    lastPing = System.currentTimeMillis();
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> it = keys.iterator();

                    //loop through all selection keys
                    while (it.hasNext()) {
                        try {
                            SelectionKey key = it.next();
                            it.remove();

                            //new connection
                            if (key.isAcceptable()) {
                                ServerSocketChannel instanceChannel = (ServerSocketChannel) key.channel();
                                instanceChannel.accept().close();
                            }
                        } catch (CancelledKeyException cce) {
                            if (logger.isWarnEnabled()) {
                                logger.warn("Key was canceled.  Ignoring and moving to next one", cce);
                            }
                        }
                    }

                    if (!isRunning) {
                        break;
                    }
                }
            } catch (Exception e) {
                badSelectCount++;
                if (badSelectCount > 5) {
                    isRunning = false;
                    logger.error("Failed too many times doing select count.  Restarting server...", e);
                    break;
                }
                if (logger.isWarnEnabled()) {
                    logger.warn("Error doing select.  Ignoring and trying again.", e);
                }
            }
        }
        try {
            if (serverChannel != null) {
                serverChannel.socket().close();
                serverChannel.close();
            }

            if (selector != null) {
                selector.close();
                selector = null;
            }
        } catch (IOException e) {
            logger.error("Unable to close server resources", e);
        } finally {
            if (logger.isInfoEnabled()) {
                logger.info("heartbeat server shut down");
            }
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public int getPort() {
        return port;
    }

    public long getLastPing() {
        return lastPing;
    }
}
