package com.foodordering.system.model;

import java.time.LocalTime;

public class MenuItem {

    private int itemId;
    private int restaurantId;
    private String name;
    private String category;
    private double basePrice;
    private boolean available;
    private boolean dynamicPricingExempt;

    private LocalTime availableFrom;
    private LocalTime availableTo;

    public MenuItem(int itemId,
                    int restaurantId,
                    String name,
                    String category,
                    double basePrice,
                    boolean available,
                    boolean dynamicPricingExempt,
                    LocalTime availableFrom,
                    LocalTime availableTo) {

        this.itemId = itemId;
        this.restaurantId = restaurantId;
        this.name = name;
        this.category = category;
        this.basePrice = basePrice;
        this.available = available;
        this.dynamicPricingExempt = dynamicPricingExempt;
        this.availableFrom = availableFrom;
        this.availableTo = availableTo;
    }

    // Getters

    public int getItemId() {
        return itemId;
    }

    public int getRestaurantId() {
        return restaurantId;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isDynamicPricingExempt() {
        return dynamicPricingExempt;
    }

    public LocalTime getAvailableFrom() {
        return availableFrom;
    }

    public LocalTime getAvailableTo() {
        return availableTo;
    }

    // Setters

    public void setRestaurantId(int restaurantId) {
        this.restaurantId = restaurantId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void setDynamicPricingExempt(boolean dynamicPricingExempt) {
        this.dynamicPricingExempt = dynamicPricingExempt;
    }

    public void setAvailableFrom(LocalTime availableFrom) {
        this.availableFrom = availableFrom;
    }

    public void setAvailableTo(LocalTime availableTo) {
        this.availableTo = availableTo;
    }

    @Override
    public String toString() {
        return "MenuItem{" +
                "itemId=" + itemId +
                ", restaurantId=" + restaurantId +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", basePrice=" + basePrice +
                ", available=" + available +
                ", dynamicPricingExempt=" + dynamicPricingExempt +
                ", availableFrom=" + availableFrom +
                ", availableTo=" + availableTo +
                '}';
    }
}