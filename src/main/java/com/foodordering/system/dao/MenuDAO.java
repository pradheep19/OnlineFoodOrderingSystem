package com.foodordering.system.dao;

import com.foodordering.system.model.MenuItem;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class MenuDAO {

    private final Map<Integer, MenuItem> menuItems = new HashMap<>();

    public MenuDAO() {
        loadSampleMenu();
    }

    private void loadSampleMenu() {

        // =========================================================
        // SPICE GARDEN - RESTAURANT ID 1
        // =========================================================

        menuItems.put(101, new MenuItem(
                101,
                1,
                "Chicken Biryani",
                "Main Course",
                220.0,
                true,
                false,
                LocalTime.of(11, 0),
                LocalTime.of(22, 30)
        ));

        menuItems.put(106, new MenuItem(
                106,
                1,
                "Paneer Butter Masala",
                "Main Course",
                180.0,
                true,
                false,
                LocalTime.of(11, 0),
                LocalTime.of(22, 30)
        ));

        menuItems.put(107, new MenuItem(
                107,
                1,
                "Veg Meals",
                "Main Course",
                150.0,
                true,
                false,
                LocalTime.of(11, 0),
                LocalTime.of(21, 30)
        ));


        // =========================================================
        // PIZZA PALACE - RESTAURANT ID 2
        // =========================================================

        menuItems.put(102, new MenuItem(
                102,
                2,
                "Paneer Pizza",
                "Pizza",
                280.0,
                true,
                false,
                LocalTime.of(11, 0),
                LocalTime.of(23, 0)
        ));

        menuItems.put(108, new MenuItem(
                108,
                2,
                "Margherita Pizza",
                "Pizza",
                250.0,
                true,
                false,
                LocalTime.of(11, 0),
                LocalTime.of(23, 0)
        ));

        menuItems.put(109, new MenuItem(
                109,
                2,
                "Garlic Bread",
                "Sides",
                120.0,
                true,
                false,
                LocalTime.of(11, 0),
                LocalTime.of(23, 0)
        ));


        // =========================================================
        // BURGER HUB - RESTAURANT ID 3
        // =========================================================

        menuItems.put(103, new MenuItem(
                103,
                3,
                "Veg Burger",
                "Fast Food",
                150.0,
                true,
                false,
                LocalTime.of(10, 0),
                LocalTime.of(22, 30)
        ));

        menuItems.put(104, new MenuItem(
                104,
                3,
                "French Fries",
                "Fast Food",
                100.0,
                true,
                false,
                LocalTime.of(10, 0),
                LocalTime.of(22, 30)
        ));

        menuItems.put(105, new MenuItem(
                105,
                3,
                "Ice Cream",
                "Dessert",
                90.0,
                true,
                true,
                LocalTime.of(11, 0),
                LocalTime.of(22, 30)
        ));
    }


    // =========================================================
    // FIND ITEM BY ID
    // =========================================================

    public MenuItem findById(int itemId) {
        return menuItems.get(itemId);
    }


    // =========================================================
    // FIND ALL ITEMS
    // =========================================================

    public List<MenuItem> findAll() {
        return new ArrayList<>(menuItems.values());
    }


    // =========================================================
    // FIND AVAILABLE ITEMS
    // =========================================================

    public List<MenuItem> findAvailableItems() {

        return menuItems.values()
                .stream()
                .filter(MenuItem::isAvailable)
                .toList();
    }


    // =========================================================
    // FIND ITEMS BY RESTAURANT
    // =========================================================

    public List<MenuItem> findByRestaurantId(int restaurantId) {

        return menuItems.values()
                .stream()
                .filter(item -> item.getRestaurantId() == restaurantId)
                .toList();
    }


    // =========================================================
    // FIND AVAILABLE ITEMS BY RESTAURANT
    // =========================================================

    public List<MenuItem> findAvailableItemsByRestaurantId(int restaurantId) {

        return menuItems.values()
                .stream()
                .filter(item ->
                        item.getRestaurantId() == restaurantId
                        && item.isAvailable()
                )
                .toList();
    }


    // =========================================================
    // ADD ITEM
    // =========================================================

    public void add(MenuItem menuItem) {
        menuItems.put(menuItem.getItemId(), menuItem);
    }


    // =========================================================
    // UPDATE ITEM
    // =========================================================

    public void update(MenuItem menuItem) {
        menuItems.put(menuItem.getItemId(), menuItem);
    }


    // =========================================================
    // DELETE ITEM
    // =========================================================

    public void delete(int itemId) {
        menuItems.remove(itemId);
    }


    // =========================================================
    // TOTAL MENU SIZE
    // =========================================================

    public int size() {
        return menuItems.size();
    }
}