package com.foodordering.system.controller;

import com.foodordering.system.dao.MenuDAO;
import com.foodordering.system.dao.RestaurantDAO;
import com.foodordering.system.model.MenuItem;
import com.foodordering.system.model.Restaurant;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class RestaurantController {

    private final RestaurantDAO restaurantDAO;
    private final MenuDAO menuDAO;

    public RestaurantController(
            RestaurantDAO restaurantDAO,
            MenuDAO menuDAO) {

        this.restaurantDAO = restaurantDAO;
        this.menuDAO = menuDAO;
    }

    // =========================================================
    // RESTAURANT LIST
    // =========================================================

    @GetMapping("/restaurants")
    public String restaurants(Model model) {

        List<Restaurant> restaurants = restaurantDAO.findAll();

        model.addAttribute("restaurants", restaurants);

        return "restaurants";
    }


    // =========================================================
    // RESTAURANT MENU
    // =========================================================

    @GetMapping("/restaurant/{id}")
    public String restaurantMenu(
            @PathVariable("id") int restaurantId,
            Model model) {

        Restaurant restaurant =
                restaurantDAO.findById(restaurantId);

        // If restaurant does not exist,
        // return to restaurant listing page.
        if (restaurant == null) {
            return "redirect:/restaurants";
        }

        List<MenuItem> menuItems =
                menuDAO.findAvailableItemsByRestaurantId(restaurantId);

        model.addAttribute("restaurant", restaurant);
        model.addAttribute("menuItems", menuItems);

        return "restaurant-menu";
    }
}