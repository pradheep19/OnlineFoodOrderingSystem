package com.foodordering.system.controller;

import com.foodordering.system.dao.MenuDAO;
import com.foodordering.system.model.MenuItem;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final MenuDAO menuDAO;

    public HomeController(MenuDAO menuDAO) {
        this.menuDAO = menuDAO;
    }

    @GetMapping("/")
    public String home(Model model) {

        List<MenuItem> menuItems = menuDAO.findAvailableItems();

        model.addAttribute("menuItems", menuItems);

        return "index";
    }
}