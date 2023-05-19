package library.util;

import java.util.Map;

public class JsonUtil {
    public static Integer getIntFromJson(String key, Map<String, String> json) {
        if (json.containsKey(key)) {
            return json.get(key) == null ? null : Integer.valueOf(json.get(key));
        } else {
            return null;
        }
    }

    public static Long getLongFromJson(String key, Map<String, String> json) {
        if (json.containsKey(key)) {
            return json.get(key) == null ? null : Long.valueOf(json.get(key));
        } else {
            return null;
        }
    }

    public static Boolean getBoolFromJson(String key, Map<String, String> json) {
        if (json.containsKey(key)) {
            return json.get(key) == null ? null : Boolean.valueOf(json.get(key));
        } else {
            return null;
        }
    }
}
