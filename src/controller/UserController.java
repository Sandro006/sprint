package controller;

import framework.annotations.Controller;
import framework.annotations.GetMapping;

@Controller
public class UserController {
    @GetMapping("/users")
    public String list() {
        return "Bonjour depuis UserController";
    }

    public String getUserById(int id) {
        return "Utilisateur avec l'ID : " + id;
    }
}



