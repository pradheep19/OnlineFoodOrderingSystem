package com.foodordering.system.model;

import java.util.ArrayList;
import java.util.List;

public class Cart {

    private int cartId;
    private Customer customer;
    private List<OrderItem> items;

    public Cart(int cartId, Customer customer) {
        this.cartId = cartId;
        this.customer = customer;
        this.items = new ArrayList<>();
    }

    public int getCartId() {
        return cartId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void addItem(OrderItem orderItem) {
        for (OrderItem item : items) {
            if (item.getMenuItem().getItemId() ==
                    orderItem.getMenuItem().getItemId()) {

                item.setQuantity(item.getQuantity() +
                        orderItem.getQuantity());
                return;
            }
        }

        items.add(orderItem);
    }

    public boolean removeItem(int itemId) {
        return items.removeIf(
                item -> item.getMenuItem().getItemId() == itemId
        );
    }

    public void updateQuantity(int itemId, int quantity) {

        if (quantity <= 0) {
            removeItem(itemId);
            return;
        }

        for (OrderItem item : items) {
            if (item.getMenuItem().getItemId() == itemId) {
                item.setQuantity(quantity);
                return;
            }
        }
    }

    public double getSubtotal() {
        double subtotal = 0.0;

        for (OrderItem item : items) {
            subtotal += item.getTotalPrice();
        }

        return subtotal;
    }

    public int getTotalItemCount() {
        int total = 0;

        for (OrderItem item : items) {
            total += item.getQuantity();
        }

        return total;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        items.clear();
    }

    @Override
    public String toString() {
        return "Cart{" +
                "cartId=" + cartId +
                ", customer=" + customer.getName() +
                ", items=" + items +
                ", subtotal=" + getSubtotal() +
                '}';
    }
}
