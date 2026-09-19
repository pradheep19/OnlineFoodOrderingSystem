package com.foodordering.system.model;

import java.time.LocalTime;

public class Restaurant {

    private int restaurantId;
    private String name;
    private String address;

    private LocalTime openingTime;
    private LocalTime closingTime;

    private double deliveryRadius;
    private double minimumOrderValue;

    public Restaurant(int restaurantId, String name, String address,
                      LocalTime openingTime, LocalTime closingTime,
                      double deliveryRadius, double minimumOrderValue) {

        this.restaurantId = restaurantId;
        this.name = name;
        this.address = address;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.deliveryRadius = deliveryRadius;
        this.minimumOrderValue = minimumOrderValue;
    }

    // Getters

    public int getRestaurantId() {
        return restaurantId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public LocalTime getOpeningTime() {
        return openingTime;
    }

    public LocalTime getClosingTime() {
        return closingTime;
    }

    public double getDeliveryRadius() {
        return deliveryRadius;
    }

    public double getMinimumOrderValue() {
        return minimumOrderValue;
    }

    // Setters

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setOpeningTime(LocalTime openingTime) {
        this.openingTime = openingTime;
    }

    public void setClosingTime(LocalTime closingTime) {
        this.closingTime = closingTime;
    }

    public void setDeliveryRadius(double deliveryRadius) {
        this.deliveryRadius = deliveryRadius;
    }

    public void setMinimumOrderValue(double minimumOrderValue) {
        this.minimumOrderValue = minimumOrderValue;
    }

    @Override
    public String toString() {
        return "Restaurant{" +
                "restaurantId=" + restaurantId +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", openingTime=" + openingTime +
                ", closingTime=" + closingTime +
                ", deliveryRadius=" + deliveryRadius +
                ", minimumOrderValue=" + minimumOrderValue +
                '}';
    }
}
