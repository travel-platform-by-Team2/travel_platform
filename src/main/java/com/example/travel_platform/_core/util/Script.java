package com.example.travel_platform._core.util;

public class Script {

    public static String back(String msg) {
        return wrapScript(createAlertScript(msg) + "history.back();");
    }

    public static String href(String path, String msg) {
        return wrapScript(createAlertScript(msg) + "location.href='" + escapeJavaScriptValue(path) + "';");
    }

    private static String createAlertScript(String msg) {
        return "alert('" + escapeJavaScriptValue(msg) + "');";
    }

    private static String wrapScript(String scriptBody) {
        StringBuilder sb = new StringBuilder();
        sb.append("<script>");
        sb.append(scriptBody);
        sb.append("</script>");
        return sb.toString();
    }

    private static String escapeJavaScriptValue(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\r", "")
                .replace("\n", "\\n");
    }
}
