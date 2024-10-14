package org.zjzWx.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

/**
 * Author: xiaozhou.qin
 * Date: 2023/5/30
 */
public class BizStackUtil {
    private static String GENERATED_FILENAME = "<generated>";
    private static String PROJECT_PACKAGE = "org.zjzWx";
    private static Boolean DEBUG_MODE = false;

    public static void setMode(Boolean mode) {
        // 只允许在启动类中设置该值
        if (null != mode) {
            BizStackUtil.DEBUG_MODE = mode;
        }
    }

    public static void setPackage(String projectPackage) {
        // 只允许在启动类中设置该值
        if (StringUtils.isNotBlank(projectPackage)) {
            if (BizStackUtil.PROJECT_PACKAGE != null) {
                // 该变量是全局配置, 只允许设置一次
                return;
            }
            BizStackUtil.PROJECT_PACKAGE = projectPackage;
        }
    }

    public static void stack(Logger log, Throwable e) {
        StringBuilder sb = new StringBuilder();
        sb.append("\r\n<===========================================================================================");
        internalStack(e, sb);
        sb.append("\r\n===========================================================================================>");
        log.error("{}", sb);
    }

    public static void stack(Logger log, Throwable e, String method, String requestUri) {
        StringBuilder sb = new StringBuilder();
        sb.append("\r\n<===========================================================================================");
        sb.append("\r\n").append("请求地址: 【").append(method).append(" ").append(requestUri).append("】");
        sb.append("\r\n--------------------------------------------------------------------------------------------");
        internalStack(e, sb);
        sb.append("\r\n===========================================================================================>");
        log.error("{}", sb);
    }

    private static void internalStack(Throwable e, StringBuilder sb) {
        sb.append("\r\n异常信息：");
        Throwable throwable = getDetailThrowable(e);
        sb.append("\r\n").append(e.getClass().getName()).append(": ").append(throwable.getMessage());
        sb.append("\r\n--------------------------------------------------------------------------------------------");
        sb.append("\r\n业务异常调用链：");
        addStack(throwable, sb);
    }

    private static Throwable getDetailThrowable(Throwable e) {
        if (e.getCause() != null) {
            return getDetailThrowable(e.getCause());
        } else {
            return e;
        }
    }

    private static void addStack(Throwable e, StringBuilder sb) {
//        if (e.getCause() != null) {
//            addStack(e.getCause(), sb);
//        }
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            if (stackTraceElement.getClassName().contains(PROJECT_PACKAGE) && !GENERATED_FILENAME.equals(stackTraceElement.getFileName())) {
                sb.append("\r\n").append(stackTraceElement.getClassName()).append(".").append(stackTraceElement.getMethodName())
                        .append("(").append(stackTraceElement.getFileName()).append(":").append(stackTraceElement.getLineNumber()).append(")");
            }
        }
    }
}
