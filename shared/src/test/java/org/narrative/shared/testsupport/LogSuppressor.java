package org.narrative.shared.testsupport;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LogSuppressor {
    private final Map<Class, Level> previousLevelMap = new ConcurrentHashMap<>();

    public void setLevel(Level level, Class...classes) {
        for (Class clazz : classes) {
            Logger logger = (Logger) LoggerFactory.getLogger(clazz);
            if (logger.getLevel() != null) {
                previousLevelMap.putIfAbsent(clazz, logger.getLevel());
            }
            logger.setLevel(level);
        }
    }

    public void suppressLogs(Class...classes) {
        setLevel(Level.OFF, classes);
    }

    public void resumeLogs(Class...classes) {
        for (Class clazz : classes) {
            Logger logger = (Logger) LoggerFactory.getLogger(clazz);
            Level prevLevel = previousLevelMap.get(clazz);
            logger.setLevel(prevLevel);
            previousLevelMap.remove(clazz);
        }
    }
}
