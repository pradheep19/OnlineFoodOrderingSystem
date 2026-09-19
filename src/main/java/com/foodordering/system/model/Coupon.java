package com.foodordering.system.model;

import java.time.LocalDate;

public class Coupon {

    private String couponCode;
    private String description;
    private double discountPercentage;
    private double maximumDiscount;
    private double minimumOrderValue;
    private LocalDate expiryDate;
    private boolean active;

    public Coupon(
            String couponCode,
            String description,
            double discountPercentage,
            double maximumDiscount,
            double minimumOrderValue,
            LocalDate expiryDate,
            boolean active) {

        this.couponCode = couponCode;
        this.description = description;
        this.discountPercentage = discountPercentage;
        this.maximumDiscount = maximumDiscount;
        this.minimumOrderValue = minimumOrderValue;
        this.expiryDate = expiryDate;
        this.active = active;
    }

    // =========================================================
    // GETTERS
    // =========================================================

    public String getCouponCode() {
        return couponCode;
    }

    /*
     * Compatibility getter.
     * CheckoutController can use getCode().
     */
    public String getCode() {
        return couponCode;
    }

    public String getDescription() {
        return description;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public double getMaximumDiscount() {
        return maximumDiscount;
    }

    public double getMinimumOrderValue() {
        return minimumOrderValue;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public boolean isActive() {
        return active;
    }

    // =========================================================
    // SETTERS
    // =========================================================

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public void setMaximumDiscount(double maximumDiscount) {
        this.maximumDiscount = maximumDiscount;
    }

    public void setMinimumOrderValue(double minimumOrderValue) {
        this.minimumOrderValue = minimumOrderValue;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // =========================================================
    // COUPON VALIDATION
    // =========================================================

    public boolean isValid(
            double orderValue,
            LocalDate currentDate) {

        // Coupon must be active
        if (!active) {
            return false;
        }

        // Order must satisfy minimum order value
        if (orderValue < minimumOrderValue) {
            return false;
        }

        // Check expiry only if an expiry date exists
        if (expiryDate != null &&
                currentDate.isAfter(expiryDate)) {

            return false;
        }

        return true;
    }

    // =========================================================
    // CALCULATE DISCOUNT
    // =========================================================

    public double calculateDiscount(double orderValue) {

        // Minimum order value check
        if (orderValue < minimumOrderValue) {
            return 0.0;
        }

        // Calculate percentage discount
        double discount =
                orderValue * discountPercentage / 100.0;

        // Apply maximum discount limit
        if (discount > maximumDiscount) {
            discount = maximumDiscount;
        }

        return discount;
    }

    // =========================================================
    // TO STRING
    // =========================================================

    @Override
    public String toString() {

        return "Coupon{" +
                "couponCode='" + couponCode + '\'' +
                ", description='" + description + '\'' +
                ", discountPercentage=" + discountPercentage +
                ", maximumDiscount=" + maximumDiscount +
                ", minimumOrderValue=" + minimumOrderValue +
                ", expiryDate=" + expiryDate +
                ", active=" + active +
                '}';
    }
}