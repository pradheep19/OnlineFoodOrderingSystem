package com.foodordering.system.metrics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DSCICalculator {

    private static final String DISCOUNT_CALCULATOR =
            "src/main/java/com/foodordering/system/service/DiscountCalculator.java";

    public static void main(String[] args) {

        try {

            // =========================================================
            // STEP 1: Read the actual DiscountCalculator source
            // =========================================================

            Path sourcePath =
                    Paths.get(DISCOUNT_CALCULATOR);

            String source =
                    Files.readString(sourcePath);

            // =========================================================
            // STEP 2: Detect implemented discount types
            // =========================================================

            boolean loyaltyImplemented =
                    source.contains("calculateLoyaltyDiscount");

            boolean bulkImplemented =
                    source.contains("calculateBulkDiscount");

            boolean couponImplemented =
                    source.contains("calculateCouponDiscount");

            int totalDiscountTypes = 0;

            if (loyaltyImplemented) {
                totalDiscountTypes++;
            }

            if (bulkImplemented) {
                totalDiscountTypes++;
            }

            if (couponImplemented) {
                totalDiscountTypes++;
            }

            // =========================================================
            // STEP 3: Calculate different stacking scenarios
            // =========================================================

            System.out.println();
            System.out.println(
                    "=============================================================="
            );

            System.out.println(
                    "       DSCI - DISCOUNT STACKING COMPLEXITY INDEX"
            );

            System.out.println(
                    "=============================================================="
            );

            System.out.println(
                    "Implemented Discount Types"
            );

            System.out.println(
                    "--------------------------------------------------------------"
            );

            System.out.println(
                    "Loyalty Discount : "
                            + (loyaltyImplemented
                            ? "Implemented"
                            : "Not Implemented")
            );

            System.out.println(
                    "Bulk Discount    : "
                            + (bulkImplemented
                            ? "Implemented"
                            : "Not Implemented")
            );

            System.out.println(
                    "Coupon Discount  : "
                            + (couponImplemented
                            ? "Implemented"
                            : "Not Implemented")
            );

            System.out.println(
                    "--------------------------------------------------------------"
            );

            System.out.println(
                    "Total Discount Types : "
                            + totalDiscountTypes
            );

            System.out.println();

            System.out.println(
                    "DSCI = Discounts Used Together / Total Implemented Discounts"
            );

            System.out.println(
                    "--------------------------------------------------------------"
            );

            // =========================================================
            // Scenario 1: Coupon only
            // =========================================================

            int couponOnly = 0;

            if (couponImplemented) {
                couponOnly++;
            }

            double dsciCouponOnly =
                    calculateDSCI(
                            couponOnly,
                            totalDiscountTypes
                    );

            printScenario(
                    "Coupon Only",
                    couponOnly,
                    totalDiscountTypes,
                    dsciCouponOnly
            );

            // =========================================================
            // Scenario 2: Loyalty only
            // =========================================================

            int loyaltyOnly = 0;

            if (loyaltyImplemented) {
                loyaltyOnly++;
            }

            double dsciLoyaltyOnly =
                    calculateDSCI(
                            loyaltyOnly,
                            totalDiscountTypes
                    );

            printScenario(
                    "Loyalty Only",
                    loyaltyOnly,
                    totalDiscountTypes,
                    dsciLoyaltyOnly
            );

            // =========================================================
            // Scenario 3: Loyalty + Coupon
            // =========================================================

            int loyaltyCoupon = 0;

            if (loyaltyImplemented) {
                loyaltyCoupon++;
            }

            if (couponImplemented) {
                loyaltyCoupon++;
            }

            double dsciLoyaltyCoupon =
                    calculateDSCI(
                            loyaltyCoupon,
                            totalDiscountTypes
                    );

            printScenario(
                    "Loyalty + Coupon",
                    loyaltyCoupon,
                    totalDiscountTypes,
                    dsciLoyaltyCoupon
            );

            // =========================================================
            // Scenario 4: Loyalty + Bulk
            // =========================================================

            int loyaltyBulk = 0;

            if (loyaltyImplemented) {
                loyaltyBulk++;
            }

            if (bulkImplemented) {
                loyaltyBulk++;
            }

            double dsciLoyaltyBulk =
                    calculateDSCI(
                            loyaltyBulk,
                            totalDiscountTypes
                    );

            printScenario(
                    "Loyalty + Bulk",
                    loyaltyBulk,
                    totalDiscountTypes,
                    dsciLoyaltyBulk
            );

            // =========================================================
            // Scenario 5: Maximum stacking
            // =========================================================

            int maximumStacking = 0;

            if (loyaltyImplemented) {
                maximumStacking++;
            }

            if (bulkImplemented) {
                maximumStacking++;
            }

            if (couponImplemented) {
                maximumStacking++;
            }

            double maximumDSCI =
                    calculateDSCI(
                            maximumStacking,
                            totalDiscountTypes
                    );

            printScenario(
                    "Loyalty + Bulk + Coupon",
                    maximumStacking,
                    totalDiscountTypes,
                    maximumDSCI
            );

            // =========================================================
            // Final result
            // =========================================================

            System.out.println(
                    "--------------------------------------------------------------"
            );

            System.out.printf(
                    "Maximum DSCI : %.2f%n",
                    maximumDSCI
            );

            System.out.println(
                    "=============================================================="
            );

        } catch (IOException e) {

            System.err.println(
                    "Unable to read DiscountCalculator.java"
            );

            e.printStackTrace();
        }
    }

    /*
     * DSCI Formula:
     *
     * DSCI =
     * Discounts Actually Used Together
     * ---------------------------------
     * Total Implemented Discounts
     */
    private static double calculateDSCI(
            int activeDiscounts,
            int totalDiscounts) {

        if (totalDiscounts == 0) {
            return 0.0;
        }

        return (double) activeDiscounts
                / totalDiscounts;
    }

    /*
     * Prints one DSCI scenario.
     */
    private static void printScenario(
            String scenario,
            int activeDiscounts,
            int totalDiscounts,
            double dsci) {

        System.out.printf(
                "%-30s %d / %d        %.2f%n",
                scenario,
                activeDiscounts,
                totalDiscounts,
                dsci
        );
    }
}