package com.foodordering.system.service;

import org.springframework.stereotype.Service;

import com.foodordering.system.model.Customer;
import com.foodordering.system.model.LoyaltyTier;


@Service
public class LoyaltyService {


    // =========================================================
    // LOYALTY TIER THRESHOLDS
    // =========================================================

    /*
     * Loyalty tiers are determined by the customer's
     * accumulated loyalty points.
     *
     * REGULAR  : Below 500 points
     * SILVER   : 500 - 999.99 points
     * GOLD     : 1000 - 1499.99 points
     * PLATINUM : 1500 points and above
     */

    private static final double SILVER_THRESHOLD = 500;

    private static final double GOLD_THRESHOLD = 1000;

    private static final double PLATINUM_THRESHOLD = 1500;


    // =========================================================
    // POINTS EARNED PER RUPEE
    // =========================================================

    /*
     * Customer earns 1 loyalty point for every
     * rupee spent on the final order amount.
     */

    private static final double POINTS_PER_RUPEE = 1.0;


    // =========================================================
    // ADD LOYALTY POINTS
    // =========================================================

    /**
     * Adds loyalty points to a customer based on
     * the final order amount.
     */
    public void addPoints(
            Customer customer,
            double orderAmount) {


        // Invalid customer or order amount
        if (customer == null ||
                orderAmount <= 0) {

            return;
        }


        // Calculate points
        double points =
                orderAmount *
                        POINTS_PER_RUPEE;


        // Add points to customer
        customer.addLoyaltyPoints(
                points
        );
    }


    // =========================================================
    // GET LOYALTY POINTS
    // =========================================================

    /**
     * Returns the customer's current loyalty points.
     */
    public double getLoyaltyPoints(
            Customer customer) {


        if (customer == null) {

            return 0.0;
        }


        return customer.getLoyaltyPoints();
    }


    // =========================================================
    // DETERMINE LOYALTY TIER
    // =========================================================

    /**
     * Determines the loyalty tier based on
     * the customer's accumulated points.
     */
    public LoyaltyTier determineTier(
            double loyaltyPoints) {


        // Platinum
        if (loyaltyPoints >=
                PLATINUM_THRESHOLD) {

            return LoyaltyTier.PLATINUM;
        }


        // Gold
        if (loyaltyPoints >=
                GOLD_THRESHOLD) {

            return LoyaltyTier.GOLD;
        }


        // Silver
        if (loyaltyPoints >=
                SILVER_THRESHOLD) {

            return LoyaltyTier.SILVER;
        }


        // Regular
        return LoyaltyTier.REGULAR;
    }


    // =========================================================
    // UPDATE CUSTOMER TIER
    // =========================================================

    /**
     * Updates the customer's loyalty tier
     * based on their current loyalty points.
     */
    public void updateLoyaltyTier(
            Customer customer) {


        if (customer == null) {

            return;
        }


        LoyaltyTier newTier =
                determineTier(
                        customer.getLoyaltyPoints()
                );


        customer.setLoyaltyTier(
                newTier
        );
    }


    // =========================================================
    // PROCESS LOYALTY POINTS
    // =========================================================

    /**
     * Adds loyalty points and then updates
     * the customer's loyalty tier.
     */
    public void processLoyaltyPoints(
            Customer customer,
            double orderAmount) {


        if (customer == null ||
                orderAmount <= 0) {

            return;
        }


        // Step 1: Add points
        addPoints(
                customer,
                orderAmount
        );


        // Step 2: Recalculate tier
        updateLoyaltyTier(
                customer
        );
    }


    // =========================================================
    // GET LOYALTY DISCOUNT
    // =========================================================

    /**
     * Returns the discount percentage associated
     * with the customer's current loyalty tier.
     */
    public double getLoyaltyDiscountPercentage(
            Customer customer) {


        if (customer == null ||
                customer.getLoyaltyTier() == null) {

            return 0.0;
        }


        switch (customer.getLoyaltyTier()) {


            // ---------------------------------------------
            // SILVER
            // ---------------------------------------------

            case SILVER:

                return 5.0;


            // ---------------------------------------------
            // GOLD
            // ---------------------------------------------

            case GOLD:

                return 10.0;


            // ---------------------------------------------
            // PLATINUM
            // ---------------------------------------------

            case PLATINUM:

                return 15.0;


            // ---------------------------------------------
            // REGULAR
            // ---------------------------------------------

            case REGULAR:

            default:

                return 0.0;
        }
    }
}