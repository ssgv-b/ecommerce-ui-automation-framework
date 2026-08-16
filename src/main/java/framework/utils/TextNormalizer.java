package framework.utils;

import java.util.Locale;

public final class TextNormalizer {

    public static String normalizeText(String rawText) {
        if (rawText == null) {
            return "";
        }
        return rawText.replace("\n", " ").replaceAll("\\([^)]*\\)", "").trim().toLowerCase(Locale.ROOT);
    }

    public static String safeTrim(String rawText) {
        if (rawText == null) {
            return "";
        }
        return rawText.trim();
    }
}
