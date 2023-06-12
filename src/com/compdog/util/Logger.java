package com.compdog.util;

import java.time.Instant;
import java.util.Date;

public class Logger {
    public static Level getTargetLevel() {
        return targetLevel;
    }

    public static void setTargetLevel(Level targetLevel) {
        Logger.targetLevel = targetLevel;
    }

    public enum Level {
        ALL(6),
        TRACE(5),
        DEBUG(4),
        INFO(3),
        WARNING(2),
        ERROR(1),
        OFF(0);

        private final int weight;

        Level(int weight) {
            this.weight = weight;
        }

        public int compare(Level other) {
            return Integer.compare(this.weight, other.weight);
        }

        @Override
        public String toString() {
            switch (this) {
                case ALL:
                    return "ALL";
                case TRACE:
                    return "TRACE";
                case DEBUG:
                    return "DEBUG";
                case INFO:
                    return "INFO";
                case WARNING:
                    return "WARNING";
                case ERROR:
                    return "ERROR";
                case OFF:
                    return "OFF";
                default:
                    return "UNKNOWN";
            }
        }
    }

    private static Level targetLevel = Level.ALL;

    private final String name;

    private Logger(String name) {
        this.name = name;
    }

    public static Logger getLogger(String name) {
        return new Logger(name);
    }

    public String getName() {
        return name;
    }

    public boolean isLoggable(Level level) {
        return level.compare(getTargetLevel()) <= 0; // less than or equal weight
    }

    public void log(Level level, String message) {
        if (isLoggable(level)) {
            StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
            StackTraceElement e = stacktrace[2];
            String methodName = e.getMethodName();
            System.out.println(Date.from(Instant.now()).toString() + " [" + methodName + "] " + level + " " + name + ": " + message);
        }
    }
}
