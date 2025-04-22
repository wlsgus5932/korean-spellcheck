package com.korean.spellcheck.log;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

public class Log {
    public static final String PLUGIN_ID = "korean-spellcheck";

    // 싱글톤 인스턴스
    private static Log instance;

    private Log() {}

    public static Log getInstance() {
        if (instance == null) {
            instance = new Log();
        }
        return instance;
    }

    // 정보 로그
    public static void logInfo(String message) {
        Platform.getLog(Platform.getBundle(PLUGIN_ID)).log(new Status(IStatus.INFO, PLUGIN_ID, message));
    }

    // 오류 로그
    public static void logError(String message, Throwable t) {
        Platform.getLog(Platform.getBundle(PLUGIN_ID)).log(new Status(IStatus.ERROR, PLUGIN_ID, message, t));
    }
}
