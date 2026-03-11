package com.theodo.springblueprint.testhelpers.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public interface StringUtils {
    static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
