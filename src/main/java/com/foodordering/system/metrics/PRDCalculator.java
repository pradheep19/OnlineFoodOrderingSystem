
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

    private static final Pattern IF_PATTERN =
            Pattern.compile("\\bif\\s*\\(");

    private static final Pattern ELSE_IF_PATTERN =
            Pattern.compile("\\belse\\s+if\\s*\\(");

    public static void main(String[] args) {

        try {
            Path sourcePath = Paths.get(PRICING_ENGINE);
            String source = Files.readString(sourcePath);

            String cleanedSource = removeComments(source);

            int loc = countSourceLines(cleanedSource);
            int allIfOccurrences =
                    countMatches(IF_PATTERN, cleanedSource);
            int elseIfOccurrences =
                    countMatches(ELSE_IF_PATTERN, cleanedSource);

            // else-if is already included in the general if count.
            int pricingConditions = allIfOccurrences;

            double locPerHundred = loc / 100.0;

            double prd = locPerHundred == 0
                    ? 0.0
                    : pricingConditions / locPerHundred;

            System.out.println();
            System.out.println(
                    "==============================================================");
            System.out.println(
                    "             PRD - PRICING RULE DENSITY");
            System.out.println(
                    "==============================================================");
            System.out.println("Source File : PricingEngine.java");
            System.out.println(
                    "--------------------------------------------------------------");
            System.out.println("PricingEngine LOC        : " + loc);
            System.out.println("if Conditions            : " + allIfOccurrences);
            System.out.println("else-if Conditions       : " + elseIfOccurrences);
            System.out.println("Pricing Conditions       : " + pricingConditions);
            System.out.println(
                    "--------------------------------------------------------------");
            System.out.println("PRD Formula:");
            System.out.println(
                    "PRD = Pricing Conditions / (LOC / 100)");

            System.out.printf(
                    "PRD = %d / (%d / 100)%n",
                    pricingConditions, loc);

            System.out.printf(
                    "PRD = %d / %.2f%n",
                    pricingConditions, locPerHundred);

            System.out.printf("PRD = %.2f%n", prd);
            System.out.printf(
                    "Pricing Conditions per 100 LOC = %.2f%n", prd);

            System.out.println(
                    "==============================================================");

        } catch (IOException e) {
            System.err.println(
                    "Unable to read PricingEngine.java: "
                            + e.getMessage());
        }
    }

    private static int countSourceLines(String source) {
        int count = 0;

        for (String line : source.split("\\R")) {
            if (!line.trim().isEmpty()) {
                count++;
            }
        }

        return count;
    }

    private static int countMatches(
            Pattern pattern, String source) {

        Matcher matcher = pattern.matcher(source);
        int count = 0;

        while (matcher.find()) {
            count++;
        }

        return count;
    }

    /*
     * Removes line and block comments while preserving line breaks.
     */
    private static String removeComments(String source) {
        StringBuilder result = new StringBuilder();

        boolean inLineComment = false;
        boolean inBlockComment = false;

        for (int i = 0; i < source.length(); i++) {
            char current = source.charAt(i);
            char next = i + 1 < source.length()
                    ? source.charAt(i + 1)
                    : '\0';

            if (inLineComment) {
                if (current == '\n') {
                    inLineComment = false;
                    result.append('\n');
                } else {
                    result.append(' ');
                }
                continue;
            }

            if (inBlockComment) {
                if (current == '*' && next == '/') {
                    inBlockComment = false;
                    result.append("  ");
                    i++;
                } else {
                    result.append(current == '\n' ? '\n' : ' ');
                }
                continue;
            }

            if (current == '/' && next == '/') {
                inLineComment = true;
                result.append("  ");
                i++;
                continue;
            }

            if (current == '/' && next == '*') {
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
