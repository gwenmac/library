package library.util;

import com.ibm.icu.text.Transliterator;

public class JapaneseUtil {

    private static final Transliterator TRANSLITERATOR =
            Transliterator.getInstance("Any-Latin; Latin-ASCII");

    // Checks if a string contains any Japanese characters (hiragana, katakana, or kanji)
    public static boolean containsJapanese(String text) {
        if (text == null) return false;
        for (char c : text.toCharArray()) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
            if (block == Character.UnicodeBlock.HIRAGANA ||
                    block == Character.UnicodeBlock.KATAKANA ||
                    block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
                return true;
            }
        }
        return false;
    }

    public static String toRomaji(String text) {
        return TRANSLITERATOR.transliterate(text);
    }
}