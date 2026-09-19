package com.foodordering.system.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {

    private int orderId;
    private Customer customer;
    private Restaurant restaurant;
    private List<OrderItem> items;

    private double subtotal;
    private double dynamicPricingAmount;
    private double discountAmount;
    private double deliveryFee;
    private double finalAmount;

    private Coupon appliedCoupon;
    private OrderStatus status;
    private LocalDateTime orderDateTime;

    public Order(int orderId, Customer customer, Restaurant restaurant) {
        this.orderId = orderId;
        this.customer = customer;
        this.restaurant = restaurant;
        this.items = new ArrayList<>();
        this.status = OrderStatus.CREATED;
        this.orderDateTime = LocalDateTime.now();
    }

    public void addItem(OrderItem item) {
        if (item != null) {
            items.add(item);
        }
    }

    public int getOrderId() {
        return orderId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public double calculateItemSubtotal() {
        double total = 0;

        for (OrderItem item : items) {
            total += item.getTotalPrice();
        }

        return total;
    }

    public int getTotalItemCount() {
        int count = 0;

        for (OrderItem item : items) {
            count += item.getQuantity();
        }

        return count;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDynamicPricingAmount() {
        return dynamicPricingAmount;
    }

    public void setDynamicPricingAmount(double dynamicPricingAmount) {
        this.dynamicPricingAmount = dynamicPricingAmount;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(double deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public double getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(double finalAmount) {
        this.finalAmount = finalAmount;
    }

    public Coupon getAppliedCoupon() {
        return appliedCoupon;
    }

    public void setAppliedCoupon(Coupon appliedCoupon) {
        this.appliedCoupon = appliedCoupon;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getOrderDateTime() {
        return orderDateTime;
    }

    public void setOrderDateTime(LocalDateTime orderDateTime) {
        this.orderDateTime = orderDateTime;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", customer=" + customer.getName() +
                ", restaurant=" + restaurant.getName() +
                ", items=" + items.size() +
                ", subtotal=" + subtotal +
                ", dynamicPricingAmount=" + dynamicPricingAmount +
                ", discountAmount=" + discountAmount +
                ", deliveryFee=" + deliveryFee +
                ", finalAmount=" + finalAmount +
                ", status=" + status +
                '}';
    }
}