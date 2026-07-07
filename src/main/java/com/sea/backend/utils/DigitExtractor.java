package com.sea.backend.utils;

public final class DigitExtractor {

    private DigitExtractor() {
    }

    public static String onlyDigits(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\D", "");
    }
}
