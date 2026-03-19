package framework.utils;

import java.util.Locale;

public class AddressNormalizer {
    public static String parseAddressName(String name) {
        return name.replaceAll("^\\s*(?:[A-Za-z]+\\.|\\.)\\s+", "").trim().toLowerCase(Locale.ROOT);
    }

}
