package com.foodordering.system.model;
import java.io.Serializable;
public class OrderItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private MenuItem menuItem;
    private int quantity;
    private double itemPrice;

    public OrderItem(MenuItem menuItem, int quantity) {
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.itemPrice = menuItem.getBasePrice();
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getItemPrice() {
        return itemPrice;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setItemPrice(double itemPrice) {
        this.itemPrice = itemPrice;
    }

    public double getTotalPrice() {
        return itemPrice * quantity;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "menuItem=" + menuItem.getName() +
                ", quantity=" + quantity +
                ", itemPrice=" + itemPrice +
                ", totalPrice=" + getTotalPrice() +
                '}';
    }
}