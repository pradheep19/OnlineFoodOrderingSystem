
package com.foodordering.system.metrics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DSCICalculator {

    private static final Logger LOGGER =
            Logger.getLogger(DSCICalculator.class.getName());

    private static final String DISCOUNT_CALCULATOR =
            "src/main/java/com/foodordering/system/service/DiscountCalculator.java";

    public static void main(String[] args) {

        try {
            // STEP 1: Read the actual DiscountCalculator source
            Path sourcePath = Paths.get(DISCOUNT_CALCULATOR);
            String source = Files.readString(sourcePath);

            // STEP 2: Detect implemented discount types
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

            // STEP 3: Display implemented discount types
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
            System.out.println("Implemented Discount Types");
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
                    "Total Discount Types : " + totalDiscountTypes
            );
            System.out.println();

            System.out.println(
                    "DSCI = Discounts Used Together / Total Implemented Discounts"
            );
            System.out.println(
                    "--------------------------------------------------------------"
            );

            // Scenario 1: Coupon only
            int couponOnly = couponImplemented ? 1 : 0;

            printScenario(
                    "Coupon Only",
                    couponOnly,
                    totalDiscountTypes
            );

            // Scenario 2: Loyalty only
            int loyaltyOnly = loyaltyImplemented ? 1 : 0;

            printScenario(
                    "Loyalty Only",
                    loyaltyOnly,
                    totalDiscountTypes
            );

            // Scenario 3: Loyalty + Coupon
            int loyaltyCoupon =
                    (loyaltyImplemented ? 1 : 0)
                            + (couponImplemented ? 1 : 0);

            printScenario(
                    "Loyalty + Coupon",
                    loyaltyCoupon,
                    totalDiscountTypes
            );

            // Scenario 4: Loyalty + Bulk
            int loyaltyBulk =
                    (loyaltyImplemented ? 1 : 0)
                            + (bulkImplemented ? 1 : 0);

            printScenario(
                    "Loyalty + Bulk",
                    loyaltyBulk,
                    totalDiscountTypes
            );

            // Scenario 5: Maximum stacking
            int maximumStacking =
                    (loyaltyImplemented ? 1 : 0)
                            + (bulkImplemented ? 1 : 0)
                            + (couponImplemented ? 1 : 0);

            double maximumDSCI = calculateDSCI(
                    maximumStacking,
                    totalDiscountTypes
            );

            printScenario(
                    "Loyalty + Bulk + Coupon",
                    maximumStacking,
                    totalDiscountTypes
            );

            // Final result
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
            LOGGER.log(
                    Level.SEVERE,
                    "Unable to read DiscountCalculator.java",
                    e
            );
        }
    }

    /*
     * DSCI = Active Discounts / Total Implemented Discounts
     */
    private static double calculateDSCI(
            int activeDiscounts,
            int totalDiscounts) {

        if (totalDiscounts == 0) {
            return 0.0;
        }

        return (double) activeDiscounts / totalDiscounts;
    }

    /*
     * Prints one DSCI scenario.
     */
    private static void printScenario(
            String scenario,
            int activeDiscounts,
            int totalDiscounts) {

        double dsci = calculateDSCI(
                activeDiscounts,
                totalDiscounts
        );

        System.out.printf(
                "%-30s %d / %d        %.2f%n",
                scenario,
                activeDiscounts,
                totalDiscounts,
                dsci
        );
    }
}
