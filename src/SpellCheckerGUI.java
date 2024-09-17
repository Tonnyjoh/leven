import javax.swing.*;
import javax.swing.Timer;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpellCheckerGUI extends JFrame {
    private final JTextPane textPane;
    private final HashSet<String> dictionary;
    private final HashMap<String, ArrayList<String>> suggestions;
    private Timer typingTimer;

    public SpellCheckerGUI() throws IOException {
        super("Spell Checker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);

        dictionary = LevenshteinDistanceDP.getWordSet("ressources/gutenberg.txt");
        suggestions = new HashMap<>();

        textPane = new JTextPane();
        JScrollPane scrollPane = new JScrollPane(textPane);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);

        setupTextPaneListeners();
        setupTypingTimer();
    }

    private void setupTextPaneListeners() {
        textPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (Character.isLetterOrDigit(e.getKeyChar()) || e.getKeyCode() == KeyEvent.VK_SPACE) {
                    typingTimer.restart();
                }
            }
        });

        textPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int pos = textPane.viewToModel2D(e.getPoint());
                    String word = getWordAtPosition(pos);
                    if (suggestions.containsKey(word)) {
                        showSuggestionsPopup(e.getPoint(), word);
                    }
                }
            }
        });
    }

    private void setupTypingTimer() {
        typingTimer = new Timer(500, e -> checkSpelling());
        typingTimer.setRepeats(false);
    }

    private String getWordAtPosition(int pos) {
        String text = textPane.getText();
        int start = pos;
        int end = pos;

        while (start > 0 && Character.isLetterOrDigit(text.charAt(start - 1))) {
            start--;
        }
        while (end < text.length() && Character.isLetterOrDigit(text.charAt(end))) {
            end++;
        }

        return text.substring(start, end);
    }

    private void checkSpelling() {
        String text = textPane.getText();
        StyledDocument doc = textPane.getStyledDocument();

        Style yellowHighlight = doc.addStyle("Highlight", null);
        StyleConstants.setBackground(yellowHighlight, Color.YELLOW);

        // Supprimer les anciens surlignages
        doc.setCharacterAttributes(0, doc.getLength(), textPane.getStyle(StyleContext.DEFAULT_STYLE), true);

        // Regex pour identifier les mots, même avec des ponctuations adjacentes
        String regex = "\\b[\\w']+\\b"; // Matches words (ignores punctuation)

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String word = matcher.group();
            int wordStartIndex = matcher.start();
            int wordEndIndex = matcher.end();

            if (!LevenshteinDistanceDP.isExistInDictionary(word.toLowerCase(), dictionary)) {
                // Applique le surlignage pour les mots mal orthographiés
                doc.setCharacterAttributes(wordStartIndex, word.length(), yellowHighlight, false);

                ArrayList<String> wordSuggestions = LevenshteinDistanceDP.proposalWords(word.toLowerCase(), dictionary);
                suggestions.put(word, wordSuggestions);
            }
        }
    }


    private void showSuggestionsPopup(Point point, String word) {
        JPopupMenu popup = new JPopupMenu();
        ArrayList<String> wordSuggestions = suggestions.get(word);

        for (String suggestion : wordSuggestions) {
            JMenuItem item = new JMenuItem(suggestion);
            item.addActionListener(e -> replaceMisspelledWord(word, suggestion));
            popup.add(item);
        }

        popup.show(textPane, point.x, point.y);
    }

    private void replaceMisspelledWord(String misspelledWord, String correction) {
        String text = textPane.getText();
        int start = text.indexOf(misspelledWord);
        if (start != -1) {
            try {
                textPane.getDocument().remove(start, misspelledWord.length());
                textPane.getDocument().insertString(start, correction, null);
                checkSpelling();
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new SpellCheckerGUI().setVisible(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}