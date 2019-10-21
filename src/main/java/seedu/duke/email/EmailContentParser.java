package seedu.duke.email;

import seedu.duke.Duke;
import seedu.duke.email.entity.Email;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A parser to process the content of emails to support automatic management of email.
 */
public class EmailContentParser {
    private static int KEYWORD_SUBJECT_WEIGHTAGE = 5;
    private static int KEYWORD_SENDER_WEIGHTAGE = 3;
    private static int KEYWORD_BODY_WEIGHTAGE = 1;
    private static ArrayList<KeywordPair> keywordList;
    private static int INFINITY = 0x3f3f3f;
    private static int FUZZY_LIMIT = 3;

    /**
     * Finds all keywords in email.
     * @param email Email to be scanned for keywords
     */
    public static void allKeywordInEmail(Email email) {
        for (KeywordPair keywordPair : keywordList) {
            int relevance = keywordInEmail(email, keywordPair);
            if (relevance > 0) {
                Duke.getUI().showDebug(keywordPair.getKeyword() + ": " + keywordInEmail(email, keywordPair)
                        + " => " + email.getSubject());
                email.addTag(keywordPair, relevance);
            }
        }
    }

    /**
     * Calculates the keyword relevance score within an email based on its position and number of occurrence.
     *
     * @param email       the email where the keyword pair is to be looked for
     * @param keywordPair the target keyword pair
     * @return the occurrence score
     */
    public static int keywordInEmail(Email email, KeywordPair keywordPair) {
        int totalScore = 0;
        totalScore += keywordInSubject(email, keywordPair) * KEYWORD_SUBJECT_WEIGHTAGE;
        totalScore += keywordInSender(email, keywordPair) * KEYWORD_SENDER_WEIGHTAGE;
        totalScore += keywordInBody(email, keywordPair) * KEYWORD_BODY_WEIGHTAGE;
        return totalScore;
    }

    private static int keywordInSubject(Email email, KeywordPair keywordPair) {
        return keywordInString(email.getSubject(), keywordPair);
    }

    private static int keywordInSender(Email email, KeywordPair keywordPair) {
        return keywordInString(email.getSenderString(), keywordPair);
    }

    private static int keywordInBody(Email email, KeywordPair keywordPair) {
        return keywordInString(email.getBody(), keywordPair);
    }

    /**
     * Looks for a keyword within a given string.
     *
     * @param input       the string where the keyword is looked for
     * @param keywordPair the target keyword looking for
     * @return whether the keyword pair is found in the string
     */
    public static int keywordInString(String input, KeywordPair keywordPair) {
        int occurrence = 0;
        for (int i = 0; i < keywordPair.getExpressions().size(); i++) {
            String expression = keywordPair.getExpressions().get(i);
            Pattern expressionPattern = Pattern.compile(".*" + expression + ".*",
                    Pattern.CASE_INSENSITIVE);
            Matcher expressionMatcher = expressionPattern.matcher(input);
            while (expressionMatcher.find()) {
                occurrence++;
            }
        }
        return occurrence;
    }

    /**
     * Keyword List for searching.
     * Hardcoded initial keywords relavant to SoC students, which is our target audience.
     */
    public static void initKeywordList() {
        ArrayList<KeywordPair> keywordList = new ArrayList<>();
        keywordList.add(new KeywordPair("CS2113T", new ArrayList<String>(List.of(
                "CS2113T", "CS2113", "TAN KIAN WEI, JASON", "Leow Wei Xiang", "Akshay Narayan", "Akshay"))));
        keywordList.add(new KeywordPair("CS2101", new ArrayList<String>(List.of(
                "CS2101", "Anita Toh Ann Lee"))));
        keywordList.add(new KeywordPair("CG2271", new ArrayList<String>(List.of(
                "CG2271", "Djordje Jevdjic"))));
        keywordList.add(new KeywordPair("CS2102", new ArrayList<String>(List.of(
                "CS2102", "Adi Yoga Sidi Prabawa"))));
        keywordList.add(new KeywordPair("CS3230", new ArrayList<String>(List.of(
                "CS3230", "Divesh Aggarwal"))));
        keywordList.add(new KeywordPair("CEG Admin", new ArrayList<String>(List.of(
                "Low Mun Bak"))));
        keywordList.add(new KeywordPair("SEP", new ArrayList<String>(List.of(
                "SEP", "Student Exchange Programme"))));
        keywordList.add(new KeywordPair("Tutorial", new ArrayList<String>(List.of(
                "Tutorial"))));
        keywordList.add(new KeywordPair("Assignment", new ArrayList<String>(List.of(
                "Assignment"))));
        keywordList.add(new KeywordPair("Spam", new ArrayList<String>(List.of(
                "UHC Wellness", "luminus-do-not-reply", "NUS Libraries"))));

        EmailContentParser.keywordList = keywordList;
    }

    /**
     * Computes the edit distance between first and second, which is the number of steps required to transform
     * first to second if only addition, deletion, update of a single character is allowed for each step.
     *
     * @param first first string
     * @param second second string
     * @return edit distance between first and second
     */
    public static int editDistance(String first, String second) {
        if (first.length() == 0 || second.length() == 0) {
            return first.length() + second.length();
        }
        first = first.toLowerCase();
        second = second.toLowerCase();
        //Prepare a distance array for DP
        int[][] dist = new int[first.length() + 1][second.length() + 1];
        //Initialize distance array with all zeros
        for (int[] row : dist) {
            Arrays.fill(row, 0);
        }
        //Initialize starting positions for DP
        for (int i = 0; i <= first.length(); i++) {
            dist[i][0] = i;
        }
        for (int j = 0; j <= second.length(); j++) {
            dist[0][j] = j;
        }
        //Start DP
        for (int i = 1; i <= first.length(); i++) {
            for (int j = 1; j <= second.length(); j++) {
                int min = INFINITY;
                min = Math.min(min, dist[i - 1][j - 1] + (first.charAt(i - 1) == second.charAt(j - 1) ? 0 : 1));
                min = Math.min(min, dist[i - 1][j] + 1);
                min = Math.min(min, dist[i][j - 1] + 1);
                dist[i][j] = min;
            }
        }
        return dist[first.length()][second.length()];
    }

    /**
     * Searches a keyword in input string with some tolerance of inaccuracy.
     *
     * @param input  input string where the keyword is searched
     * @param target the target keyword to be searched
     * @return a relevance score related to both occurrence and relevance
     */
    private static int fuzzySearchInString(String input, String target) {
        int score = 0;
        String[] inputWords = input.split("\\W");
        String[] targetWords = target.split("\\W");
        for (String inputWord : inputWords) {
            for (String targetWord : targetWords) {
                if (inputWord.length() == 0 || targetWord.length() == 0) {
                    continue;
                }
                int distance = editDistance(inputWord, targetWord);
                if (distance <= FUZZY_LIMIT) {
                    score += FUZZY_LIMIT - distance + 1;
                }
            }
        }
        Duke.getUI().showError(score + " : " + input + " <> " + target);
        return score;
    }

    /**
     * A pair of keyword with its possible expressions.
     */
    public static class KeywordPair {
        private String keyword;
        private ArrayList<String> expressions;

        /**
         * Constructs a keyword pair.
         *
         * @param keyword     the value of keyword looked for
         * @param expressions the possible expressions of that keyword
         */
        public KeywordPair(String keyword, ArrayList<String> expressions) {
            this.keyword = keyword;
            this.expressions = expressions;
        }

        /**
         * Constructs a keyword pair with only keyword. Expression will be the same as the keyword by
         * default.
         *
         * @param keyword the value of keyword looked for
         */
        public KeywordPair(String keyword) {
            this.keyword = keyword;
            this.expressions = new ArrayList<>(List.of(keyword));
        }

        public String getKeyword() {
            return this.keyword;
        }

        public ArrayList<String> getExpressions() {
            return this.expressions;
        }
    }
}
