import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static wordle.Constants.*;

public class Wordle {
    private final String chosenWord;
    private final int wordLength;
    private final int maxAttempts;
    private final Set<String> dictionarySet;
    private final List<String> dictionaryList;
    private Set<String> alreadyGuessed;
    private int[] alphabetStates;
    private int[] puzzleCharCounts;
    private int remainingHints;
    private List<Character> availableHints;
    private Set<Character> revealedHints = new TreeSet<>();

    private Wordle(String wordLength, String maxAttempts, String chosenWord) {
        this.wordLength = parseOrDefault(wordLength, chosenWord.isBlank() ? DEFAULT_WORD_LENGTH : chosenWord.length());
        this.maxAttempts = parseOrDefault(maxAttempts, DEFAULT_MAX_ATTEMPTS);

        dictionarySet = loadDictionary(this.wordLength);
        dictionaryList = new ArrayList<>(dictionarySet);

        if (isChosenWordValid(chosenWord)) {
            this.chosenWord = chosenWord.toUpperCase();

            dictionarySet.add(this.chosenWord);

            // if (dictionarySet.add(this.chosenWord)) {
            //     dictionaryList.addLast(this.chosenWord); // remove for max performance? (due to ArrayList resizing) and since it isn't used after this point
            // }
        } else {
            this.chosenWord = getRandomWord();
        }

        availableHints = initializeHints(this.chosenWord);
        alreadyGuessed = HashSet.newHashSet(this.maxAttempts);
        puzzleCharCounts = new int[26];

        for (char letter : this.chosenWord.toCharArray()) {
            puzzleCharCounts[letter - 'A']++;
        }

        alphabetStates = new int[26];

        Arrays.fill(this.alphabetStates, DEFAULT_STATE);
    }

    private int parseOrDefault(String arg, int defaultValue) {
        try {
            int value = Integer.parseInt(arg);

            return value > 0 && value < 30 ? value : defaultValue; // 30 is the longest word in all_words.txt; too expensive to not hardcode it i think
        } catch (NumberFormatException _) {
            return defaultValue;
        }
    }

    private Set<String> loadDictionary(int wordLength) {
        try (Stream<String> stream = Files.lines(Path.of(WORD_FILE), StandardCharsets.UTF_8)) {
            return stream
                .map(String::toUpperCase)
                .filter(word -> word.length() == wordLength && word.chars().allMatch(Character::isLetter))
                .collect(Collectors.toCollection(HashSet::new));
        } catch (IOException _) {
            System.out.printf("%sNo %d-letter words in `%s`", RED_TEXT, wordLength, WORD_FILE);

            return Collections.emptySet();
        }
    }

    private boolean isChosenWordValid(String word) {
        return word != null && word.length() == wordLength && word.chars().allMatch(Character::isLetter);
    }

    private String getRandomWord() {
        return dictionaryList.get(RANDOM.nextInt(dictionaryList.size()));
    }

    private List<Character> initializeHints(String word) {
        int maxHints = (word.length() * 40) / 100;

        if (maxHints == 0) return new ArrayList<>();

        Set<Character> wordVowels = HashSet.newHashSet(wordLength);
        Set<Character> wordConsonants = HashSet.newHashSet(wordLength);

        for (char letter : word.toCharArray()) {
            if (VOWELS.contains(letter)) {
                wordVowels.add(letter);
            } else {
                wordConsonants.add(letter);
            }
        }

        List<Character> vowelList = new ArrayList<>(wordVowels);
        List<Character> consonantList = new ArrayList<>(wordConsonants);

        int vowelsNeeded = Math.min((maxHints / 2), vowelList.size());
        int consonantsNeeded = Math.min(maxHints - vowelsNeeded, consonantList.size());

        shuffleList(vowelList);
        shuffleList(consonantList);

        List<Character> finalSelection = new ArrayList<>(vowelsNeeded + consonantsNeeded);

        finalSelection.addAll(vowelList.subList(0, vowelsNeeded));
        finalSelection.addAll(consonantList.subList(0, consonantsNeeded));

        shuffleList(finalSelection);

        this.remainingHints = finalSelection.size();

        return finalSelection;
    }

    private <E> void shuffleList(List<E> list) {
        Collections.shuffle(list, RANDOM);
    }

    private void printRoundLabels(int attempt) {
        String hintsString = revealedHints.isEmpty() ? "None" : revealedHints.toString();

        String attemptStringSequence = "Attempt " + attempt + " of " + maxAttempts;
        String revealedHintStringSequence = "Revealed Hints: " + hintsString;
        String hintsLeftStringSequence = "Hints Left: " + remainingHints + " (Enter 'H')";
        int attemptSequenceLength = attemptStringSequence.length();
        int revealSequenceLength = revealedHintStringSequence.length();
        int hintSequenceLength = hintsLeftStringSequence.length();

        int maxLabelWidth = 100;
        int minimumContentWidth = attemptSequenceLength + revealSequenceLength + hintSequenceLength;
        int availableSpace = maxLabelWidth - minimumContentWidth;
        int paddingPerGap = availableSpace / 2;
        int totalSequenceLength1 = attemptSequenceLength + paddingPerGap;
        int totalSequenceLength2 = revealSequenceLength + paddingPerGap;
        int borderLength = totalSequenceLength1 + totalSequenceLength2 + hintSequenceLength;

        String borderString = "-".repeat(borderLength);

        System.out.printf(
            "%n%s%n%-" + totalSequenceLength1 + "s%-" + totalSequenceLength2 + "s%s%n%s%n%n",
            borderString,
            attemptStringSequence,
            revealedHintStringSequence,
            hintsLeftStringSequence,
            borderString
        );

        List<String> displayRows = new ArrayList<>(QWERTY_ROWS.length);

        for (String rowLetters : QWERTY_ROWS) {
            StringBuilder rowString = new StringBuilder();

            for (char letter : rowLetters.toCharArray()) {
                String color = getLetterColors(alphabetStates[letter - 'A']);

                rowString.append(color).append(" ").append(letter).append(" ").append(RESET_ANSI).append(" ");
            }

            displayRows.add(rowString.toString());
        }

        int maxRowWidth = displayRows.stream()
                .mapToInt(this::getVisibleLength)
                .max()
                .orElse(0);

        for (String row : displayRows) {
            int width = getVisibleLength(row);
            int padding = (maxRowWidth - width) / 2;

            System.out.print(" ".repeat(Math.max(0, padding)));
            System.out.println(row);
        }

        System.out.println();
    }

    private String getLetterColors(int state) {
        switch (state) {
            case EXACT_STATE: return GREEN_BACKGROUND;
            case PARTIAL_STATE: return YELLOW_BACKGROUND;
            case ABSENT_STATE: return GRAY_BACKGROUND;
            default: return BLACK_BACKGROUND;
        }
    }

    private int getVisibleLength(String string) {
        return string.replaceAll("\u001B\\[[;\\d]*m", "").length();
    }

    private String getValidGuess(Scanner scanner) {
        String guess;

        while (true) {
            String hintPrompt = remainingHints <= 0 ? "" : " (or 'H' for a hint)";

            System.out.printf("%sEnter a real %d-letter word%s%s: ", BOLD_TEXT, wordLength, RESET_ANSI, hintPrompt);

            guess = scanner.nextLine().toUpperCase();

            if (guess.equals("H")) {
                revealHint();
                
                continue;
            } else if (guess.equals("QUIT")) {
                System.exit(0);

                return "";
            }

            if (guess.length() != wordLength) {
                System.out.printf("%sError: Word must be exactly %d letters.%s%n", RED_TEXT, wordLength, RESET_ANSI);
            } else if (!dictionarySet.contains(guess)) {
                System.out.printf("%sError: Not a valid word.%s%n", RED_TEXT, RESET_ANSI);
            } else if (!alreadyGuessed.add(guess)) {
                System.out.printf("%sError: You already guessed that word.%s%n", RED_TEXT, RESET_ANSI);
            } else {
                return guess;
            }
        }
    }

    private void revealHint() {
        if (remainingHints <= 0 || availableHints.isEmpty()) {
            System.out.printf("%sError: No hints remaining.%s%n", RED_TEXT, RESET_ANSI);

            return;
        }

        char hint = availableHints.removeLast();
        remainingHints--;

        revealedHints.add(hint);

        System.out.printf("%n%s Hint Revealed: The word contains the letter %s'%c' %s%n%n", BLACK_BACKGROUND, PURPLE_TEXT, hint, RESET_ANSI);
    }

    private String evaluateGuess(String guess) {
        int[] localCounts = puzzleCharCounts.clone();
        String[] colorResult = new String[wordLength];
        
        for (int i = 0; i < wordLength; i++) {
            char c = guess.charAt(i);
            int index = c - 'A';

            if (c == chosenWord.charAt(i)) {
                colorResult[i] = GREEN_BACKGROUND;
                localCounts[index]--; 
                
                updateLetterState(c, EXACT_STATE);
            }
        }

        for (int i = 0; i < wordLength; i++) {
            if (colorResult[i] != null) continue;

            char c = guess.charAt(i);
            int index = c - 'A';

            if (localCounts[index] > 0) {
                colorResult[i] = YELLOW_BACKGROUND;
                localCounts[index]--;
                
                updateLetterState(c, PARTIAL_STATE);
            } else {
                colorResult[i] = GRAY_BACKGROUND;
                
                updateLetterState(c, ABSENT_STATE);
            }
        }

        StringBuilder stringResult = new StringBuilder();
        
        for (int i = 0; i < wordLength; i++) {
            stringResult.append(colorResult[i]).append(BLACK_TEXT).append(" ").append(guess.charAt(i)).append(" ").append(RESET_ANSI);
        }

        return stringResult.toString();
    }

    private void updateLetterState(char letter, int newState) {
        int index = letter - 'A';

        if (newState > alphabetStates[index]) {
            alphabetStates[index] = newState;
        }
    }

    private void play() {
        Scanner inputScanner = new Scanner(System.in);
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            printRoundLabels(attempt);

            String guess = getValidGuess(inputScanner);
            String result = evaluateGuess(guess);
            
            System.out.printf("%n%s%n", result);

            if (guess.equals(chosenWord)) {
                System.out.printf("%nCongratulations! You guessed the word in %d attempts.%n%n", attempt);
                inputScanner.close();

                return;
            }
        }

        String displayPuzzle = chosenWord.replace("", " ").trim();

        System.out.printf("%nBetter luck next time...%nThe word was: %s%s%s %s %s%n%n", BLACK_BACKGROUND, RED_TEXT, BOLD_TEXT, displayPuzzle, RESET_ANSI);
        inputScanner.close();
    }

    public static void main(String[] args) {
        String chosenLength = args.length > 0 ? args[0] : "";
        String chosenAttempts = args.length > 1 ? args[1] : "";
        String desiredWord = args.length > 2 ? args[2] : "";

        try {
            Wordle game = new Wordle(chosenLength, chosenAttempts, desiredWord);

            game.play();
        } catch (Exception exception) {
            System.out.println("Game crashed: " + exception.getMessage());
        }
    }
}
