import java.io.*;
import java.util.*;

class LevenshteinDistanceDP {

    // Méthode pour calculer la distance de Levenshtein entre deux chaînes
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

    // Chargement d'un fichier texte et stockage dans une ArrayList
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

    // Stockage des mots dans un HashSet pour optimiser la recherche
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

    // Vérifie si un mot existe dans le dictionnaire
    static boolean isExistInDictionary(String word, HashSet<String> dictionary) {
        return dictionary.contains(word);
    }

    // Tester si un mot donné par l'utilisateur existe dans le dictionnaire ou pas
    static void testWordInDictionary(String filename) throws IOException {
        HashSet<String> dictionary = getWordSet(filename);
        Scanner scanner = new Scanner(System.in);
        System.out.print("Entrez un mot à tester: ");
        String word = scanner.nextLine().trim();

        if (isExistInDictionary(word, dictionary)) {
            System.out.println("Le mot \"" + word + "\" existe dans le dictionnaire.");
        } else {
            System.out.println("Le mot \"" + word + "\" n'existe pas dans le dictionnaire.");
            // Exécution de proposalWords pour obtenir des suggestions
            ArrayList<String> proposals = proposalWords(word, dictionary);
            System.out.println("Propositions de mots proches :");
            for (String proposition : proposals) {
                System.out.println("- " + proposition);
            }
        }
    }

    // Propose des mots proches selon la distance de Levenshtein dans le cas où un mot donné n'existe pas dans le dictionnaire
    static ArrayList<String> proposalWords(String word, HashSet<String> dictionary) {
        ArrayList<String> proposalWords = new ArrayList<>();

        // Utilisation d'une PriorityQueue pour trier les mots par distance de Levenshtein
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

    // Écriture d'une phrase dans un fichier texte
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

    // Traitement d'une phrase, vérification des mots et suggestion d'alternatives si nécessaire
    static void writePhrases(String filename) throws IOException {
        HashSet<String> dictionary = getWordSet(filename);
        Scanner scanner = new Scanner(System.in);
        System.out.print("Entrez une phrase: ");
        String phrase = scanner.nextLine().trim();
        String[] sentence = phrase.split(" ");

        writeInFile(phrase, "ressources/texte.txt");

        // Pour chaque mot, vérifier l'existence dans le dictionnaire et proposer des alternatives si nécessaire
        for (String word : sentence) {
            if (!isExistInDictionary(word, dictionary)) {
                ArrayList<String> proposals = proposalWords(word, dictionary);
                for (String proposal : proposals) {
                    writeInFile(proposal, "ressources/texte.err");
                }
            }
        }
    }

    // Driver Code
    public static void main(String[] args) throws IOException {
        String filename = "ressources/gutenberg.txt";
        textLoader(filename);
        testWordInDictionary(filename);
    }
}
