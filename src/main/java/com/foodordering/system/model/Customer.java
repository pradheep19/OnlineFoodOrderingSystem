package com.foodordering.system.model;

public class Customer {

    private int customerId;
    private String name;
    private String phoneNumber;
    private String address;
    private double distanceFromRestaurant;

    private LoyaltyTier loyaltyTier;
    private double loyaltyPoints;

    public Customer(int customerId, String name, String phoneNumber,
                    String address, double distanceFromRestaurant,
                    LoyaltyTier loyaltyTier, double loyaltyPoints) {

        this.customerId = customerId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.distanceFromRestaurant = distanceFromRestaurant;
        this.loyaltyTier = loyaltyTier;
        this.loyaltyPoints = loyaltyPoints;
    }

    // Getters

    public int getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public double getDistanceFromRestaurant() {
        return distanceFromRestaurant;
    }

    public LoyaltyTier getLoyaltyTier() {
        return loyaltyTier;
    }

    public double getLoyaltyPoints() {
        return loyaltyPoints;
    }

    // Setters

    public void setName(String name) {
        this.name = name;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setDistanceFromRestaurant(double distanceFromRestaurant) {
        this.distanceFromRestaurant = distanceFromRestaurant;
    }

    public void setLoyaltyTier(LoyaltyTier loyaltyTier) {
        this.loyaltyTier = loyaltyTier;
    }

    public void setLoyaltyPoints(double loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    public void addLoyaltyPoints(double points) {
        this.loyaltyPoints += points;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerId=" + customerId +
                ", name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", address='" + address + '\'' +
                ", distanceFromRestaurant=" + distanceFromRestaurant +
                ", loyaltyTier=" + loyaltyTier +
                ", loyaltyPoints=" + loyaltyPoints +
                '}';
    }
}