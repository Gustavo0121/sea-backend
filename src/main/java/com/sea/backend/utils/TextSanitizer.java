package com.sea.backend.utils;

import java.util.regex.Pattern;

public final class TextSanitizer {

    private static final Pattern ESPACOS_MULTIPLOS = Pattern.compile(" {2,}");
    private static final Pattern CARACTERES_PERIGOSOS = Pattern.compile("[<>\\p{Cntrl}]");

    private TextSanitizer() {
    }

    public static String collapseSpaces(String value) {
        if (value == null) {
            return null;
        }
        return ESPACOS_MULTIPLOS.matcher(value.trim()).replaceAll(" ");
    }

    /**
     * Strips characters with no legitimate use in address/name free text (angle brackets,
     * control chars) so stored values can never carry HTML/script fragments, then normalizes
     * whitespace. Constrained fields (e.g. nome) are already restricted by @Pattern; this is
     * the defense-in-depth pass for free-text fields without such a strict regex (endereco).
     */
    public static String sanitize(String value) {
        if (value == null) {
            return null;
        }
        String semCaracteresPerigosos = CARACTERES_PERIGOSOS.matcher(value).replaceAll("");
        return collapseSpaces(semCaracteresPerigosos);
    }
}
