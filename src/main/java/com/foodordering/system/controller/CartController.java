package com.foodordering.system.controller;

import com.foodordering.system.dao.MenuDAO;
import com.foodordering.system.dao.RestaurantDAO;
import com.foodordering.system.model.Cart;
import com.foodordering.system.model.Customer;
import com.foodordering.system.model.MenuItem;
import com.foodordering.system.model.OrderItem;
import com.foodordering.system.model.Restaurant;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CartController {

    private final MenuDAO menuDAO;
    private final RestaurantDAO restaurantDAO;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public CartController(
            MenuDAO menuDAO,
            RestaurantDAO restaurantDAO) {

        this.menuDAO = menuDAO;
        this.restaurantDAO = restaurantDAO;
    }


    // =========================================================
    // GET OR CREATE CART
    // =========================================================

    private Cart getCart(HttpSession session) {

        Cart cart =
                (Cart) session.getAttribute("cart");


        // -----------------------------------------------------
        // EXISTING CART
        // -----------------------------------------------------

        if (cart != null) {
            return cart;
        }


        // -----------------------------------------------------
        // GET LOGGED-IN CUSTOMER
        // -----------------------------------------------------

        Customer customer =
                (Customer) session.getAttribute(
                        "loggedInCustomer"
                );


        // -----------------------------------------------------
        // NO LOGIN
        // -----------------------------------------------------

        if (customer == null) {
            return null;
        }


        // -----------------------------------------------------
        // CREATE CART FOR LOGGED-IN CUSTOMER
        // -----------------------------------------------------

        cart =
                new Cart(
                        1,
                        customer
                );


        session.setAttribute(
                "cart",
                cart
        );


        return cart;
    }


    // =========================================================
    // ADD ITEM TO CART
    // =========================================================

    @PostMapping("/cart/add/{itemId}")
    public String addToCart(
            @PathVariable("itemId") int itemId,
            HttpSession session,
            Model model) {


        // -----------------------------------------------------
        // CHECK LOGIN
        // -----------------------------------------------------

        Customer customer =
                (Customer) session.getAttribute(
                        "loggedInCustomer"
                );

        if (customer == null) {
            return "redirect:/login";
        }


        // -----------------------------------------------------
        // FIND MENU ITEM
        // -----------------------------------------------------

        MenuItem menuItem =
                menuDAO.findById(itemId);


        if (menuItem == null ||
                !menuItem.isAvailable()) {

            return "redirect:/restaurants";
        }


        // -----------------------------------------------------
        // GET CUSTOMER'S CART
        // -----------------------------------------------------

        Cart cart =
                getCart(session);


        if (cart == null) {
            return "redirect:/login";
        }


        // =====================================================
        // ONE RESTAURANT PER CART
        // =====================================================

        if (!cart.getItems().isEmpty()) {


            int currentRestaurantId =
                    cart.getItems()
                            .get(0)
                            .getMenuItem()
                            .getRestaurantId();


            // -------------------------------------------------
            // DIFFERENT RESTAURANT
            // -------------------------------------------------

            if (currentRestaurantId !=
                    menuItem.getRestaurantId()) {


                Restaurant currentRestaurant =
                        restaurantDAO.findById(
                                currentRestaurantId
                        );


                model.addAttribute(
                        "error",
                        "Your cart contains items from "
                                + (
                                currentRestaurant != null
                                        ? currentRestaurant.getName()
                                        : "another restaurant"
                        )
                                + ". Please clear your cart before "
                                + "ordering from a different restaurant."
                );


                model.addAttribute(
                        "cart",
                        cart
                );


                model.addAttribute(
                        "restaurant",
                        currentRestaurant
                );


                return "cart";
            }
        }


        // =====================================================
        // ADD ITEM
        // =====================================================

        OrderItem orderItem =
                new OrderItem(
                        menuItem,
                        1
                );


        cart.addItem(
                orderItem
        );


        // -----------------------------------------------------
        // SAVE CART IN SESSION
        // -----------------------------------------------------

        session.setAttribute(
                "cart",
                cart
        );


        return "redirect:/cart";
    }


    // =========================================================
    // VIEW CART
    // =========================================================

    @GetMapping("/cart")
    public String viewCart(
            HttpSession session,
            Model model) {


        // -----------------------------------------------------
        // CHECK LOGIN
        // -----------------------------------------------------

        Customer customer =
                (Customer) session.getAttribute(
                        "loggedInCustomer"
                );


        if (customer == null) {
            return "redirect:/login";
        }


        // -----------------------------------------------------
        // GET CART
        // -----------------------------------------------------

        Cart cart =
                getCart(session);


        if (cart == null) {
            return "redirect:/login";
        }


        // -----------------------------------------------------
        // FIND RESTAURANT
        // -----------------------------------------------------

        Restaurant restaurant =
                null;


        if (!cart.getItems().isEmpty()) {


            int restaurantId =
                    cart.getItems()
                            .get(0)
                            .getMenuItem()
                            .getRestaurantId();


            restaurant =
                    restaurantDAO.findById(
                            restaurantId
                    );
        }


        // -----------------------------------------------------
        // SEND DATA TO THYMELEAF
        // -----------------------------------------------------

        model.addAttribute(
                "cart",
                cart
        );


        model.addAttribute(
                "restaurant",
                restaurant
        );


        model.addAttribute(
                "customer",
                customer
        );


        return "cart";
    }


    // =========================================================
    // INCREASE QUANTITY
    // =========================================================

    @PostMapping("/cart/increase/{itemId}")
    public String increaseQuantity(
            @PathVariable("itemId") int itemId,
            HttpSession session) {


        // -----------------------------------------------------
        // CHECK LOGIN
        // -----------------------------------------------------

        Customer customer =
                (Customer) session.getAttribute(
                        "loggedInCustomer"
                );


        if (customer == null) {
            return "redirect:/login";
        }


        // -----------------------------------------------------
        // GET CART
        // -----------------------------------------------------

        Cart cart =
                getCart(session);


        if (cart == null) {
            return "redirect:/login";
        }


        // -----------------------------------------------------
        // FIND MENU ITEM
        // -----------------------------------------------------

        MenuItem menuItem =
                menuDAO.findById(itemId);


        // -----------------------------------------------------
        // INCREASE
        // -----------------------------------------------------

        if (menuItem != null &&
                menuItem.isAvailable()) {


            OrderItem orderItem =
                    new OrderItem(
                            menuItem,
                            1
                    );


            cart.addItem(
                    orderItem
            );
        }


        // -----------------------------------------------------
        // SAVE
        // -----------------------------------------------------

        session.setAttribute(
                "cart",
                cart
        );


        return "redirect:/cart";
    }


    // =========================================================
    // DECREASE QUANTITY
    // =========================================================

    @PostMapping("/cart/decrease/{itemId}")
    public String decreaseQuantity(
            @PathVariable("itemId") int itemId,
            HttpSession session) {


        // -----------------------------------------------------
        // CHECK LOGIN
        // -----------------------------------------------------

        Customer customer =
                (Customer) session.getAttribute(
                        "loggedInCustomer"
                );


        if (customer == null) {
            return "redirect:/login";
        }


        // -----------------------------------------------------
        // GET CART
        // -----------------------------------------------------

        Cart cart =
                getCart(session);


        if (cart == null) {
            return "redirect:/login";
        }


        // -----------------------------------------------------
        // CURRENT QUANTITY
        // -----------------------------------------------------

        int currentQuantity =
                getCurrentQuantity(
                        cart,
                        itemId
                );


        // -----------------------------------------------------
        // DECREASE
        // -----------------------------------------------------

        if (currentQuantity > 1) {


            cart.updateQuantity(
                    itemId,
                    currentQuantity - 1
            );


        } else if (currentQuantity == 1) {


            cart.removeItem(
                    itemId
            );
        }


        // -----------------------------------------------------
        // SAVE
        // -----------------------------------------------------

        session.setAttribute(
                "cart",
                cart
        );


        return "redirect:/cart";
    }


    // =========================================================
    // GET CURRENT QUANTITY
    // =========================================================

    private int getCurrentQuantity(
            Cart cart,
            int itemId) {


        return cart.getItems()
                .stream()
                .filter(
                        item ->
                                item.getMenuItem()
                                        .getItemId()
                                        == itemId
                )
                .mapToInt(
                        OrderItem::getQuantity
                )
                .findFirst()
                .orElse(0);
    }


    // =========================================================
    // REMOVE ITEM
    // =========================================================

    @PostMapping("/cart/remove/{itemId}")
    public String removeItem(
            @PathVariable("itemId") int itemId,
            HttpSession session) {


        // -----------------------------------------------------
        // CHECK LOGIN
        // -----------------------------------------------------

        Customer customer =
                (Customer) session.getAttribute(
                        "loggedInCustomer"
                );


        if (customer == null) {
            return "redirect:/login";
        }


        // -----------------------------------------------------
        // GET CART
        // -----------------------------------------------------

        Cart cart =
                getCart(session);


        if (cart == null) {
            return "redirect:/login";
        }


        // -----------------------------------------------------
        // REMOVE
        // -----------------------------------------------------

        cart.removeItem(
                itemId
        );


        // -----------------------------------------------------
        // SAVE
        // -----------------------------------------------------

        session.setAttribute(
                "cart",
                cart
        );


        return "redirect:/cart";
    }


    // =========================================================
    // CLEAR CART
    // =========================================================

    @PostMapping("/cart/clear")
    public String clearCart(
            HttpSession session) {


        // -----------------------------------------------------
        // CHECK LOGIN
        // -----------------------------------------------------

        Customer customer =
                (Customer) session.getAttribute(
                        "loggedInCustomer"
                );


        if (customer == null) {
            return "redirect:/login";
        }


        // -----------------------------------------------------
        // GET CART
        // -----------------------------------------------------

        Cart cart =
                getCart(session);


        if (cart == null) {
            return "redirect:/login";
        }


        // -----------------------------------------------------
        // CLEAR
        // -----------------------------------------------------

        cart.clear();


        // -----------------------------------------------------
        // SAVE
        // -----------------------------------------------------

        session.setAttribute(
                "cart",
                cart
        );


        return "redirect:/cart";
    }
}