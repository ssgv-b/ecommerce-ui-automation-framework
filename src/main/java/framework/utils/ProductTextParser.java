package framework.utils;

import java.math.BigDecimal;

public class ProductTextParser {

    private ProductTextParser() {}

    private static final String CURRENCY_PREFIX = "Rs.";

    public static BigDecimal parsePrice(String priceValue) {
        return new BigDecimal(priceValue.replace(CURRENCY_PREFIX, "").trim());
    }

    public static String parseTextAndTrim(String inputString) {
        // If multiline (bold label format), grab everything after the first newline
        if (inputString.contains("\n")) {
            return inputString.replaceAll("^.*:\\n\\s*", "").trim();
        }
        // Otherwise just strip "Word: " prefix
        return inputString.replaceAll("^\\w+:\\s+", "").trim();
    }
}
