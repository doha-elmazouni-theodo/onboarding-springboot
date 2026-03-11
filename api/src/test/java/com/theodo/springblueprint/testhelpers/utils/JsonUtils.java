package com.theodo.springblueprint.testhelpers.utils;

import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.test.json.JsonAssert;
import org.springframework.test.json.JsonComparator;

public interface JsonUtils {
    JsonComparator jsonIgnoreArrayOrder = JsonAssert.comparator(JSONCompareMode.NON_EXTENSIBLE);
    JsonComparator jsonStrictArrayOrder = JsonAssert.comparator(JSONCompareMode.STRICT);

    static void assertIsMatchIgnoreArrayOrder(String expected, String actual) {
        jsonIgnoreArrayOrder.assertIsMatch(expected, actual);
    }

    static void assertIsMatchStrictArrayOrder(String expected, String actual) {
        jsonStrictArrayOrder.assertIsMatch(expected, actual);
    }
}
