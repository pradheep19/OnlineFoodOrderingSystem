package com.foodordering.system.metrics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PRDCalculator {

    private static final String PRICING_ENGINE =
            "src/main/java/com/foodordering/system/service/PricingEngine.java";

    /*
     * Matches both:
     *
     * if (...)
     * else if (...)
     *
     * Each represents one pricing decision condition.
     */
    private static final Pattern IF_PATTERN =
            Pattern.compile("\\bif\\s*\\(");

    private static final Pattern ELSE_IF_PATTERN =
            Pattern.compile("\\belse\\s+if\\s*\\(");

    public static void main(String[] args) {

        try {

            // =========================================================
            // STEP 1: Read PricingEngine.java
            // =========================================================

            Path sourcePath =
                    Paths.get(PRICING_ENGINE);

            String source =
                    Files.readString(sourcePath);

            // =========================================================
            // STEP 2: Remove comments
            // =========================================================

            String cleanedSource =
                    removeComments(source);

            // =========================================================
            // STEP 3: Count LOC
            // =========================================================

            int loc = countSourceLines(cleanedSource);

            // =========================================================
            // STEP 4: Count pricing conditions
            // =========================================================

            int allIfOccurrences =
                    countMatches(
                            IF_PATTERN,
                            cleanedSource
                    );

            int elseIfOccurrences =
                    countMatches(
                            ELSE_IF_PATTERN,
                            cleanedSource
                    );

            /*
             * "else if" contains "if", so the general if count
             * already includes else-if conditions.
             *
             * Therefore the total number of pricing conditions
             * is simply the number of "if" occurrences.
             */
            int pricingConditions =
                    allIfOccurrences;

            // =========================================================
            // STEP 5: Calculate PRD
            // =========================================================

            double locPerHundred =
                    loc / 100.0;

            double prd =
                    locPerHundred == 0
                            ? 0.0
                            : pricingConditions
                            / locPerHundred;

            // =========================================================
            // STEP 6: Display calculation
            // =========================================================

            System.out.println();

            System.out.println(
                    "=============================================================="
            );

            System.out.println(
                    "             PRD - PRICING RULE DENSITY"
            );

            System.out.println(
                    "=============================================================="
            );

            System.out.println(
                    "Source File : PricingEngine.java"
            );

            System.out.println(
                    "--------------------------------------------------------------"
            );

            System.out.println(
                    "PricingEngine LOC        : "
                            + loc
            );

            System.out.println(
                    "if Conditions            : "
                            + allIfOccurrences
            );

            System.out.println(
                    "else-if Conditions       : "
                            + elseIfOccurrences
            );

            System.out.println(
                    "Pricing Conditions       : "
                            + pricingConditions
            );

            System.out.println(
                    "--------------------------------------------------------------"
            );

            System.out.println(
                    "PRD Formula:"
            );

            System.out.println(
                    "PRD = Pricing Conditions / (LOC / 100)"
            );

            System.out.printf(
                    "PRD = %d / (%d / 100)%n",
                    pricingConditions,
                    loc
            );

            System.out.printf(
                    "PRD = %d / %.2f%n",
                    pricingConditions,
                    locPerHundred
            );

            System.out.printf(
                    "PRD = %.2f%n",
                    prd
            );

            System.out.printf(
                    "Pricing Conditions per 100 LOC = %.2f%n",
                    prd
            );

            System.out.println(
                    "=============================================================="
            );

        } catch (IOException e) {

            System.err.println(
                    "Unable to read PricingEngine.java"
            );

            e.printStackTrace();
        }
    }

    /*
     * Counts non-blank source lines.
     */
    private static int countSourceLines(
            String source) {

        int count = 0;

        String[] lines =
                source.split("\\R");

        for (String line : lines) {

            if (!line.trim().isEmpty()) {
                count++;
            }
        }

        return count;
    }

    /*
     * Counts occurrences of a regular expression.
     */
    private static int countMatches(
            Pattern pattern,
            String source) {

        Matcher matcher =
                pattern.matcher(source);

        int count = 0;

        while (matcher.find()) {
            count++;
        }

        return count;
    }

    /*
     * Removes:
     *
     * // single-line comments
     * /* multi-line comments *\/
     *
     * This prevents conditions written inside comments
     * from being counted.
     */
    private static String removeComments(
            String source) {

        StringBuilder result =
                new StringBuilder();

        boolean inLineComment = false;
        boolean inBlockComment = false;

        for (int i = 0;
             i < source.length();
             i++) {

            char current =
                    source.charAt(i);

            char next =
                    i + 1 < source.length()
                            ? source.charAt(i + 1)
                            : '\0';

            // ---------------------------------------------------------
            // Inside single-line comment
            // ---------------------------------------------------------

            if (inLineComment) {

                if (current == '\n') {

                    inLineComment = false;

                    result.append('\n');

                } else {

                    result.append(' ');
                }

                continue;
            }

            // ---------------------------------------------------------
            // Inside block comment
            // ---------------------------------------------------------

            if (inBlockComment) {

                if (current == '*'
                        && next == '/') {

                    inBlockComment = false;

                    result.append("  ");

                    i++;

                } else {

                    result.append(
                            current == '\n'
                                    ? '\n'
                                    : ' '
                    );
                }

                continue;
            }

            // ---------------------------------------------------------
            // Start single-line comment
            // ---------------------------------------------------------

            if (current == '/'
                    && next == '/') {

                inLineComment = true;

                result.append("  ");

                i++;

                continue;
            }

            // ---------------------------------------------------------
            // Start block comment
            // ---------------------------------------------------------

            if (current == '/'
                    && next == '*') {

                inBlockComment = true;

                result.append("  ");

                i++;

                continue;
            }

            result.append(current);
        }

        return result.toString();
    }
}