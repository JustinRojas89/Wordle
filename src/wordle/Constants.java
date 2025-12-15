package wordle;

import java.util.Random;
import java.util.Set;

public final class Constants {
    private Constants() {}

    public static final String WORD_FILE = "src\\all_words.txt";
    public static final int DEFAULT_WORD_LENGTH = 5;
    public static final int DEFAULT_MAX_ATTEMPTS = 6;
    public static final Random RANDOM = new Random();

    public static final String RESET_ANSI = "\u001B[0m";
    public static final String BOLD_TEXT = "\u001B[1m";
    public static final String GREEN_BACKGROUND = "\u001B[42m";
    public static final String YELLOW_BACKGROUND = "\u001B[43m";
    public static final String GRAY_BACKGROUND = "\u001B[100m";
    public static final String BLACK_BACKGROUND = "\u001B[40m";
    public static final String RED_TEXT = "\u001B[31m";
    public static final String PURPLE_TEXT = "\u001B[1;95m";
    public static final String BLACK_TEXT = "\u001b[1;90m";

    public static final int DEFAULT_STATE = 0;
    public static final int ABSENT_STATE = 1;
    public static final int PARTIAL_STATE = 2;
    public static final int EXACT_STATE = 3;
    public static final Set<Character> VOWELS = Set.of( 'A', 'E', 'I', 'O', 'U' );
    public static final String[] QWERTY_ROWS = { "QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM" };
}
