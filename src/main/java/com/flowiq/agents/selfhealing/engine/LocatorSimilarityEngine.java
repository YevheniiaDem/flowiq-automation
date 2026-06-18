package com.flowiq.agents.selfhealing.engine;

/**
 * Computes string similarity using Levenshtein distance for locator self-healing.
 */
public final class LocatorSimilarityEngine {

    private LocatorSimilarityEngine() {
    }

    /**
     * Returns similarity in range [0.0, 1.0] where 1.0 is an exact match.
     */
    public static double similarity(String left, String right) {
        if (left == null || right == null) {
            return 0.0;
        }
        String a = normalize(left);
        String b = normalize(right);
        if (a.isEmpty() && b.isEmpty()) {
            return 1.0;
        }
        if (a.isEmpty() || b.isEmpty()) {
            return 0.0;
        }
        if (a.equals(b)) {
            return 1.0;
        }
        if (a.contains(b) || b.contains(a)) {
            return 0.92;
        }
        int distance = levenshteinDistance(a, b);
        int maxLen = Math.max(a.length(), b.length());
        return Math.max(0.0, 1.0 - ((double) distance / maxLen));
    }

    public static int levenshteinDistance(String left, String right) {
        String a = normalize(left);
        String b = normalize(right);
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost);
            }
        }
        return dp[a.length()][b.length()];
    }

    private static String normalize(String value) {
        return value.trim()
                .toLowerCase()
                .replaceAll("[\\s_\\-]+", "")
                .replaceAll("[\"'`]", "");
    }
}
