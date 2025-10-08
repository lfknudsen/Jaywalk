package com.falkknudsen.jaywalk.util;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

/** Contains various functions for string manipulation. */
public class Strings {
    /** Returns a copy of the string without leading/trailing whitespace,
     as well as without extraneous internal whitespace.<br>
     If input is null, returns an empty string. */
    public static String strip(final String input) {
        if (input == null) {
            return "";
        }
        String output = input.strip();
        output = output.replaceAll("\\s(\\s)+", " ");
        return output;
    }

    /** Null-safe variant of {@link String#toLowerCase()}. If input is null, returns an empty string.
     Otherwise, return {@code input.toLowerCase()}. */
    public static String toLowerCase(final String input) {
        if (input == null) return "";
        return input.toLowerCase();
    }

    /** Null-safe variant of {@link String#isBlank()}. Returns true if input is null or an empty string. */
    public static boolean isBlank(String str) {
        return (str == null) || (str.isBlank());
    }

    /** Returns true if input is non-null and not empty.
     Easier to follow the logic of this than a bunch of !______.isEmpty() expressions */
    public static boolean exists(String str) {
        return (str != null) && (!str.isEmpty());
    }

    // TODO: Use sensible weights/costs, so that, for example, substituting
    //  letters that are near each other on the keyboard is cheaper than ones far away.
    /** Calculate the Levenshtein/edit distance between two Strings.<br>
     In other words, the output number is the number of deletions, insertions, and
     substitutions that would have to be performed for one of the strings to equal the other.<br>
     Adapted from the pseudocode on the
     <a href=https://en.wikipedia.org/wiki/Levenshtein_distance#Iterative_with_two_matrix_rows>Wikipedia page</a>. */
    protected static int levenshtein(String strA, String strB) {
        int[] vectorA = new int[strB.length() + 1];
        int[] vectorB = new int[strB.length() + 1];
        for (int i = 0; i <= strB.length(); i++) {
            vectorA[i] = i; // vectorA = { 0, 1, 2, ..., n }
        }
        for (int i = 0; i < strA.length(); i++) {
            vectorB[0] = i + 1; // vectorB = { 1, 2, 3, ..., n-1, ? }

            for (int j = 0; j < strB.length(); j++) {
                int deletionCost = vectorA[j + 1] + 1;
                int insertionCost = vectorB[j] + 1;
                int substitutionCost = vectorA[j];
                if (strA.charAt(i) != strB.charAt(j)) {
                    substitutionCost ++;
                }
                vectorB[j + 1] = Math.min(deletionCost,
                        Math.min(insertionCost, substitutionCost));
            }
            int[] temp = vectorA.clone();
            vectorA = vectorB.clone();
            vectorB = temp;
        }
        return vectorA[strB.length()];
    }

    /** Determine which of two words {@code a} and {@code b} is closest to the
     {@code key} word, based on the Levenshtein distance.<br>
     Used to determine which of the two nearest neighbours is the
     closest to the key. */
    public static String nearestWord(String key, String a, String b) {
        if (key.compareToIgnoreCase(a) == 0) {
            return a;
        }
        if (key.compareToIgnoreCase(b) == 0) {
            return b;
        }
        if (levenshtein(key, a) < levenshtein(key, b)) {
            return a;
        }
        return b;
    }

    public static String toTitleCase(String s) {
        String[] words = s.split(" ");
        StringBuilder sb = new StringBuilder(s.length());
        if (words.length > 0) {
            sb.append(words[0].substring(0, 1).toUpperCase()).append(words[0].substring(1).toLowerCase());
        }
        for (int i = 1; i < words.length; i++) {
            sb.append(" ").append(words[i].substring(0, 1).toUpperCase()).append(words[i].substring(1).toLowerCase());
        }
        return sb.toString();
    }

    public static boolean isFirstUpperCase(String s) {
        return (Character.isUpperCase(s.charAt(0)));
    }

    public static class comparator implements Comparator<String>, Serializable {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareToIgnoreCase(o2);
        }
    }

    public static boolean is(Map<String, String> dict, String key, String value) {
        if (dict == null || key == null || value == null) throw new NullPointerException();
        String result = dict.get(key);
        if (result != null) return result.equals(value);
        return false;
    }
}