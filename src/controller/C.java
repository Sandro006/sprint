package controller;
import framework.annotations.Controller;
import framework.annotations.GetMapping;

@Controller
public class C {
    @GetMapping("/c/test")
    public String goodString() {
        return "Hello from C COntrller";
    }
}
