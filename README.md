# Wordle
An optimized implementation of the Wordle game by the New York Times, but for your terminal.
- Support for custom game parameters (guessing words of a certain length, requiring a certain number of guesses, and choosing custom words for friends)
- Colorful text labels and output for a more immersive experience
- Play as many times as you want without a daily limit.

---

## Set Up

### Prerequisites

You need to have the Java Development Kit (JDK) installed on your system. This project was developed and tested using **Java 17+**.

### Compilation

1. Ensure `Constants.java` is in a package (named `wordle`) that is in the same directory as `Wordle.java`.
2. Open your terminal in the project directory and compile it:

```bash
javac Wordle.java
```

3. Execute:

```bash
java Wordle
```
This starts the game with default settings (5-letter word, 6 attempts).

## Game Customization

You can enter up to three command-line arguments for a challenging (or easier) experience. All terminal arguments are completely optional, giving you total freedom.

| Argument Position | Purpose | Example |
| :---: | :--- | :--- |
| **1** | Word Length | `6` (for a 6-letter word) |
| **2** | Max Attempts | `7` (for 7 total guesses) |
| **3** | Secret Word | `"BANKROLL"` (sets the secret word directly) |

### Usage Examples

| Command | Description |
| :--- | :--- |
| `java Wordle` | Default game: 5-letter word, 6 attempts. |
| `java Wordle 6 7` | Play with 6-letter words and 7 attempts. |
| `java Wordle 6 7 BANKROLL` | Play a 6-letter, 7-attempt game with "BANKROLL" as the secret word. |

## In-Game Controls

The game primarily accepts English guesses of a particular length, but also supports two special commands:

| Command | Action |
| :--- | :--- |
| `H` or `h` | **Reveal a hint.** Uses one of your remaining hints to show a letter present in the word (total hints are calculated as 40% of the word's length). |
| `QUIT` or any variation of it | Immediately ends the game. |

---

## License

This project is licensed under the MIT License - see the LICENSE.md file for details.
