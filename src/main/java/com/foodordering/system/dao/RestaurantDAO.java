package com.foodordering.system.dao;

import com.foodordering.system.model.Restaurant;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class RestaurantDAO {

    private final List<Restaurant> restaurants = new ArrayList<>();

    public RestaurantDAO() {
        loadSampleRestaurants();
    }

    private void loadSampleRestaurants() {

        restaurants.add(new Restaurant(
                1,
                "Spice Garden",
                "Vellore",
                LocalTime.of(10, 0),
                LocalTime.of(23, 0),
                10.0,
                100.0
        ));

        restaurants.add(new Restaurant(
                2,
                "Pizza Palace",
                "Katpadi",
                LocalTime.of(11, 0),
                LocalTime.of(23, 30),
                8.0,
                150.0
        ));

        restaurants.add(new Restaurant(
                3,
                "Burger Hub",
                "Vellore",
                LocalTime.of(10, 0),
                LocalTime.of(22, 30),
                7.0,
                80.0
        ));
    }

    public List<Restaurant> findAll() {
        return new ArrayList<>(restaurants);
    }

    public Restaurant findById(int restaurantId) {
        return restaurants.stream()
                .filter(r -> r.getRestaurantId() == restaurantId)
                .findFirst()
                .orElse(null);
    }

    public void add(Restaurant restaurant) {
        restaurants.add(restaurant);
    }

    public void delete(int restaurantId) {
        restaurants.removeIf(r -> r.getRestaurantId() == restaurantId);
    }

    public int count() {
        return restaurants.size();
    }
}