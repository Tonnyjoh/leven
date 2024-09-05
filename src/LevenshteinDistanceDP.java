import java.io.*;
import java.util.*;

class LevenshteinDistanceDP {

    static int computeLevenshteinDistanceDP(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i][j - 1] + 1, dp[i - 1][j] + 1), dp[i - 1][j - 1] + cost);
            }
        }

        return dp[s1.length()][s2.length()];
    }

/*
    // Check for distinct characters in str1 and str2
    static int numOfReplacement(char c1, char c2) {
        return c1 == c2 ? 0 : 1;
    }

    // Return the minimum value among the provided operations
    static int minmEdits(int... nums) {
        return Arrays.stream(nums).min().orElse(Integer.MAX_VALUE);
    }
*/

    static void textLoader(String filename) throws IOException {
        ArrayList<String> wordList = new ArrayList<>();
        try (BufferedReader breader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = breader.readLine()) != null) {
                String word = line.trim();

                wordList.add(word);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Number of words in ArrayList: " + wordList.size());
    }

    // stockage liste des mots dans hashset
    static HashSet<String> getWordSet(String filename) throws IOException {
        HashSet<String> wordSet = new HashSet<>();
        try (BufferedReader breader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = breader.readLine()) != null) {
                String word = line.trim();
                wordSet.add(word);
            }
        }
        return wordSet;
    }

    // exist or not
    static boolean isExistInDictionary(String word, HashSet<String> dictionary) {
        return dictionary.contains(word);
    }

    //tester si un mot donné par l’utilisateur existe dans le dictionnaire ou pas.
    static void testWordInDictionary(String filename) throws IOException {
        HashSet<String> dictionary = getWordSet(filename);
        Scanner scanner = new Scanner(System.in);
        System.out.print("Entrez un mot à tester: ");
        String word = scanner.nextLine().trim();

        if (isExistInDictionary(word, dictionary)) {
            System.out.println("Le mot \"" + word + "\" existe dans le dictionnaire.");
        } else {
            System.out.println("Le mot \"" + word + "\" n'existe pas dans le dictionnaire.");
            //execution de proposalWords
            ArrayList<String> proposals = proposalWords(word, dictionary);
            System.out.println("Propositions de mots proches :");
            for (String proposition : proposals) {
                System.out.println("- " + proposition);
            }

        }
    }

    //proches, selon la distance de Levenshtein dans le cas où un mot donné n’existe pas dans le dictionnaire.
    //donner une liste de propositions de quelques mots les plus
    static ArrayList<String> proposalWords(String word, HashSet<String> dictionary) {
        ArrayList<String> proposalWords = new ArrayList<>();

        PriorityQueue<Map.Entry<String, Integer>> pq = new PriorityQueue<>(
                Map.Entry.comparingByValue()
        );

        for (String dictWord : dictionary) {
            int distance = computeLevenshteinDistanceDP(word, dictWord);
            pq.offer(new AbstractMap.SimpleEntry<>(dictWord, distance));
        }

        int count = 0;
        int maxProposals = 10;
        while (!pq.isEmpty() && count < maxProposals) {
            proposalWords.add(pq.poll().getKey());
            count++;
        }

        return proposalWords;
    }

    //ecriture dans un fichier
    static void writeInFile(String sentence, String filename) {
        try {
            FileWriter fileWriter = new FileWriter(filename, true); // 'true' pour append
            BufferedWriter writer = new BufferedWriter(fileWriter);
            for (String word : sentence.split(" ")) {
                writer.write(word + "  ");
            }
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //
    static void writePhrases(String filename) throws IOException {
        HashSet<String> dictionary = getWordSet(filename);
        Scanner scanner = new Scanner(System.in);
        System.out.print("Entrez un phrase: ");
        String phrase = scanner.nextLine().trim();
        String[] sentence = phrase.split(" ");
        writeInFile(phrase,"ressources/texte.txt");
        for (String word : sentence) {
            ArrayList<String> proposals = proposalWords(word, dictionary);

            if (!isExistInDictionary(word, dictionary)) {
               for (String proposal : proposals) {
                   writeInFile(proposal,"ressources/texte.err");
               }
            }
        }

    }
    // Driver Code
    public static void main(String[] args) throws IOException {
        //Example usage of Levenshtein distance calculation
        //String s1 = "CHIEN";
        //String s2 = "NICHE";
        //System.out.println("Levenshtein Distance: " + computeLevenshteinDistanceDP(s1, s2));
        String filename = "ressources/gutenberg.txt";
        //writePhrases(filename);
        textLoader(filename);
        testWordInDictionary(filename);
    }
}
